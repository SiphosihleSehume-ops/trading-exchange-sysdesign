package za.co.eliteproject.service;

import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.model.TradeRecord;
import za.co.eliteproject.observer.MarketObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages MarketObserver subscriptions and broadcasts price/status/trade events.
 */
public class MarketDataFeed {

    private final List<MarketObserver> observers;
    private final HashMap<String, Double> priceCache;

    public MarketDataFeed() {
        this.observers = new ArrayList<>();
        this.priceCache = new HashMap<>();
    }

    /**
     * Registers an observer; no duplicates.
     */
    public void subscribe(MarketObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * @return true if the observer was found and removed.
     */
    public boolean unsubscribe(MarketObserver observer) {
        return observers.remove(observer);
    }

    public void publishPriceUpdate(String marketId, AssetType assetType, double newPrice) {
        double previousPrice = priceCache.getOrDefault(marketId, -1.0);
        priceCache.put(marketId, newPrice);
        for (MarketObserver observer : observers) {
            observer.onPriceUpdate(marketId, assetType, newPrice, previousPrice);
        }
    }

    public void publishMarketStatusChange(String marketId, boolean isOpen) {
        for (MarketObserver observer : observers) {
            observer.onMarketStatusChange(marketId, isOpen);
        }
    }

    public void publishTradeExecuted(TradeRecord trade) {
        for (MarketObserver observer : observers) {
            observer.onTradeExecuted(trade);
        }
    }

    /**
     * @return the last cached price for the market, or -1 if unknown.
     */
    public double getLastPrice(String marketId) {
        return priceCache.getOrDefault(marketId, -1.0);
    }

    public int getObserverCount() {
        return observers.size();
    }
}
