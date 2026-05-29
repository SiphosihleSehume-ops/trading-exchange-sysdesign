package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.*;
import za.co.eliteproject.model.*;
import static org.junit.jupiter.api.Assertions.*;

public class TraderTest {

    private Portfolio portfolio;
    private Trader trader;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio(1, 50000.0);
        trader = new Trader(1, "energy_trader_01", portfolio);
    }

    @Test void testTraderCreation() {
        assertEquals(1, trader.getTraderId());
        assertEquals("energy_trader_01", trader.getUsername());
        assertSame(portfolio, trader.getPortfolio());
        assertFalse(trader.isBlacklisted());
    }

    @Test void testBlankUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Trader(2, "  ", portfolio));
    }

    @Test void testNullPortfolioThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Trader(2, "user", null));
    }

    @Test void testBlacklist() {
        trader.blacklist();
        assertTrue(trader.isBlacklisted());
    }

    @Test void testAddAndRemoveOpenOrder() {
        LimitOrder order = new LimitOrder(1, trader, AssetType.ELECTRICITY,
                OrderSide.BUY, 10.0, 150.0);
        trader.addOpenOrder(order);
        assertEquals(1, trader.getOpenOrders().size());
        assertTrue(trader.removeOpenOrder(1));
        assertEquals(0, trader.getOpenOrders().size());
    }

    @Test void testGetOpenOrdersIsDefensiveCopy() {
        LimitOrder order = new LimitOrder(1, trader, AssetType.ELECTRICITY,
                OrderSide.BUY, 10.0, 150.0);
        trader.addOpenOrder(order);
        trader.getOpenOrders().clear();
        assertEquals(1, trader.getOpenOrders().size());
    }

    @Test void testGetTotalExposureOnlyBuyLimitOrders() {
        LimitOrder buy = new LimitOrder(1, trader, AssetType.ELECTRICITY,
                OrderSide.BUY, 10.0, 200.0);
        LimitOrder sell = new LimitOrder(2, trader, AssetType.ELECTRICITY,
                OrderSide.SELL, 5.0, 210.0);
        MarketOrder mktBuy = new MarketOrder(3, trader, AssetType.GAS, OrderSide.BUY, 3.0);
        trader.addOpenOrder(buy);
        trader.addOpenOrder(sell);
        trader.addOpenOrder(mktBuy);
        assertEquals(2000.0, trader.getTotalExposure(), 0.01); // only buy LimitOrder: 10 * 200
    }

    @Test void testRemoveNonExistentOrderReturnsFalse() {
        assertFalse(trader.removeOpenOrder(999));
    }
}