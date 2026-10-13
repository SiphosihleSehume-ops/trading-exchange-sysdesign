package za.co.eliteproject.strategy;

import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Order;
import za.co.eliteproject.model.TradeRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Pro-rata matching: the best buy order is distributed across all eligible sell orders
 * (limitPrice <= buy limitPrice) in proportion to their remaining quantities.
 * Each seller receives floor(sellerRemaining / totalEligible * buyRemaining); any rounding
 * remainder goes to the seller with the largest remaining quantity. Each trade executes at
 * the seller's own limit price.
 */
public class ProRataMatchingStrategy implements MatchingStrategy {

    @Override
    public List<TradeRecord> match(PriorityQueue<LimitOrder> buyQueue,
                                   PriorityQueue<LimitOrder> sellQueue,
                                   int tradeIdStart) {
        List<TradeRecord> trades = new ArrayList<>();
        int tradeId = tradeIdStart;

        while (!buyQueue.isEmpty() && !sellQueue.isEmpty()) {
            LimitOrder bestBuy = buyQueue.peek();

            List<LimitOrder> eligible = new ArrayList<>();
            double totalEligibleQty = 0.0;
            for (LimitOrder sell : sellQueue) {
                if (sell.getLimitPrice() <= bestBuy.getLimitPrice()) {
                    eligible.add(sell);
                    totalEligibleQty += sell.getRemainingQty();
                }
            }
            if (eligible.isEmpty()) {
                break;
            }

            // Deterministic order (best price, then time) so remainder/ID allocation is stable.
            eligible.sort(LimitOrder.SELL_PRIORITY);

            LimitOrder largest = eligible.get(0);
            for (LimitOrder sell : eligible) {
                if (sell.getRemainingQty() > largest.getRemainingQty()) {
                    largest = sell;
                }
            }

            double buyRemaining = bestBuy.getRemainingQty();
            double allocated = 0.0;

            for (LimitOrder sell : eligible) {
                if (sell == largest) {
                    continue; // receives the remainder below
                }
                // Multiply before dividing and nudge by epsilon so e.g. 10*30/30 floors to 10, not 9.
                double share = Math.floor(sell.getRemainingQty() * buyRemaining / totalEligibleQty + Order.QTY_EPSILON);
                double qty = Math.min(share, Math.min(sell.getRemainingQty(), buyRemaining - allocated));
                if (qty > Order.QTY_EPSILON) {
                    trades.add(executeLeg(tradeId++, bestBuy, sell, qty, sellQueue));
                    allocated += qty;
                }
            }

            double remainder = Math.min(buyRemaining - allocated, largest.getRemainingQty());
            if (remainder > Order.QTY_EPSILON) {
                trades.add(executeLeg(tradeId++, bestBuy, largest, remainder, sellQueue));
                allocated += remainder;
            }

            if (bestBuy.isFullyFilled()) {
                buyQueue.poll();
            }

            if (allocated <= Order.QTY_EPSILON) {
                break; // nothing could be allocated; avoid looping forever
            }
        }

        return trades;
    }

    /** Executes one buyer/seller leg and drops the seller from the book once fully filled. */
    private static TradeRecord executeLeg(int tradeId, LimitOrder buy, LimitOrder sell, double qty,
                                      PriorityQueue<LimitOrder> sellQueue) {
        TradeRecord trade = MatchingStrategy.executeTrade(tradeId, buy, sell, qty, sell.getLimitPrice());
        if (sell.isFullyFilled()) {
            sellQueue.remove(sell);
        }
        return trade;
    }
}
