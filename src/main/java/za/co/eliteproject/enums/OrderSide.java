package za.co.eliteproject.enums;

public enum OrderSide {
    BUY, SELL;

    public OrderSide opposite() {
        return this == SELL ? BUY : SELL;
    }
}
