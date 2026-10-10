package za.co.eliteproject.enums;

public enum CircuitBreakerState {
    CLOSED, OPEN, TESTING;

    /** @return true for CLOSED and TESTING, false for OPEN. */
    public boolean allowsTrading() {
        return this != OPEN;
    }
}
