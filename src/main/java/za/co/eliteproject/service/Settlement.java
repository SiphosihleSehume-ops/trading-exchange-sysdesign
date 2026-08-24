package za.co.eliteproject.service;

import za.co.eliteproject.model.Asset;
import za.co.eliteproject.model.Portfolio;
import za.co.eliteproject.model.TradeRecord;
import za.co.eliteproject.model.Trader;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes TradeRecords and updates the portfolios of the buyer and seller.
 */
public class Settlement {

    private final AuditLog auditLog;
    private final List<TradeRecord> settled;

    public Settlement(AuditLog auditLog) {
        this.auditLog = auditLog;
        this.settled = new ArrayList<>();
    }

    /**
     * Executes settlement of a trade between buyer and seller portfolios.
     *
     * @return false if any precondition fails (already settled, insufficient reserved cash,
     *         or insufficient asset holding).
     */
    public boolean settle(TradeRecord trade, Trader buyer, Trader seller) {
        if (trade == null || buyer == null || seller == null) {
            throw new IllegalArgumentException("Trade, buyer, and seller cannot be null");
        }
        if (isAlreadySettled(trade.getTradeId())) {
            return false;
        }

        double tradeValue = trade.getQuantity() * trade.getPrice();

        Portfolio buyerPortfolio = buyer.getPortfolio();
        Portfolio sellerPortfolio = seller.getPortfolio();

        if (!buyerPortfolio.deductReservedCash(tradeValue)) {
            return false;
        }

        Asset sellerAsset = sellerPortfolio.getAsset(trade.getAssetType());
        double sellerAvgCost = sellerAsset != null ? sellerAsset.getAvgCost() : 0.0;

        if (!sellerPortfolio.reduceAsset(trade.getAssetType(), trade.getQuantity())) {
            // Roll back the buyer's cash deduction since the trade cannot be completed.
            buyerPortfolio.creditCash(tradeValue);
            return false;
        }

        sellerPortfolio.creditCash(tradeValue);
        buyerPortfolio.addAsset(trade.getAssetType(), trade.getQuantity(), trade.getPrice());

        double realisedPnL = (trade.getPrice() - sellerAvgCost) * trade.getQuantity();
        sellerPortfolio.recordRealisedPnL(realisedPnL);

        settled.add(trade);

        auditLog.log("TRADE_SETTLED", "Trade " + trade.getTradeId() + " settled between buyer "
                + buyer.getUsername() + " and seller " + seller.getUsername());

        return true;
    }

    public List<TradeRecord> getSettledTrades() {
        return new ArrayList<>(settled);
    }

    public double getTotalSettledValue() {
        double total = 0.0;
        for (TradeRecord trade : settled) {
            total += trade.getQuantity() * trade.getPrice();
        }
        return total;
    }

    public boolean isAlreadySettled(int tradeId) {
        for (TradeRecord trade : settled) {
            if (trade.getTradeId() == tradeId) {
                return true;
            }
        }
        return false;
    }
}
