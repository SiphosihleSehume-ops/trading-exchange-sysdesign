package za.co.eliteproject.risk;

import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Order;

/**
 * Rejects a BUY LimitOrder if it would push the trader's total exposure over the configured limit.
 * MarketOrders always pass (price unknown).
 */
public class PositionLimitRule implements RiskRule {

    private final double maxPositionValue;

    public PositionLimitRule(double maxPositionValue) {
        if (maxPositionValue < 0) {
            throw new IllegalArgumentException("Maximum position value cannot be negative");
        }
        this.maxPositionValue = maxPositionValue;
    }

    @Override
    public boolean evaluate(Order order, Market market) {
        if (order.getSide() != OrderSide.BUY || !(order instanceof LimitOrder limitOrder)) {
            return true;
        }
        double projectedExposure = order.getTrader().getTotalExposure()
                + (order.getQuantity() * limitOrder.getLimitPrice());
        return projectedExposure <= maxPositionValue;
    }

    @Override
    public String getRuleName() {
        return "PositionLimitRule";
    }
}
