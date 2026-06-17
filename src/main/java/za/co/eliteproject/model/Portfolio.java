package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;

import java.util.*;
import java.util.concurrent.RecursiveTask;

public class Portfolio {
    private int portfolioId;
    private double cashBalance;
    private double reservedCash;
    private HashMap<AssetType, Asset> assets;
    private double realisedPnL;

    public Portfolio(int portfolioId, double initialCash) {
        this.portfolioId = portfolioId;
        this.cashBalance = initialCash;
        this.assets = new HashMap<>();
    }

    public int getPortfolioId() {
        return portfolioId;
    }

    public double getCashBalance() {
        return reservedCash;
    }

    public double getTotalCash() {
        return cashBalance + reservedCash;
    }

    public boolean reserveCash(double amount) {
        if (cashBalance > 0) {
            cashBalance -= amount;
            reservedCash += amount;
            return true;
        }
        return false;
    }

    public boolean deductReservedCash(double amount) {
        if (reservedCash > 0) {
            reservedCash -= amount;
            return true;
        }
        return false;
    }

    public void creditCash(double amount) {
        cashBalance += amount;
    }

    public void addAsset(AssetType type, double qty, double price) {
        if (assets.containsKey(type)) {
            assets.get(type).add(qty, price); //By getting the type you retrieve the value
        }
        else {
              assets.put(type, new Asset(type, qty, price));
        }
    }

    public boolean reduceAsset(AssetType type, double qty) {
        if (assets.containsKey(type)) {
            assets.get(type).reduce(qty);
            return true;
        }
        return false;
     }

    public Asset getAsset(AssetType type) {
        return assets.get(type);
    }

    public Map<AssetType, Asset> getAllAssets() {
        return new HashMap<>(assets);
    }

    public double getRealizedPnL() {
        return realisedPnL;
    }

    public void recordRealizedPnL(double amount) {
        realisedPnL += amount;
    }

    public double getTotalValue(Map<AssetType,Double> prices) {
        double totalPrice = getTotalCash();

        for (Map.Entry<AssetType, Double> entry : prices.entrySet()) {
            AssetType type = entry.getKey();
            double price = entry.getValue();

//            if (prices != null && prices)
        }
        return 0.0;
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