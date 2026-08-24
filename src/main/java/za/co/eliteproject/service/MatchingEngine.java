package za.co.eliteproject.service;

import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.MarketOrder;
import za.co.eliteproject.model.Order;
import za.co.eliteproject.model.TradeRecord;
import za.co.eliteproject.strategy.MatchingStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Orchestrates order books per market, runs risk checks, and executes matches.
 */
public class MatchingEngine {

    private final HashMap<String, Market> markets;
    private final HashMap<String, PriorityQueue<LimitOrder>> buyQueues;
    private final HashMap<String, PriorityQueue<LimitOrder>> sellQueues;
    private final RiskEngine riskEngine;
    private MatchingStrategy matchingStrategy;
    private final AuditLog auditLog;
    private final MarketDataFeed dataFeed;
    private int tradeCounter;

    public MatchingEngine(RiskEngine riskEngine, MatchingStrategy strategy, AuditLog auditLog,
                           MarketDataFeed dataFeed) {
        this.markets = new HashMap<>();
        this.buyQueues = new HashMap<>();
        this.sellQueues = new HashMap<>();
        this.riskEngine = riskEngine;
        this.matchingStrategy = strategy;
        this.auditLog = auditLog;
        this.dataFeed = dataFeed;
        this.tradeCounter = 1;
    }

    /**
     * Adds a market; initialises buy/sell queues with the correct Comparator.
     */
    public void registerMarket(Market market) {
        if (market == null) {
            throw new IllegalArgumentException("Market cannot be null");
        }
        String marketId = market.getMarketId();
        markets.put(marketId, market);

        // Buy queue: higher limit price first; ties resolved by earlier timestamp (ascending).
        Comparator<LimitOrder> buyComparator = Comparator
                .comparingDouble(LimitOrder::getLimitPrice).reversed()
                .thenComparingLong(LimitOrder::getTimestamp);
        buyQueues.put(marketId, new PriorityQueue<>(buyComparator));

        // Sell queue: lower limit price first; ties resolved by earlier timestamp (ascending).
        Comparator<LimitOrder> sellComparator = Comparator
                .comparingDouble(LimitOrder::getLimitPrice)
                .thenComparingLong(LimitOrder::getTimestamp);
        sellQueues.put(marketId, new PriorityQueue<>(sellComparator));
    }

    /**
     * Runs risk checks, adds the order to the book, runs matching, and returns generated trades.
     */
    public List<TradeRecord> submitOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        String marketId = findMarketId(order);
        Market market = markets.get(marketId);
        if (market == null) {
            throw new IllegalArgumentException("Market not found for order: " + order.getOrderId());
        }
        if (!market.isOpen()) {
            throw new IllegalStateException("Market is closed: " + marketId);
        }

        if (!riskEngine.evaluate(order, market)) {
            order.reject();
            auditLog.log("ORDER_REJECTED", "Order " + order.getOrderId() + " rejected by risk engine");
            return new ArrayList<>();
        }

        if (order instanceof LimitOrder && order.getSide() == OrderSide.BUY) {
            LimitOrder limitOrder = (LimitOrder) order;
            boolean reserved = order.getTrader().getPortfolio()
                    .reserveCash(limitOrder.getQuantity() * limitOrder.getLimitPrice());
            if (!reserved) {
                order.reject();
                auditLog.log("ORDER_REJECTED", "Order " + order.getOrderId() + " rejected: insufficient cash to reserve");
                return new ArrayList<>();
            }
        }

        PriorityQueue<LimitOrder> buyQueue = buyQueues.get(marketId);
        PriorityQueue<LimitOrder> sellQueue = sellQueues.get(marketId);

        List<TradeRecord> marketOrderTrades = new ArrayList<>();

        if (order instanceof LimitOrder) {
            LimitOrder limitOrder = (LimitOrder) order;
            if (order.getSide() == OrderSide.BUY) {
                buyQueue.add(limitOrder);
            } else {
                sellQueue.add(limitOrder);
            }
        } else if (order instanceof MarketOrder) {
            // MarketOrders match immediately against the opposite book at the resting orders'
            // limit prices, walking the book until filled or the book is exhausted.
            marketOrderTrades.addAll(matchMarketOrder((MarketOrder) order,
                    order.getSide() == OrderSide.BUY ? sellQueue : buyQueue));
        }

        market.submitOrder(order);

        List<TradeRecord> trades = matchingStrategy.match(buyQueue, sellQueue, tradeCounter + marketOrderTrades.size());
        List<TradeRecord> allTrades = new ArrayList<>(marketOrderTrades);
        allTrades.addAll(trades);
        trades = allTrades;

        for (TradeRecord trade : trades) {
            market.recordTrade(trade);
            dataFeed.publishTradeExecuted(trade);
            dataFeed.publishPriceUpdate(marketId, market.getAssetType(), trade.getPrice());
            auditLog.log("TRADE_EXECUTED", "Trade " + trade.getTradeId() + " executed on " + marketId
                    + " qty=" + trade.getQuantity() + " price=" + trade.getPrice());
            tradeCounter++;
        }

