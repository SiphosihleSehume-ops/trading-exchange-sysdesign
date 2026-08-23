
package za.co.eliteproject.risk;

import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Order;

/**
 * A single rule in the RiskEngine's chain of responsibility.
 */
public interface RiskRule {

    /**
     * @return true if the order passes the rule, false if it should be rejected.
     */
    boolean evaluate(Order order, Market market);

    String getRuleName();
}
