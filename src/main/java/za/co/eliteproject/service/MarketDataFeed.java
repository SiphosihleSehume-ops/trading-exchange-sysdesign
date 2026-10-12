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

    private static final double UNKNOWN_PRICE = -1.0;

    private final List<MarketObserver> observers = new ArrayList<>();
    private final HashMap<String, Double> priceCache = new HashMap<>();

    /** Registers an observer; duplicates are ignored. */
    public void subscribe(MarketObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** @return true if the observer was found and removed. */
    public boolean unsubscribe(MarketObserver observer) {
        return observers.remove(observer);
    }

    public void publishPriceUpdate(String marketId, AssetType assetType, double newPrice) {
        double previousPrice = getLastPrice(marketId);
        priceCache.put(marketId, newPrice);
        for (MarketObserver observer : snapshot()) {
            observer.onPriceUpdate(marketId, assetType, newPrice, previousPrice);
        }
    }

    public void publishMarketStatusChange(String marketId, boolean isOpen) {
        for (MarketObserver observer : snapshot()) {
            observer.onMarketStatusChange(marketId, isOpen);
        }
    }

    public void publishTradeExecuted(TradeRecord trade) {
        for (MarketObserver observer : snapshot()) {
            observer.onTradeExecuted(trade);
        }
    }

    /** @return the last cached price for the market, or -1 if unknown. */
    public double getLastPrice(String marketId) {
        return priceCache.getOrDefault(marketId, UNKNOWN_PRICE);
    }

    public int getObserverCount() {
        return observers.size();
    }

    /** Copy so an observer may (un)subscribe from inside a callback without breaking iteration. */
    private List<MarketObserver> snapshot() {
        return new ArrayList<>(observers);
    }
}
