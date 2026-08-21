package za.co.eliteproject.model;

import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.enums.OrderStatus;

public class LimitOrder extends Order {
    private double limitPrice;

    public LimitOrder(int oderId, Trader trader, AssetType assetType, OrderSide orderside side, double quantity, double limitPrice) {
        super(orderId, trader, assetType, orderside, quantity, limitPrice);
        this.limitPrice = limitPrice;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    public String getOrderType() {
        return "LIMIT";
    }

    public boolean canCatchAt(double price) {
        if (price <= limitPrice) {
            OrderSide.BUY; // Change later on
        } else if (price >= limitPrice) {
            OrderSide.SELL; // Change later on
        }
    }

    
}
