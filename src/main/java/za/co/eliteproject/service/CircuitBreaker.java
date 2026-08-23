package za.co.eliteproject.service;

import za.co.eliteproject.enums.CircuitBreakerState;
import za.co.eliteproject.model.Market;

/**
 * Monitors a market's price movement and halts trading if it exceeds a threshold.
 */
public class CircuitBreaker {

    private final Market market;
    private final double threshold;
    private CircuitBreakerState state;
    private int tripCount;

    public CircuitBreaker(Market market, double threshold) {
        if (market == null) {
            throw new IllegalArgumentException("Market cannot be null");
        }
        if (threshold < 1 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be between 1 and 100");
        }
        this.market = market;
        this.threshold = threshold;
        this.state = CircuitBreakerState.CLOSED;
        this.tripCount = 0;
    }

    public CircuitBreakerState getState() {
        return state;
    }

    public int getTripCount() {
        return tripCount;
    }

    public double getThreshold() {
        return threshold;
    }

    /**
     * Evaluates market.getPriceMovement(); trips to OPEN if abs(movement) >= threshold.
     */
    public void check() {
        double movement = market.getPriceMovement();
        if (Math.abs(movement) >= threshold && state != CircuitBreakerState.OPEN) {
            state = CircuitBreakerState.OPEN;
            tripCount++;
        }
    }

    /**
     * Moves from OPEN to TESTING.
     */
    public void reset() {
        if (state != CircuitBreakerState.OPEN) {
            throw new IllegalStateException("reset() is only valid from OPEN state");
        }
        state = CircuitBreakerState.TESTING;
    }

    /**
     * Moves from TESTING to CLOSED.
     */
    public void confirmStable() {
        if (state != CircuitBreakerState.TESTING) {
            throw new IllegalStateException("confirmStable() is only valid from TESTING state");
        }
        state = CircuitBreakerState.CLOSED;
    }

    public boolean allowsTrading() {
        return state.allowsTrading();
    }

    @Override
    public String toString() {
        return "CircuitBreaker{" +
                "market=" + market.getMarketId() +
                ", threshold=" + threshold +
                ", state=" + state +
                ", tripCount=" + tripCount +
                '}';
    }
}

