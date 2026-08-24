package za.co.eliteproject.service;

import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Order;
import za.co.eliteproject.risk.RiskRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain of Responsibility: evaluates an order against a sequence of RiskRules.
 */
public class RiskEngine {

    private final List<RiskRule> rules;

    public RiskEngine() {
        this.rules = new ArrayList<>();
    }

    public void addRule(RiskRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule cannot be null");
        }
        rules.add(rule);
    }

    public List<RiskRule> getRules() {
        return new ArrayList<>(rules);
    }

    /**
     * Runs all rules in order; returns false (and stops) on the first rule failure.
     */
    public boolean evaluate(Order order, Market market) {
        for (RiskRule rule : rules) {
            if (!rule.evaluate(order, market)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the first rule that rejects the order, or null if all pass.
     */
    public RiskRule getFailingRule(Order order, Market market) {
        for (RiskRule rule : rules) {
            if (!rule.evaluate(order, market)) {
                return rule;
            }
        }
        return null;
    }
}
