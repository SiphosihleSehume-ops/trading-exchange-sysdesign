package za.co.eliteproject.enums;

public enum OrderSide {
    BUY, SELL;

    public OrderSide opposite() {
<<<<<<< HEAD
        return this == SELL ? BUY : SELL;
=======
        return this == BUY ? SELL : BUY;
>>>>>>> main
    }
}
