package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.model.TradeRecord;
import za.co.eliteproject.observer.MarketObserver;
import za.co.eliteproject.service.MarketDataFeed;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MarketDataFeedTest {

    static class TestObserver implements MarketObserver {
        List<String> priceEvents = new ArrayList<>();
        List<String> statusEvents = new ArrayList<>();
        List<TradeRecord> tradeEvents = new ArrayList<>();

        @Override
        public void onPriceUpdate(String marketId, AssetType assetType,
                                  double newPrice, double previousPrice) {
            priceEvents.add(marketId + ":" + newPrice);
        }

        @Override
        public void onMarketStatusChange(String marketId, boolean isOpen) {
            statusEvents.add(marketId + ":" + isOpen);
        }

        @Override
        public void onTradeExecuted(TradeRecord trade) {
            tradeEvents.add(trade);
        }
    }

    private MarketDataFeed feed;
    private TestObserver obs1, obs2;

    @BeforeEach
    void setUp() {
        feed = new MarketDataFeed();
        obs1 = new TestObserver();
        obs2 = new TestObserver();
    }

    @Test void testSubscribeAndReceivePriceUpdate() {
        feed.subscribe(obs1);
        feed.publishPriceUpdate("ELEC-ZA", AssetType.ELECTRICITY, 160.0);
        assertEquals(1, obs1.priceEvents.size());
        assertEquals("ELEC-ZA:160.0", obs1.priceEvents.get(0));
    }

    @Test void testMultipleObserversReceiveUpdate() {
        feed.subscribe(obs1);
        feed.subscribe(obs2);
        feed.publishPriceUpdate("GAS-ZA", AssetType.GAS, 310.0);
        assertEquals(1, obs1.priceEvents.size());
        assertEquals(1, obs2.priceEvents.size());
    }

    @Test void testUnsubscribe() {
        feed.subscribe(obs1);
        feed.unsubscribe(obs1);
        feed.publishPriceUpdate("ELEC-ZA", AssetType.ELECTRICITY, 155.0);
        assertEquals(0, obs1.priceEvents.size());
    }

    @Test void testDuplicateSubscribeIgnored() {
        feed.subscribe(obs1);
        feed.subscribe(obs1);
        assertEquals(1, feed.getObserverCount());
    }

    @Test void testPriceCacheUpdated() {
        feed.subscribe(obs1);
        feed.publishPriceUpdate("COAL-ZA", AssetType.COAL, 90.0);
        assertEquals(90.0, feed.getLastPrice("COAL-ZA"), 0.01);
    }

    @Test void testUnknownMarketPriceReturnsMinusOne() {
        assertEquals(-1.0, feed.getLastPrice("UNKNOWN"), 0.01);
    }

    @Test void testMarketStatusChangeNotified() {
        feed.subscribe(obs1);
        feed.publishMarketStatusChange("ELEC-ZA", false);
        assertEquals("ELEC-ZA:false", obs1.statusEvents.get(0));
    }

    @Test void testTradeExecutedNotified() {
        feed.subscribe(obs1);
        TradeRecord t = new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 5.0, 155.0);
        feed.publishTradeExecuted(t);
        assertEquals(1, obs1.tradeEvents.size());
        assertSame(t, obs1.tradeEvents.get(0));
    }
}