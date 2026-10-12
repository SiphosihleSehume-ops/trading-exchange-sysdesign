package za.co.eliteproject.service;

import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.MarketOrder;
import za.co.eliteproject.model.Order;
import za.co.eliteproject.model.TradeRecord;
import za.co.eliteproject.strategy.MatchingStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Orchestrates order books per market, runs risk checks, and executes matches.
 */
public class MatchingEngine {

    private final HashMap<String, Market> markets = new HashMap<>();
    private final HashMap<String, PriorityQueue<LimitOrder>> buyQueues = new HashMap<>();
    private final HashMap<String, PriorityQueue<LimitOrder>> sellQueues = new HashMap<>();
    private final RiskEngine riskEngine;
    private MatchingStrategy matchingStrategy;
    private final AuditLog auditLog;
    private final MarketDataFeed dataFeed;
    private int tradeCounter = 1;

    public MatchingEngine(RiskEngine riskEngine, MatchingStrategy strategy, AuditLog auditLog,
                          MarketDataFeed dataFeed) {
        if (riskEngine == null || strategy == null || auditLog == null || dataFeed == null) {
            throw new IllegalArgumentException("Engine dependencies cannot be null");
        }
        this.riskEngine = riskEngine;
        this.matchingStrategy = strategy;
        this.auditLog = auditLog;
        this.dataFeed = dataFeed;
    }

    /** Adds a market and initialises its buy/sell queues with the side-appropriate Comparator. */
    public void registerMarket(Market market) {
        if (market == null) {
            throw new IllegalArgumentException("Market cannot be null");
        }
        String marketId = market.getMarketId();
        markets.put(marketId, market);
        buyQueues.put(marketId, new PriorityQueue<>(LimitOrder.priorityFor(OrderSide.BUY)));
        sellQueues.put(marketId, new PriorityQueue<>(LimitOrder.priorityFor(OrderSide.SELL)));
    }

    /**
     * Runs risk checks, adds the order to the book, runs matching, and returns generated trades.
     *
     * @throws IllegalArgumentException if no market is registered for the order's asset
     * @throws IllegalStateException    if that market is closed
     */
    public List<TradeRecord> submitOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        Market market = findMarket(order);
        if (market == null) {
            throw new IllegalArgumentException("No market registered for " + order.getAssetType());
        }
        if (!market.isOpen()) {
            throw new IllegalStateException("Market is closed: " + market.getMarketId());
        }

        if (!riskEngine.evaluate(order, market)) {
            return rejectOrder(order, "rejected by risk engine");
        }
        if (order instanceof LimitOrder limit && order.getSide() == OrderSide.BUY
                && !order.getTrader().getPortfolio().reserveCash(limit.getQuantity() * limit.getLimitPrice())) {
            return rejectOrder(order, "rejected: insufficient cash to reserve");
        }

        String marketId = market.getMarketId();
        PriorityQueue<LimitOrder> buyQueue = buyQueues.get(marketId);
        PriorityQueue<LimitOrder> sellQueue = sellQueues.get(marketId);

        List<TradeRecord> trades = new ArrayList<>();
        if (order instanceof LimitOrder limit) {
            (order.getSide() == OrderSide.BUY ? buyQueue : sellQueue).add(limit);
        } else if (order instanceof MarketOrder marketOrder) {
            // Market orders never rest in the book: they match immediately or not at all.
            trades.addAll(matchMarketOrder(marketOrder, order.getSide() == OrderSide.BUY ? sellQueue : buyQueue));
        }

