package za.co.eliteproject.risk;

import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Order;

/**
 * Rejects a BUY LimitOrder if the trader's available cash is below the required fraction
 * of the order value. SELL orders and MarketOrders always pass.
 */
public class MarginRequirementRule implements RiskRule {

    private final double marginFraction;

    public MarginRequirementRule(double marginFraction) {
        if (marginFraction < 0 || marginFraction > 1) {
            throw new IllegalArgumentException("Margin fraction must be between 0 and 1");
        }
        this.marginFraction = marginFraction;
    }

    @Override
    public boolean evaluate(Order order, Market market) {
        if (order.getSide() != OrderSide.BUY || !(order instanceof LimitOrder limitOrder)) {
            return true;
        }
        double requiredMargin = order.getQuantity() * limitOrder.getLimitPrice() * marginFraction;
        return order.getTrader().getPortfolio().getCashBalance() >= requiredMargin;
    }

    @Override
    public String getRuleName() {
        return "MarginRequirementRule";
    }
}
