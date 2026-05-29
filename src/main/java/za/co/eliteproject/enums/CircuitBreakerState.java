package za.co.eliteproject.enums;

public enum CircuitBreakerState {
    PENDING, PARTIAL, FILLED, CANCELLED, REJECTED;

    public boolean isTerminal() {
        switch (this) {
            case FILLED, CANCELLED, REJECTED -> true;
            default false;
        }
    }
}
