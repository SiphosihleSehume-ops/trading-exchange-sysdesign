package za.co.eliteproject.enums;

public enum OrderStatus {
    PENDING, PARTIAL, FILLED, CANCELLED, REJECTED;

    public boolean isTerminal() {
        switch (this) {
            case FILLED:
            case CANCELLED:
            case REJECTED:
                return true;
            default:
                return false;
        }
    }
}
