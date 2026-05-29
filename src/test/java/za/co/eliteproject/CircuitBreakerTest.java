package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.*;
import za.co.eliteproject.model.*;

import za.co.eliteproject.service.CircuitBreaker;
import static org.junit.jupiter.api.Assertions.*;

public class CircuitBreakerTest {

    private Market market;
    private CircuitBreaker breaker;

    @BeforeEach
    void setUp() {
        market = new Market("ELEC-ZA", AssetType.ELECTRICITY, 100.0);
        market.open();
        breaker = new CircuitBreaker(market, 10.0); // trips at ±10% movement
    }

    @Test void testInitialState() {
        assertEquals(CircuitBreakerState.CLOSED, breaker.getState());
        assertTrue(breaker.allowsTrading());
        assertEquals(0, breaker.getTripCount());
    }

    @Test void testDoesNotTripBelowThreshold() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 105.0));
        breaker.check(); // 5% movement — under threshold
        assertEquals(CircuitBreakerState.CLOSED, breaker.getState());
    }

    @Test void testTripsWhenThresholdExceeded() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 115.0));
        breaker.check(); // 15% movement — exceeds 10% threshold
        assertEquals(CircuitBreakerState.OPEN, breaker.getState());
        assertFalse(breaker.allowsTrading());
        assertEquals(1, breaker.getTripCount());
    }

    @Test void testNegativeMovementAlsoTrips() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 85.0));
        breaker.check(); // -15% movement
        assertEquals(CircuitBreakerState.OPEN, breaker.getState());
    }

    @Test void testResetMovesToTesting() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 115.0));
        breaker.check();
        breaker.reset();
        assertEquals(CircuitBreakerState.TESTING, breaker.getState());
        assertTrue(breaker.allowsTrading());
    }

    @Test void testConfirmStableMovesToClosed() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 115.0));
        breaker.check();
        breaker.reset();
        breaker.confirmStable();
        assertEquals(CircuitBreakerState.CLOSED, breaker.getState());
    }

    @Test void testResetFromClosedThrows() {
        assertThrows(IllegalStateException.class, () -> breaker.reset());
    }

    @Test void testConfirmStableFromClosedThrows() {
        assertThrows(IllegalStateException.class, () -> breaker.confirmStable());
    }

    @Test void testInvalidThresholdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreaker(market, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new CircuitBreaker(market, 101.0));
    }

    @Test void testCircuitBreakerStateAllowsTrading() {
        assertTrue(CircuitBreakerState.CLOSED.allowsTrading());
        assertFalse(CircuitBreakerState.OPEN.allowsTrading());
        assertTrue(CircuitBreakerState.TESTING.allowsTrading());
    }

    @Test void testTripCountIncrements() {
        market.recordTrade(new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 10.0, 115.0));
        breaker.check();
        breaker.reset();
        breaker.confirmStable();
        market.recordTrade(new TradeRecord(2, 3, 4, AssetType.ELECTRICITY, 10.0, 60.0));
        breaker.check();
        assertEquals(2, breaker.getTripCount());
    }
}