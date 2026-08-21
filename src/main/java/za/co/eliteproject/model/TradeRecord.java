package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

/**
 * Immutable record of a single matched trade between a buy and sell order.
 */
public final class TradeRecord {

    private final int tradeId;
    private final int buyOrderId;
    private final int sellOrderId;
    private final AssetType assetType;
    private final double quantity;
    private final double price;
    private final long timestamp;

    public TradeRecord(int tradeId, int buyOrderId, int sellOrderId, AssetType assetType,
                        double quantity, double price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (buyOrderId == sellOrderId) {
            throw new IllegalArgumentException("Buy and sell order IDs cannot be equal");
        }
        if (assetType == null) {
            throw new IllegalArgumentException("Asset type cannot be null");
        }
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.assetType = assetType;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = System.currentTimeMillis();
    }

    public int getTradeId() {
        return tradeId;
    }

    public int getBuyOrderId() {
        return buyOrderId;
    }

    public int getSellOrderId() {
        return sellOrderId;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TradeRecord{" +
                "tradeId=" + tradeId +
                ", buyOrderId=" + buyOrderId +
                ", sellOrderId=" + sellOrderId +
                ", assetType=" + assetType +
                ", quantity=" + quantity +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}

