package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

/**
 * A tradeable commodity holding with a volume-weighted average cost.
 */
public class Asset {

    private final AssetType assetType;
    private double quantity;
    private double avgCost;

    public Asset(AssetType assetType, double quantity, double avgCost) {
        if (assetType == null) {
            throw new IllegalArgumentException("Asset type cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (avgCost < 0) {
            throw new IllegalArgumentException("Average cost cannot be negative");
        }
        this.assetType = assetType;
        this.quantity = quantity;
        this.avgCost = avgCost;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAvgCost() {
        return avgCost;
    }

    /** Increases the holding and recalculates avgCost using VWAP. */
    public void add(double addedQuantity, double addedPrice) {
        if (addedQuantity <= 0 || addedPrice <= 0) {
            throw new IllegalArgumentException("Quantity and price must be positive");
        }
        double newQuantity = quantity + addedQuantity;
        avgCost = ((quantity * avgCost) + (addedQuantity * addedPrice)) / newQuantity;
        quantity = newQuantity;
    }

    /** @return false (and leaves the holding unchanged) if quantity would go below zero. */
    public boolean reduce(double reducedQuantity) {
        if (reducedQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (reducedQuantity > quantity) {
            return false;
        }
        quantity -= reducedQuantity;
        return true;
    }

    public double getMarketValue(double currentPrice) {
        return quantity * currentPrice;
    }

    public double getUnrealisedPnL(double currentPrice) {
        return (currentPrice - avgCost) * quantity;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetType=" + assetType +
                ", quantity=" + quantity +
                ", avgCost=" + avgCost +
                '}';
    }
}
