package za.co.eliteproject;

package za.co.wethinkcode;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.*;
import za.co.eliteproject.model.*;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    private Trader trader;

    @BeforeEach
    void setUp() {
        trader = new Trader(1, "tester", new Portfolio(1, 100000.0));
    }

    @Test void testLimitOrderCreation() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 5.0, 300.0);
        assertEquals("LIMIT", o.getOrderType());
        assertEquals(OrderStatus.PENDING, o.getStatus());
        assertEquals(0.0, o.getFilledQty(), 0.001);
        assertEquals(5.0, o.getRemainingQty(), 0.001);
    }


    @Test void testMarketOrderCreation() {
        MarketOrder o = new MarketOrder(1, trader, AssetType.COAL, OrderSide.SELL, 20.0);
        assertEquals("MARKET", o.getOrderType());
        assertTrue(o.canMatchAt(0.0));
        assertTrue(o.canMatchAt(999999.0));
    }


    @Test void testLimitBuyCanMatchAt() {
        LimitOrder buy = new LimitOrder(1, trader, AssetType.ELECTRICITY, OrderSide.BUY, 10.0, 150.0);
        assertTrue(buy.canMatchAt(150.0));
        assertTrue(buy.canMatchAt(140.0));
        assertFalse(buy.canMatchAt(151.0));
    }

    @Test void testLimitSellCanMatchAt() {
        LimitOrder sell = new LimitOrder(1, trader, AssetType.ELECTRICITY, OrderSide.SELL, 10.0, 150.0);
        assertTrue(sell.canMatchAt(150.0));
        assertTrue(sell.canMatchAt(160.0));
        assertFalse(sell.canMatchAt(149.0));
    }

    @Test void testFillUpdatesStatusToPartial() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 10.0, 200.0);
        o.fill(4.0);
        assertEquals(OrderStatus.PARTIAL, o.getStatus());
        assertEquals(4.0, o.getFilledQty(), 0.001);
        assertEquals(6.0, o.getRemainingQty(), 0.001);
    }

    @Test void testFillUpdatesStatusToFilled() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 10.0, 200.0);
        o.fill(10.0);
        assertEquals(OrderStatus.FILLED, o.getStatus());
        assertTrue(o.getStatus().isTerminal());
    }

    @Test void testFillBeyondRemainingThrows() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 5.0, 100.0);
        assertThrows(IllegalArgumentException.class, () -> o.fill(6.0));
    }

    @Test void testCancelOrder() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.COAL, OrderSide.SELL, 3.0, 80.0);
        o.cancel();
        assertEquals(OrderStatus.CANCELLED, o.getStatus());
        assertTrue(o.getStatus().isTerminal());
    }

    @Test void testCannotCancelTerminalOrder() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.COAL, OrderSide.SELL, 3.0, 80.0);
        o.fill(3.0);
        o.cancel(); // should silently do nothing
        assertEquals(OrderStatus.FILLED, o.getStatus());
    }

    @Test void testRejectOrder() {
        LimitOrder o = new LimitOrder(1, trader, AssetType.CARBON, OrderSide.BUY, 1.0, 50.0);
        o.reject();
        assertEquals(OrderStatus.REJECTED, o.getStatus());
    }

    @Test void testNegativeQuantityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, -1.0, 100.0));
    }

    @Test void testNegativeLimitPriceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new LimitOrder(1, trader, AssetType.GAS, OrderSide.BUY, 5.0, -10.0));
    }

    @Test void testOrderSideOpposite() {
        assertEquals(OrderSide.SELL, OrderSide.BUY.opposite());
        assertEquals(OrderSide.BUY, OrderSide.SELL.opposite());
    }

    @Test void testOrderStatusIsTerminal() {
        assertFalse(OrderStatus.PENDING.isTerminal());
        assertFalse(OrderStatus.PARTIAL.isTerminal());
        assertTrue(OrderStatus.FILLED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertTrue(OrderStatus.REJECTED.isTerminal());
    }

    @Test void testAssetTypeLabel() {
        assertEquals("Electricity (MWh)", AssetType.ELECTRICITY.getLabel());
        assertEquals("Carbon Credits (t)", AssetType.CARBON.getLabel());
    }
}
