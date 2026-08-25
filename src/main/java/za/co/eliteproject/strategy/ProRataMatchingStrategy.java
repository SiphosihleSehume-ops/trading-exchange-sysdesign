package za.co.eliteproject.strategy;

import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.TradeRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Pro-rata matching: when multiple sell orders are eligible against the best buy order,
 * the buy quantity is distributed proportionally across those sellers by their remaining size.
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

            // Collect all eligible sellers: limitPrice <= buy.limitPrice
            List<LimitOrder> eligibleSellers = new ArrayList<>();
            for (LimitOrder sell : sellQueue) {
                if (sell.getLimitPrice() <= bestBuy.getLimitPrice()) {
                    eligibleSellers.add(sell);
                }
            }

            if (eligibleSellers.isEmpty()) {
                break;
            }

            double totalEligibleQty = 0.0;
            for (LimitOrder sell : eligibleSellers) {
                totalEligibleQty += sell.getRemainingQty();
            }

            double buyRemaining = bestBuy.getRemainingQty();

            // Sort eligible sellers deterministically: best price first (lowest), then earliest timestamp,
            // and track the largest remaining-quantity seller for remainder allocation.
            eligibleSellers.sort(Comparator
                    .comparingDouble(LimitOrder::getLimitPrice)
                    .thenComparingLong(LimitOrder::getTimestamp));

            LimitOrder largestSeller = eligibleSellers.get(0);
            for (LimitOrder sell : eligibleSellers) {
                if (sell.getRemainingQty() > largestSeller.getRemainingQty()) {
                    largestSeller = sell;
                }
            }

            double allocatedTotal = 0.0;
            List<LimitOrder> filledThisRound = new ArrayList<>();

            for (LimitOrder sell : eligibleSellers) {
                double proportion = sell.getRemainingQty() / totalEligibleQty;
                double allocation = Math.floor(proportion * buyRemaining);

                if (sell == largestSeller) {
                    // remainder handled after loop
                    continue;
                }

                if (allocation > 0) {
                    double matchedQty = Math.min(allocation, Math.min(sell.getRemainingQty(), buyRemaining - allocatedTotal));
                    if (matchedQty > 1e-9) {
                        double executionPrice = sell.getLimitPrice();
                        bestBuy.fill(matchedQty);
                        sell.fill(matchedQty);
                        trades.add(new TradeRecord(tradeId, bestBuy.getOrderId(), sell.getOrderId(),
                                bestBuy.getAssetType(), matchedQty, executionPrice));
                        tradeId++;
                        allocatedTotal += matchedQty;
                        if (sell.getRemainingQty() <= 1e-9) {
                            filledThisRound.add(sell);
                        }
                    }
                }
            }

            // Remainder goes to the largest remaining-quantity seller
            double remainder = Math.min(buyRemaining - allocatedTotal, largestSeller.getRemainingQty());
            if (remainder > 1e-9) {
                double executionPrice = largestSeller.getLimitPrice();
                bestBuy.fill(remainder);
                largestSeller.fill(remainder);
                trades.add(new TradeRecord(tradeId, bestBuy.getOrderId(), largestSeller.getOrderId(),
                        bestBuy.getAssetType(), remainder, executionPrice));
                tradeId++;
                allocatedTotal += remainder;
                if (largestSeller.getRemainingQty() <= 1e-9) {
                    filledThisRound.add(largestSeller);
                }
            }

            // Remove fully filled sellers from the queue
            for (LimitOrder filled : filledThisRound) {
                sellQueue.remove(filled);
            }

            // Remove buy order if fully filled
            if (bestBuy.getRemainingQty() <= 1e-9) {
                buyQueue.poll();
            }

            // If nothing was allocated this round (e.g. buy fully filled at zero remaining, or no
            // eligible seller had capacity), stop to avoid an infinite loop.
            if (allocatedTotal <= 1e-9) {
                break;
            }
        }

        return trades;
    }
}
