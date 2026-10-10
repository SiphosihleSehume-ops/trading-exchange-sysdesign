package za.co.eliteproject.enums;

public enum OrderStatus {
    PENDING, PARTIAL, FILLED, CANCELLED, REJECTED;

    /** @return true if no further state changes are possible. */
    public boolean isTerminal() {
        return this == FILLED || this == CANCELLED || this == REJECTED;
    }
}
