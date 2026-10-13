package za.co.eliteproject;

import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.model.LimitOrder;
import za.co.eliteproject.model.Market;
import za.co.eliteproject.model.Portfolio;
import za.co.eliteproject.model.TradeRecord;
import za.co.eliteproject.model.Trader;
import za.co.eliteproject.risk.BlacklistRule;
import za.co.eliteproject.risk.MarginRequirementRule;
import za.co.eliteproject.risk.PositionLimitRule;
import za.co.eliteproject.service.AuditLog;
import za.co.eliteproject.service.CircuitBreaker;
import za.co.eliteproject.service.MarketDataFeed;
import za.co.eliteproject.service.MatchingEngine;
import za.co.eliteproject.service.RiskEngine;
import za.co.eliteproject.service.Settlement;
import za.co.eliteproject.strategy.PriceTimeMatchingStrategy;

import java.util.List;

/**
 * Small end-to-end demo: match a buy and a sell, settle the trade, and check the circuit breaker.
 */
public class Main {

    public static void main(String[] args) {
        AuditLog auditLog = new AuditLog();
        RiskEngine riskEngine = new RiskEngine();
        riskEngine.addRule(new BlacklistRule());
        riskEngine.addRule(new PositionLimitRule(1_000_000));
        riskEngine.addRule(new MarginRequirementRule(0.2));

        MatchingEngine engine = new MatchingEngine(riskEngine, new PriceTimeMatchingStrategy(),
                auditLog, new MarketDataFeed());
        Market market = new Market("ELEC-ZA", AssetType.ELECTRICITY, 150.0);
        engine.registerMarket(market);

    }
}
