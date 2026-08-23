package za.co.eliteproject.risk;

import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Order;

/**
 * Rejects a BUY LimitOrder if it would push the trader's total exposure over the configured limit.
 */
public class PositionLimitRule implements RiskRule {

    private final double maxPositionValue;

    public PositionLimitRule(double maxPositionValue) {
        this.maxPositionValue = maxPositionValue;
    }

    @Override
    public boolean evaluate(Order order, Market market) {
        if (order.getSide() != OrderSide.BUY || !(order instanceof LimitOrder)) {
            return true;
        }
        LimitOrder limitOrder = (LimitOrder) order;
        double projectedExposure = order.getTrader().getTotalExposure()
                + (order.getQuantity() * limitOrder.getLimitPrice());
        return projectedExposure <= maxPositionValue;
    }

    @Override
    public String getRuleName() {
        return "PositionLimitRule";
    }
}
