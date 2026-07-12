package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

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

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public List<TradeRecord> getTradeHistory() {
        return new ArrayList<>(tradeHistory);
    }

    public double getTotalVolume() {
        //Sum of quantities across all `TradeRecord`s
        return 0.0;
    }

    public double getPriceMovement() {
        return ((lastTradePrice - openingPrice) / openingPrice) * 100;
    }

    @Override
    public String toString() {
        return "Market{" +
                "marketId='" + marketId + '\'' +
                ", assetType=" + assetType +
                ", lastTradePrice=" + lastTradePrice +
                ", openingPrice=" + openingPrice +
                ", isOpen=" + isOpen +
                ", orders=" + orders +
                ", tradeHistory=" + tradeHistory +
                '}';
    }
}
