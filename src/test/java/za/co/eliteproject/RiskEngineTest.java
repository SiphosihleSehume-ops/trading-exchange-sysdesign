package za.co.wethinkcode;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.*;
import za.co.eliteproject.model.*;
import za.co.eliteproject.risk.*;
import za.co.eliteproject.service.RiskEngine;
import static org.junit.jupiter.api.Assertions.*;

public class RiskEngineTest {

    private RiskEngine engine;
    private Market market;
    private Trader trader;

    @BeforeEach
    void setUp() {
        engine = new RiskEngine();
        market = new Market("GAS-ZA", AssetType.GAS, 300.0);
        market.open();
        trader = new Trader(1, "risk_tester", new Portfolio(1, 50000.0));
    }

    @Test void testEmptyRuleChainPassesAll() {
        LimitOrder order = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 10.0, 300.0);
        assertTrue(engine.evaluate(order, market));
    }

    @Test void testBlacklistRuleRejectsBlacklistedTrader() {
        engine.addRule(new BlacklistRule());
        trader.blacklist();
        LimitOrder order = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 1.0, 300.0);
        assertFalse(engine.evaluate(order, market));
    }

    @Test void testBlacklistRulePassesNonBlacklisted() {
        engine.addRule(new BlacklistRule());
        LimitOrder order = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 1.0, 300.0);
        assertTrue(engine.evaluate(order, market));
    }

    @Test void testPositionLimitRuleRejectsExcess() {
        engine.addRule(new PositionLimitRule(10000.0));
        // 10 * 300 = 3000; within limit — passes
        LimitOrder order1 = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 10.0, 300.0);
        assertTrue(engine.evaluate(order1, market));
        trader.addOpenOrder(order1);

        // now exposure = 3000, adding another 30*300=9000 → total 12000 > 10000 — fails
        LimitOrder order2 = new LimitOrder(2, trader, AssetType.GAS, OrderSide.BUY, 30.0, 300.0);
        assertFalse(engine.evaluate(order2, market));
    }

    @Test void testMarginRequirementRuleRejectsUnderfunded() {
        engine.addRule(new MarginRequirementRule(0.5)); // 50% margin
        // 100 * 300 * 0.5 = 15000 required; portfolio has 50000 — passes
        LimitOrder ok = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 100.0, 300.0);
        assertTrue(engine.evaluate(ok, market));

        // 1000 * 300 * 0.5 = 150000 required; portfolio only has 50000 — fails
        LimitOrder big = new LimitOrder(2, trader, AssetType.GAS, OrderSide.BUY, 1000.0, 300.0);
        assertFalse(engine.evaluate(big, market));
    }

    @Test void testSellOrderPassesMarginRule() {
        engine.addRule(new MarginRequirementRule(0.9));
        LimitOrder sell = new LimitOrder(1, trader, AssetType.GAS, OrderSide.SELL, 100.0, 300.0);
        assertTrue(engine.evaluate(sell, market));
    }

    @Test void testChainStopsAtFirstFailure() {
        engine.addRule(new BlacklistRule());
        engine.addRule(new PositionLimitRule(100.0));
        trader.blacklist();
        LimitOrder order = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 1.0, 300.0);
        RiskRule failing = engine.getFailingRule(order, market);
        assertNotNull(failing);
        assertEquals("BlacklistRule", failing.getRuleName());
    }

    @Test void testGetRulesIsDefensiveCopy() {
        engine.addRule(new BlacklistRule());
        engine.getRules().clear();
        assertEquals(1, engine.getRules().size());
    }
}