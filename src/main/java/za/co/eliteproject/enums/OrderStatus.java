package za.co.eliteproject.enums;

public enum OrderStatus {
    PENDING, PARTAL, FILLED, CANCELLED, REJECTED;

    public boolean isTerminal() {
        switch (this) {
            case FILLED:
            case CANCELLED:
            case REJECTED:
                return true;
        }
        return false;
    }
}