        market.submitOrder(order);
        trades.addAll(matchingStrategy.match(buyQueue, sellQueue, tradeCounter + trades.size()));

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
     * Cancels a resting order if found and not terminal; releases the cash reserved for an
     * unfilled BUY LimitOrder.
     */
    public boolean cancelOrder(int orderId, String marketId) {
        PriorityQueue<LimitOrder> buyQueue = buyQueues.get(marketId);
        PriorityQueue<LimitOrder> sellQueue = sellQueues.get(marketId);
        if (buyQueue == null || sellQueue == null) {
            return false;
        }

        PriorityQueue<LimitOrder> book = buyQueue;
        LimitOrder order = findInQueue(buyQueue, orderId);
        if (order == null) {
            book = sellQueue;
            order = findInQueue(sellQueue, orderId);
        }
        if (order == null || order.getStatus().isTerminal()) {
            return false;
        }

        book.remove(order);
        order.cancel();
        if (order.getSide() == OrderSide.BUY) {
            double unfilledReserve = order.getRemainingQty() * order.getLimitPrice();
            if (unfilledReserve > 0) {
                order.getTrader().getPortfolio().releaseCash(unfilledReserve);
            }
        }
        auditLog.log("ORDER_CANCELLED", "Order " + orderId + " cancelled on " + marketId);
        return true;
    }

    /** @return a defensive copy of the buy or sell book, best-priority first. */
    public List<LimitOrder> getOrderBook(String marketId, OrderSide side) {
        PriorityQueue<LimitOrder> queue = (side == OrderSide.BUY ? buyQueues : sellQueues).get(marketId);
        if (queue == null) {
            return new ArrayList<>();
        }
        List<LimitOrder> copy = new ArrayList<>(queue);
        copy.sort(LimitOrder.priorityFor(side));
        return copy;
    }

    public Market getMarket(String marketId) {
        return markets.get(marketId);
    }

    public List<Market> getAllMarkets() {
        return new ArrayList<>(markets.values());
    }

    /** Swaps the matching strategy at runtime. */
    public void setMatchingStrategy(MatchingStrategy matchingStrategy) {
        if (matchingStrategy == null) {
            throw new IllegalArgumentException("Matching strategy cannot be null");
        }
        this.matchingStrategy = matchingStrategy;
    }

    /**
     * Matches a MarketOrder against the opposite book, best price first, until it is filled or
     * the book is exhausted. Execution price is each resting order's limit price.
     */
    private List<TradeRecord> matchMarketOrder(MarketOrder marketOrder, PriorityQueue<LimitOrder> oppositeQueue) {
        List<TradeRecord> trades = new ArrayList<>();
        int tradeId = tradeCounter;
        boolean marketOrderBuys = marketOrder.getSide() == OrderSide.BUY;

        while (!marketOrder.isFullyFilled() && !oppositeQueue.isEmpty()) {
            LimitOrder resting = oppositeQueue.peek();
            double matchedQty = Math.min(marketOrder.getRemainingQty(), resting.getRemainingQty());

            trades.add(marketOrderBuys
                    ? MatchingStrategy.executeTrade(tradeId++, marketOrder, resting, matchedQty, resting.getLimitPrice())
                    : MatchingStrategy.executeTrade(tradeId++, resting, marketOrder, matchedQty, resting.getLimitPrice()));

            if (resting.isFullyFilled()) {
                oppositeQueue.poll();
            }
        }
        return trades;
    }

    /** Rejects the order, logs why, and returns the empty trade list a rejection yields. */
    private List<TradeRecord> rejectOrder(Order order, String reason) {
        order.reject();
        auditLog.log("ORDER_REJECTED", "Order " + order.getOrderId() + " " + reason);
        return new ArrayList<>();
    }

    private static LimitOrder findInQueue(PriorityQueue<LimitOrder> queue, int orderId) {
        for (LimitOrder candidate : queue) {
            if (candidate.getOrderId() == orderId) {
                return candidate;
            }
        }
        return null;
    }

    /** Orders carry no market ID, so the market is the one registered for the order's asset type. */
    private Market findMarket(Order order) {
        for (Market market : markets.values()) {
            if (market.getAssetType() == order.getAssetType()) {
                return market;
            }
        }
        return null;
    }
}
