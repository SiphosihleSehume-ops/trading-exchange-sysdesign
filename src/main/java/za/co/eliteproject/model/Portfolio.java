package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

import java.util.HashMap;
import java.util.Map;

/**
 * Cash and asset positions belonging to a Trader.
 */
public class Portfolio {

    private final int portfolioId;
    private double cashBalance;
    private double reservedCash;
    private final HashMap<AssetType, Asset> assets;
    private double realisedPnL;

    public Portfolio(int portfolioId, double initialCash) {
        if (initialCash < 0) {
            throw new IllegalArgumentException("Initial cash cannot be negative");
        }
        this.portfolioId = portfolioId;
        this.cashBalance = initialCash;
        this.reservedCash = 0.0;
        this.assets = new HashMap<>();
        this.realisedPnL = 0.0;
    }

    public int getPortfolioId() {
        return portfolioId;
    }

    /** @return available cash (excludes reserved). */
    public double getCashBalance() {
        return cashBalance;
    }

    public double getReservedCash() {
        return reservedCash;
    }

    public double getTotalCash() {
        return cashBalance + reservedCash;
    }

    /** Moves cash from available to reserved; false if insufficient. */
    public boolean reserveCash(double amount) {
        requirePositive(amount);
        if (amount > cashBalance) {
            return false;
        }
        cashBalance -= amount;
        reservedCash += amount;
        return true;
    }

    /** Moves cash from reserved back to available (never more than is reserved). */
    public void releaseCash(double amount) {
        requirePositive(amount);
        double released = Math.min(amount, reservedCash);
        reservedCash -= released;
        cashBalance += released;
    }

    /** Permanently removes cash from reserved (used at settlement); false if insufficient. */
    public boolean deductReservedCash(double amount) {
        requirePositive(amount);
        if (amount > reservedCash) {
            return false;
        }
        reservedCash -= amount;
        return true;
    }

    public void creditCash(double amount) {
        requirePositive(amount);
        cashBalance += amount;
    }

    /** Adds to (or creates) the holding for the asset type, using VWAP for the average cost. */
    public void addAsset(AssetType type, double qty, double price) {
        Asset existing = assets.get(type);
        if (existing == null) {
            Asset created = new Asset(type, 0.0, 0.0);
            created.add(qty, price);
            assets.put(type, created);
        } else {
            existing.add(qty, price);
        }
    }

    /** @return false if the asset is not held or the holding is insufficient. */
    public boolean reduceAsset(AssetType type, double qty) {
        Asset existing = assets.get(type);
        return existing != null && existing.reduce(qty);
    }

    /** @return the holding, or null if not held. */
    public Asset getAsset(AssetType type) {
        return assets.get(type);
    }

    /** @return a defensive (deep) copy of all holdings. */
    public Map<AssetType, Asset> getAllAssets() {
        Map<AssetType, Asset> copy = new HashMap<>();
        for (Asset asset : assets.values()) {
            copy.put(asset.getAssetType(),
                    new Asset(asset.getAssetType(), asset.getQuantity(), asset.getAvgCost()));
        }
        return copy;
    }

    public double getRealisedPnL() {
        return realisedPnL;
    }

    public void recordRealisedPnL(double amount) {
        realisedPnL += amount;
    }

    /** @return total cash plus the market value of every asset that has a price in the map. */
    public double getTotalValue(Map<AssetType, Double> prices) {
        double total = getTotalCash();
        for (Asset asset : assets.values()) {
            Double price = prices.get(asset.getAssetType());
            if (price != null) {
                total += asset.getMarketValue(price);
            }
        }
        return total;
    }

    private static void requirePositive(double amount) {
        if (!(amount > 0)) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "portfolioId=" + portfolioId +
                ", cashBalance=" + cashBalance +
                ", reservedCash=" + reservedCash +
                ", assets=" + assets +
                ", realisedPnL=" + realisedPnL +
                '}';
    }
}
