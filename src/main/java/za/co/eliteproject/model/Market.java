package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

import java.util.ArrayList;
import java.util.List;

/**
 * A venue for trading a single AssetType.
 */
public class Market {

    private final String marketId;
    private final AssetType assetType;
    private double lastTradePrice;
    private final double openingPrice;
    private boolean isOpen;
    private final List<Order> orders;
    private final List<TradeRecord> tradeHistory;

    public Market(String marketId, AssetType assetType, double openingPrice) {
        if (marketId == null || marketId.isBlank()) {
            throw new IllegalArgumentException("Market ID cannot be blank");
        }
        if (assetType == null) {
            throw new IllegalArgumentException("Asset type cannot be null");
        }
        if (!(openingPrice > 0)) {
            throw new IllegalArgumentException("Opening price must be positive");
        }
        this.marketId = marketId;
        this.assetType = assetType;
        this.openingPrice = openingPrice;
        this.lastTradePrice = openingPrice;
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
        isOpen = true;
    }

    public void close() {
        isOpen = false;
    }

    /** Records an order against this market; throws IllegalStateException if the market is closed. */
    public void submitOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (!isOpen) {
            throw new IllegalStateException("Market " + marketId + " is closed");
        }
        orders.add(order);
    }

    /** Appends the trade and updates lastTradePrice. */
    public void recordTrade(TradeRecord trade) {
        if (trade == null) {
            throw new IllegalArgumentException("Trade cannot be null");
        }
        tradeHistory.add(trade);
        lastTradePrice = trade.getPrice();
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public List<TradeRecord> getTradeHistory() {
        return new ArrayList<>(tradeHistory);
    }

    public double getTotalVolume() {
        double volume = 0.0;
        for (TradeRecord trade : tradeHistory) {
            volume += trade.getQuantity();
        }
        return volume;
    }

    /** @return percentage movement of the last trade price versus the opening price. */
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
                ", orders=" + orders.size() +
                ", trades=" + tradeHistory.size() +
                '}';
    }
}