        auditLog.log("ORDER_SUBMITTED", "Order " + order.getOrderId() + " submitted to " + marketId);

        return trades;
    }

    /**
     * Matches a MarketOrder immediately against the opposite side's resting LimitOrder book,
     * walking price levels (best price first, per the queue's natural priority) until the
     * MarketOrder is filled or the opposite book is exhausted. Execution price is always the
     * resting LimitOrder's limit price.
     */
    private List<TradeRecord> matchMarketOrder(MarketOrder marketOrder, PriorityQueue<LimitOrder> oppositeQueue) {
        List<TradeRecord> trades = new ArrayList<>();
        int tradeId = tradeCounter;

        while (marketOrder.getRemainingQty() > 1e-9 && !oppositeQueue.isEmpty()) {
            LimitOrder resting = oppositeQueue.peek();
            double executionPrice = resting.getLimitPrice();
            double matchedQty = Math.min(marketOrder.getRemainingQty(), resting.getRemainingQty());

            marketOrder.fill(matchedQty);
            resting.fill(matchedQty);

            int buyOrderId = marketOrder.getSide() == OrderSide.BUY ? marketOrder.getOrderId() : resting.getOrderId();
            int sellOrderId = marketOrder.getSide() == OrderSide.BUY ? resting.getOrderId() : marketOrder.getOrderId();

            trades.add(new TradeRecord(tradeId, buyOrderId, sellOrderId,
                    marketOrder.getAssetType(), matchedQty, executionPrice));
            tradeId++;

            if (resting.getRemainingQty() <= 1e-9) {
                oppositeQueue.poll();
            }
        }

        return trades;
    }

    /**
     * Cancels an order if found and not terminal; releases reserved cash if it was a BUY LimitOrder.
     */
    public boolean cancelOrder(int orderId, String marketId) {
        PriorityQueue<LimitOrder> buyQueue = buyQueues.get(marketId);
        PriorityQueue<LimitOrder> sellQueue = sellQueues.get(marketId);
        if (buyQueue == null || sellQueue == null) {
            return false;
        }

        LimitOrder found = null;
        for (LimitOrder order : buyQueue) {
            if (order.getOrderId() == orderId) {
                found = order;
                break;
            }
        }
        if (found != null) {
            buyQueue.remove(found);
        } else {
            for (LimitOrder order : sellQueue) {
                if (order.getOrderId() == orderId) {
                    found = order;
                    break;
                }
            }
            if (found != null) {
                sellQueue.remove(found);
            }
        }

        if (found == null || found.getStatus().isTerminal()) {
            return false;
        }

        found.cancel();
        if (found.getSide() == OrderSide.BUY) {
            found.getTrader().getPortfolio()
                    .releaseCash(found.getRemainingQty() * found.getLimitPrice());
        }
        auditLog.log("ORDER_CANCELLED", "Order " + orderId + " cancelled on " + marketId);
        return true;
    }

    /**
     * @return a defensive copy of the appropriate queue as a sorted list.
     */
    public List<LimitOrder> getOrderBook(String marketId, OrderSide side) {
        PriorityQueue<LimitOrder> queue = side == OrderSide.BUY ? buyQueues.get(marketId) : sellQueues.get(marketId);
        if (queue == null) {
            return new ArrayList<>();
        }
        List<LimitOrder> copy = new ArrayList<>(queue);
        Comparator<LimitOrder> comparator = side == OrderSide.BUY
                ? Comparator.comparingDouble(LimitOrder::getLimitPrice).reversed()
                        .thenComparingLong(LimitOrder::getTimestamp)
                : Comparator.comparingDouble(LimitOrder::getLimitPrice)
                        .thenComparingLong(LimitOrder::getTimestamp);
        copy.sort(comparator);
        return copy;
    }

    public Market getMarket(String marketId) {
        return markets.get(marketId);
    }

    public List<Market> getAllMarkets() {
        return new ArrayList<>(markets.values());
    }

    /**
     * Swaps the matching strategy at runtime.
     */
    public void setMatchingStrategy(MatchingStrategy matchingStrategy) {
        if (matchingStrategy == null) {
            throw new IllegalArgumentException("Matching strategy cannot be null");
        }
        this.matchingStrategy = matchingStrategy;
    }

    /**
     * Determines which market an order belongs to by matching its AssetType.
     * Since markets are registered per AssetType (one market per asset in typical usage),
     * this looks up the first market whose assetType matches the order's assetType.
     */
    private String findMarketId(Order order) {
        for (Market market : markets.values()) {
            if (market.getAssetType() == order.getAssetType()) {
                return market.getMarketId();
            }
        }
        return null;
    }
}
