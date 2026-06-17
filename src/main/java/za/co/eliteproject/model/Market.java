package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

import java.sql.Array;
import java.util.*;

public class Market {
    private String marketId;
    private AssetType assetType;
    private double lastTradePrice;
    private double openingPrice;
    private boolean isOpen;
    private List<Order> orders;
    private List<TradeRecord> tradeHistory;

    public Market(String marketId, AssetType assetType,  double openingPrice) {
        this.marketId = marketId;
        this.assetType = assetType;
        this.lastTradePrice = lastTradePrice;
        this.openingPrice = openingPrice;
        this.isOpen = true;
        this.orders = new ArrayList<>();
        this.tradeHistory = new ArrayList<>();
    }

    public String getMarketId() {
        return marketId;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public double getLastTradePrice() {
        return lastTradePrice;
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        //
    }

    public void close() {
        //
    }

    public void submitOrder(Order order) {
        if (!isOpen) throw new IllegalArgumentException();
        orders.add(order);
    }

    public void recordTrade(TradeRecord record) {
        tradeHistory.add(record);
        //Update trade;
    }
}
