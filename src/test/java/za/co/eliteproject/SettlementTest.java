package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.AssetType;

import static org.junit.jupiter.api.Assertions.*;

public class SettlementTest {

    private Settlement settlement;
    private AuditLog auditLog;
    private Trader buyer, seller;
    private TradeRecord trade;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        settlement = new Settlement(auditLog);

        buyer  = new Trader(1, "buyer",  new Portfolio(1, 100000.0));
        seller = new Trader(2, "seller", new Portfolio(2, 50000.0));

        // Buyer reserves cash for the trade
        buyer.getPortfolio().reserveCash(15000.0); // 100 units * 150.0

        // Seller holds the asset
        seller.getPortfolio().addAsset(AssetType.ELECTRICITY, 200.0, 120.0);

        trade = new TradeRecord(1, 1, 2, AssetType.ELECTRICITY, 100.0, 150.0);
    }

    @Test void testSuccessfulSettlement() {
        assertTrue(settlement.settle(trade, buyer, seller));
        assertEquals(1, settlement.getSettledTrades().size());
    }

    @Test void testBuyerReceivesAsset() {
        settlement.settle(trade, buyer, seller);
        Asset asset = buyer.getPortfolio().getAsset(AssetType.ELECTRICITY);
        assertNotNull(asset);
        assertEquals(100.0, asset.getQuantity(), 0.01);
        assertEquals(150.0, asset.getAvgCost(), 0.01);
    }

    @Test void testSellerReceivesCash() {
        double before = seller.getPortfolio().getCashBalance();
        settlement.settle(trade, buyer, seller);
        assertEquals(before + 15000.0, seller.getPortfolio().getCashBalance(), 0.01);
    }

    @Test void testSellerAssetReduced() {
        settlement.settle(trade, buyer, seller);
        assertEquals(100.0, seller.getPortfolio().getAsset(AssetType.ELECTRICITY).getQuantity(), 0.01);
    }

    @Test void testSellerRealisedPnL() {
        // avg cost 120, sold at 150 → PnL = (150-120)*100 = 3000
        settlement.settle(trade, buyer, seller);
        assertEquals(3000.0, seller.getPortfolio().getRealisedPnL(), 0.01);
    }

    @Test void testDoubleSettlementReturnsFalse() {
        settlement.settle(trade, buyer, seller);
        assertFalse(settlement.settle(trade, buyer, seller));
    }

    @Test void testSettlementFailsIfBuyerLacksReservedCash() {
        buyer.getPortfolio().releaseCash(15000.0); // undo the reservation
        assertFalse(settlement.settle(trade, buyer, seller));
    }

    @Test void testSettlementFailsIfSellerLacksAsset() {
        seller.getPortfolio().reduceAsset(AssetType.ELECTRICITY, 200.0); // zero out
        assertFalse(settlement.settle(trade, buyer, seller));
    }

    @Test void testGetTotalSettledValue() {
        TradeRecord t2 = new TradeRecord(2, 3, 4, AssetType.ELECTRICITY, 50.0, 155.0);
        buyer.getPortfolio().reserveCash(7750.0);
        seller.getPortfolio().addAsset(AssetType.ELECTRICITY, 50.0, 120.0);

        settlement.settle(trade, buyer, seller);
        // Reset for t2
        buyer.getPortfolio().reserveCash(7750.0);
        settlement.settle(t2, buyer, seller);

        // 100*150 + 50*155 = 15000 + 7750 = 22750
        assertEquals(22750.0, settlement.getTotalSettledValue(), 0.01);
    }

    @Test void testAuditLoggedOnSettlement() {
        settlement.settle(trade, buyer, seller);
        assertFalse(auditLog.getEntriesByType("TRADE_SETTLED").isEmpty());
    }
}