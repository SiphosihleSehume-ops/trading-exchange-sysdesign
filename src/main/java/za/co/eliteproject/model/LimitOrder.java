package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.enums.OrderSide;

import java.util.Comparator;

/**
 * An order with a price bound: BUY at or below the limit, SELL at or above it.
 * Natural ordering is book priority for the order's own side.
 */
public class LimitOrder extends Order implements Comparable<LimitOrder> {

    /** Buy book priority: higher price first, then earlier timestamp, then lower ID. */
    public static final Comparator<LimitOrder> BUY_PRIORITY =
            Comparator.comparingDouble(LimitOrder::getLimitPrice).reversed()
                    .thenComparingLong(LimitOrder::getTimestamp)
                    .thenComparingInt(LimitOrder::getOrderId);

    /** Sell book priority: lower price first, then earlier timestamp, then lower ID. */
    public static final Comparator<LimitOrder> SELL_PRIORITY =
            Comparator.comparingDouble(LimitOrder::getLimitPrice)
                    .thenComparingLong(LimitOrder::getTimestamp)
                    .thenComparingInt(LimitOrder::getOrderId);

    private final double limitPrice;

    public LimitOrder(int orderId, Trader trader, AssetType assetType, OrderSide side,
                      double quantity, double limitPrice) {
        super(orderId, trader, assetType, side, quantity);
        if (!(limitPrice > 0)) {
            throw new IllegalArgumentException("Limit price must be positive");
        }
        this.limitPrice = limitPrice;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    /** @return the book-priority comparator for the given side. */
    public static Comparator<LimitOrder> priorityFor(OrderSide side) {
        return side == OrderSide.BUY ? BUY_PRIORITY : SELL_PRIORITY;
    }

    @Override
    public String getOrderType() {
        return "LIMIT";
    }

    @Override
    public boolean canMatchAt(double price) {
        return side == OrderSide.BUY ? price <= limitPrice : price >= limitPrice;
    }

    @Override
    public int compareTo(LimitOrder other) {
        return priorityFor(side).compare(this, other);
    }

    @Override
    public String toString() {
        return super.toString() + "[limitPrice=" + limitPrice + "]";
    }
}
