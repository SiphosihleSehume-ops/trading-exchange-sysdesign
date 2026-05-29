package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.*;
import za.co.eliteproject.model.*;
import static org.junit.jupiter.api.Assertions.*;

public class MarketTest {

    private Market market;
    private Trader trader;

    @BeforeEach
    void setUp() {
        market = new Market("ELEC-ZA", AssetType.ELECTRICITY, 150.0);
        trader = new Trader(1, "trader1", new Portfolio(1, 50000.0));
        market.open();
    }

    @Test void testMarketCreation() {
        assertEquals("ELEC-ZA", market.getMarketId());
        assertEquals(AssetType.ELECTRICITY, market.getAssetType());
        assertEquals(150.0, market.getOpeningPrice(), 0.01);
        assertEquals(150.0, market.getLastTradePrice(), 0.01);
        assertTrue(market.isOpen());
    }

    @Test void testSubmitOrderToClosedMarketThrows() {
        market.close();
        LimitOrder order = new LimitOrder(1, trader, AssetType.ELECTRICITY, OrderSide.BUY, 10.0, 155.0);
        assertThrows(IllegalStateException.class, () -> market.submitOrder(order));
    }

    @Test void testRecordTradeUpdatesLastPrice() {
        TradeRecord trade = new TradeRecord(1, 10, 20, AssetType.ELECTRICITY, 5.0, 160.0);
        market.recordTrade(trade);
        assertEquals(160.0, market.getLastTradePrice(), 0.01);
    }

    @Test void testGetTotalVolume() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 155.0));
        market.recordTrade(new TradeRecord(2, 3, 4, AssetType.ELECTRICITY, 5.0, 160.0));
        assertEquals(15.0, market.getTotalVolume(), 0.01);
    }

    @Test void testGetPriceMovement() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 165.0));
        // ((165 - 150) / 150) * 100 = 10%
        assertEquals(10.0, market.getPriceMovement(), 0.01);
    }

    @Test void testGetOrdersIsDefensiveCopy() {
        LimitOrder order = new LimitOrder(1, trader, AssetType.ELECTRICITY, OrderSide.BUY, 5.0, 150.0);
        market.submitOrder(order);
        market.getOrders().clear();
        assertEquals(1, market.getOrders().size());
    }

    @Test void testGetTradeHistoryIsDefensiveCopy() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 5.0, 155.0));
        market.getTradeHistory().clear();
        assertEquals(1, market.getTradeHistory().size());
    }

    @Test void testBlankMarketIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Market("", AssetType.GAS, 100.0));
    }

    @Test void testNegativeOpeningPriceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Market("GAS-ZA", AssetType.GAS, -10.0));
    }

    @Test void testTradeRecordImmutable() {
        TradeRecord t = new TradeRecord(1, 2, 3, AssetType.COAL, 10.0, 90.0);
        assertEquals(1, t.getTradeId());
        assertEquals(10.0, t.getQuantity(), 0.01);
        assertEquals(90.0, t.getPrice(), 0.01);
        // No setters — confirmed by compile-time absence
    }

    @Test void testSameBuySellIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new TradeRecord(1, 5, 5, AssetType.COAL, 10.0, 90.0));
    }
}