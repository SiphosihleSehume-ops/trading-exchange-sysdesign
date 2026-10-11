package za.co.eliteproject.risk;

import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Order;

/**
 * Rejects any order from a blacklisted trader.
 */
public class BlacklistRule implements RiskRule {

    @Override
    public boolean evaluate(Order order, Market market) {
        return !order.getTrader().isBlacklisted();
    }

    @Override
    public String getRuleName() {
        return "BlacklistRule";
    }
}
