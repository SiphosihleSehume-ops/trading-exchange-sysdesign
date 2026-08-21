package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.enums.OrderSide;

/**
 * An order that executes immediately at the best available price.
 */
public class MarketOrder extends Order {

    public MarketOrder(int orderId, Trader trader, AssetType assetType, OrderSide side, double quantity) {
        super(orderId, trader, assetType, side, quantity);
    }

    @Override
    public String getOrderType() {
        return "MARKET";
    }

    @Override
    public boolean canMatchAt(double price) {
        return true;
    }
}
