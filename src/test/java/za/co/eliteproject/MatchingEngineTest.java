package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.*;
import za.co.eliteproject.model.*;
import za.co.eliteproject.service.*;
import za.co.eliteproject.strategy.*;
import za.co.eliteproject.risk.*;
import za.co.eliteproject.observer.MarketObserver;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MatchingEngineTest {

    private MatchingEngine engine;
    private Market market;
    private Trader buyer, seller;
    private AuditLog auditLog;
    private MarketDataFeed dataFeed;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        dataFeed = new MarketDataFeed();
        RiskEngine risk = new RiskEngine();
        engine = new MatchingEngine(risk, new PriceTimeMatchingStrategy(), auditLog, dataFeed);

        market = new Market("ELEC-ZA", AssetType.ELECTRICITY, 150.0);
        market.open();
        engine.registerMarket(market);

        buyer  = new Trader(1, "buyer",  new Portfolio(1, 500000.0));
        seller = new Trader(2, "seller", new Portfolio(2, 100000.0));
        seller.getPortfolio().addAsset(AssetType.ELECTRICITY, 1000.0, 120.0);
    }

    @Test void testSubmitOrderGeneratesTrade() {
        LimitOrder buyOrder  = new LimitOrder(1, buyer,  AssetType.ELECTRICITY, OrderSide.BUY,  10.0, 155.0);
        LimitOrder sellOrder = new LimitOrder(2, seller, AssetType.ELECTRICITY, OrderSide.SELL, 10.0, 150.0);

        engine.submitOrder(sellOrder);
        List<TradeRecord> trades = engine.submitOrder(buyOrder);

        assertEquals(1, trades.size());
        assertEquals(10.0, trades.get(0).getQuantity(), 0.01);
    }

    @Test void testNoMatchWhenPricesDontCross() {
        LimitOrder buy  = new LimitOrder(1, buyer,  AssetType.ELECTRICITY, OrderSide.BUY,  10.0, 140.0);
        LimitOrder sell = new LimitOrder(2, seller, AssetType.ELECTRICITY, OrderSide.SELL, 10.0, 160.0);

        engine.submitOrder(buy);
        List<TradeRecord> trades = engine.submitOrder(sell);
        assertEquals(0, trades.size());
    }

    @Test void testPartialFill() {
        LimitOrder buy  = new LimitOrder(1, buyer,  AssetType.ELECTRICITY, OrderSide.BUY,  15.0, 155.0);
        LimitOrder sell = new LimitOrder(2, seller, AssetType.ELECTRICITY, OrderSide.SELL,  8.0, 150.0);

        engine.submitOrder(sell);
        List<TradeRecord> trades = engine.submitOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(8.0, trades.get(0).getQuantity(), 0.01);
        assertEquals(OrderStatus.FILLED,  sell.getStatus());
        assertEquals(OrderStatus.PARTIAL, buy.getStatus());
    }

    @Test void testRejectedByRiskEngineReturnsEmptyList() {
        RiskEngine strictRisk = new RiskEngine();
        strictRisk.addRule(new BlacklistRule());
        MatchingEngine strictEngine = new MatchingEngine(strictRisk,
                new PriceTimeMatchingStrategy(), auditLog, dataFeed);
        strictEngine.registerMarket(market);

        buyer.blacklist();
        LimitOrder order = new LimitOrder(1, buyer, AssetType.ELECTRICITY, OrderSide.BUY, 5.0, 155.0);
        List<TradeRecord> result = strictEngine.submitOrder(order);

        assertTrue(result.isEmpty());
        assertEquals(OrderStatus.REJECTED, order.getStatus());
    }

    @Test void testCancelOrder() {
        LimitOrder buy = new LimitOrder(1, buyer, AssetType.ELECTRICITY, OrderSide.BUY, 10.0, 140.0);
        engine.submitOrder(buy);
        assertTrue(engine.cancelOrder(1, "ELEC-ZA"));
        assertEquals(OrderStatus.CANCELLED, buy.getStatus());
    }

    @Test void testCancelAlreadyFilledOrderReturnsFalse() {
        LimitOrder buy  = new LimitOrder(1, buyer,  AssetType.ELECTRICITY, OrderSide.BUY,  5.0, 160.0);
        LimitOrder sell = new LimitOrder(2, seller, AssetType.ELECTRICITY, OrderSide.SELL, 5.0, 150.0);
        engine.submitOrder(sell);
        engine.submitOrder(buy);
        assertFalse(engine.cancelOrder(1, "ELEC-ZA"));
    }

    @Test void testAuditLogRecordsEvents() {
        LimitOrder buy  = new LimitOrder(1, buyer,  AssetType.ELECTRICITY, OrderSide.BUY,  5.0, 155.0);
        LimitOrder sell = new LimitOrder(2, seller, AssetType.ELECTRICITY, OrderSide.SELL, 5.0, 150.0);
        engine.submitOrder(sell);
        engine.submitOrder(buy);

        assertFalse(auditLog.getEntriesByType("ORDER_SUBMITTED").isEmpty());
        assertFalse(auditLog.getEntriesByType("TRADE_EXECUTED").isEmpty());
    }

    @Test void testGetOrderBook() {
        LimitOrder b1 = new LimitOrder(1, buyer, AssetType.ELECTRICITY, OrderSide.BUY, 5.0, 150.0);
        LimitOrder b2 = new LimitOrder(2, buyer, AssetType.ELECTRICITY, OrderSide.BUY, 5.0, 145.0);
        engine.submitOrder(b1);
        engine.submitOrder(b2);

        List<LimitOrder> book = engine.getOrderBook("ELEC-ZA", OrderSide.BUY);
        assertEquals(2, book.size());
        // Higher price should be first
        assertEquals(150.0, book.get(0).getLimitPrice(), 0.01);
    }

    @Test void testSubmitToUnregisteredMarketThrows() {
        LimitOrder order = new LimitOrder(1, buyer, AssetType.GAS, OrderSide.BUY, 5.0, 100.0);
        assertThrows(IllegalArgumentException.class, () -> engine.submitOrder(order));
    }

    @Test void testSwapMatchingStrategy() {
        engine.setMatchingStrategy(new ProRataMatchingStrategy());
        // Should not throw; strategy is swapped
        assertNotNull(engine);
    }
}