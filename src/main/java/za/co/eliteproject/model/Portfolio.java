package za.co.eliteproject.model;

import java.util.*;

public class Portfolio {
    private int portfolioId;
    private double cashBalance;
    private double reversedCash;
    private HashMap<AssetType, Asset> assets;
    private double realisedPnL;

    public Portfolio(int portfolioId, double initialCash) {
        this.porfolioId = portfolioId;
        this.initialCash = initialCash;
    }

    public int getPortfolioId() {
        return porfolioId;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public double getReservedCash() {
        return reservedCash;
    }

    public double getTotalCash() {
        return cashBalance + reservedCash;
    }

    public boolean reservedCash(double amount) {
        cashBalance -= amount;
        reservedCash += amount;
        
        if (cashBalance <= 0) {
            return false;
        }

        return true;
    }

    public void releaseCash(double amount) {
        reservedCash -= amount;
        cashBalance += amount;
    }

    public boolean deductReservedCash(double amount) {
        reservedCash -= amount;

        if (reservedCash <= 0) {
            return false;
        }

        return true;
    }

    public Asset getAset(AssetType asset) {
        Asset asset = assets.getOrDefault(asset, null);
        return asset;
    }

    public Map<AssetType, Asset> getAllAssets() {
        return new ArrayList<>(assets);
    }

    public double getRealizedPnL(double amount) {
        return realisedPnL;
    }

    public void recordRealizedPnL(double amount) {
        realisedPnL += amount;
    }

    public double getTotalValue(Map<AssetType, Double> prices) {
        Double asset = null;

        for (Map.Entry<AssetType, Double> entry : prices.entrySet()) {
            Double asset = entry.getValue();
        }
        return asset;
    }

    @Override
    public String toString() {
        ///
    }
}
