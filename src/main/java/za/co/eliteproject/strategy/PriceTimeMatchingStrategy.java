package za.co.eliteproject.strategy;

import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.TradeRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Standard price-time priority matching: best price first, ties broken by earliest timestamp.
 * Execution price is always the resting sell order's limit price.
 */
public class PriceTimeMatchingStrategy implements MatchingStrategy {

    @Override
    public List<TradeRecord> match(PriorityQueue<LimitOrder> buyQueue,
                                     PriorityQueue<LimitOrder> sellQueue,
                                     int tradeIdStart) {
        List<TradeRecord> trades = new ArrayList<>();
        int tradeId = tradeIdStart;

        while (!buyQueue.isEmpty() && !sellQueue.isEmpty()) {
            LimitOrder bestBuy = buyQueue.peek();
            LimitOrder bestSell = sellQueue.peek();

            if (bestBuy.getLimitPrice() < bestSell.getLimitPrice()) {
                break;
            }

            double executionPrice = bestSell.getLimitPrice();
            double matchedQty = Math.min(bestBuy.getRemainingQty(), bestSell.getRemainingQty());

            bestBuy.fill(matchedQty);
            bestSell.fill(matchedQty);

            trades.add(new TradeRecord(tradeId, bestBuy.getOrderId(), bestSell.getOrderId(),
                    bestBuy.getAssetType(), matchedQty, executionPrice));
            tradeId++;

            if (bestBuy.getRemainingQty() <= 1e-9) {
                buyQueue.poll();
            }
            if (bestSell.getRemainingQty() <= 1e-9) {
                sellQueue.poll();
            }
        }

        return trades;
    }
}
