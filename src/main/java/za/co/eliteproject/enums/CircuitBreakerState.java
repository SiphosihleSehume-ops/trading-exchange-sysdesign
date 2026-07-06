package za.co.eliteproject.enums;

public enum CircuitBreakerState {
    CLOSED, OPEN, TESTING;

    public boolean allowsTrading() {
        switch (this) {
            // Chain similar logic
            case CLOSED:
            case TESTING:
                return true;
            case OPEN:
                return false;
        }
        return false;
    }
}
