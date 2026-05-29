package za.co.eliteproject;

import org.junit.jupiter.api.*;
import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.model.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PortfolioTest {

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio(1, 100000.0);
    }

    @Test void testInitialState() {
        assertEquals(100000.0, portfolio.getCashBalance(), 0.01);
        assertEquals(0.0, portfolio.getReservedCash(), 0.01);
        assertEquals(100000.0, portfolio.getTotalCash(), 0.01);
    }

    @Test void testNegativeInitialCashThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Portfolio(2, -1.0));
    }

    @Test void testReserveCash() {
        assertTrue(portfolio.reserveCash(30000.0));
        assertEquals(70000.0, portfolio.getCashBalance(), 0.01);
        assertEquals(30000.0, portfolio.getReservedCash(), 0.01);
    }

    @Test void testReserveCashInsufficientReturnsFalse() {
        assertFalse(portfolio.reserveCash(200000.0));
        assertEquals(100000.0, portfolio.getCashBalance(), 0.01);
    }

    @Test void testReleaseCash() {
        portfolio.reserveCash(20000.0);
        portfolio.releaseCash(20000.0);
        assertEquals(100000.0, portfolio.getCashBalance(), 0.01);
        assertEquals(0.0, portfolio.getReservedCash(), 0.01);
    }

    @Test void testDeductReservedCash() {
        portfolio.reserveCash(50000.0);
        assertTrue(portfolio.deductReservedCash(50000.0));
        assertEquals(0.0, portfolio.getReservedCash(), 0.01);
    }

    @Test void testCreditCash() {
        portfolio.creditCash(5000.0);
        assertEquals(105000.0, portfolio.getCashBalance(), 0.01);
    }

    @Test void testAddAndGetAsset() {
        portfolio.addAsset(AssetType.ELECTRICITY, 100.0, 150.0);
        Asset asset = portfolio.getAsset(AssetType.ELECTRICITY);
        assertNotNull(asset);
        assertEquals(100.0, asset.getQuantity(), 0.01);
        assertEquals(150.0, asset.getAvgCost(), 0.01);
    }

    @Test void testAddAssetVwap() {
        portfolio.addAsset(AssetType.GAS, 100.0, 200.0);
        portfolio.addAsset(AssetType.GAS, 100.0, 300.0);
        Asset asset = portfolio.getAsset(AssetType.GAS);
        assertEquals(200.0, asset.getQuantity(), 0.01);
        assertEquals(250.0, asset.getAvgCost(), 0.01); // (100*200 + 100*300) / 200
    }

    @Test void testReduceAsset() {
        portfolio.addAsset(AssetType.COAL, 50.0, 80.0);
        assertTrue(portfolio.reduceAsset(AssetType.COAL, 30.0));
        assertEquals(20.0, portfolio.getAsset(AssetType.COAL).getQuantity(), 0.01);
    }

    @Test void testReduceAssetBelowZeroReturnsFalse() {
        portfolio.addAsset(AssetType.COAL, 10.0, 80.0);
        assertFalse(portfolio.reduceAsset(AssetType.COAL, 20.0));
    }

    @Test void testReduceAssetNotHeldReturnsFalse() {
        assertFalse(portfolio.reduceAsset(AssetType.CARBON, 5.0));
    }

    @Test void testGetAllAssetsIsDefensiveCopy() {
        portfolio.addAsset(AssetType.GAS, 10.0, 100.0);
        Map<AssetType, Asset> copy = portfolio.getAllAssets();
        copy.clear();
        assertEquals(1, portfolio.getAllAssets().size());
    }

    @Test void testGetTotalValue() {
        portfolio.addAsset(AssetType.ELECTRICITY, 100.0, 150.0);
        Map<AssetType, Double> prices = Map.of(AssetType.ELECTRICITY, 200.0);
        // cash (100000) + market value (100 * 200 = 20000) = 120000
        assertEquals(120000.0, portfolio.getTotalValue(prices), 0.01);
    }

    @Test void testRealisedPnL() {
        portfolio.recordRealisedPnL(5000.0);
        portfolio.recordRealisedPnL(-1000.0);
        assertEquals(4000.0, portfolio.getRealisedPnL(), 0.01);
    }
}