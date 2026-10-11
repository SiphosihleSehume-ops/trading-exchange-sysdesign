package za.co.eliteproject.observer;

import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.model.TradeRecord;

/**
 * Listener for market events published by MarketDataFeed.
 */
public interface MarketObserver {

    void onPriceUpdate(String marketId, AssetType assetType, double newPrice, double previousPrice);

    void onMarketStatusChange(String marketId, boolean isOpen);

    void onTradeExecuted(TradeRecord trade);
}
