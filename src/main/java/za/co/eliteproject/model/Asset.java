package za.co.eliteproject.model;

public class Asset {
    private AssetType assetType;
    private double quantity;
    private double avgCost;

    public Asset(AssetType assetType, double quantity, double avgCost) {
        this.assetType = assetType;
        this.quantity = quantity;
        this.avgCost = avgCost;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public double getQuanity() {
        return quantity;
    }

    public double getAvgCost() {
        return avgCost;
    }

    public void add(double quant, double price) {
        this.quantity += quant;
        avgCost = ((this.quantity * this.quantity) + (quant * price))
             / (this.quantity + quant);
    }

    public boolean reduce(double quant) {
        quantity -= quant;
        if (quantity <= 0) {
            return false;
        }
        return false;
    }

    public double getMarketValue(double currentPrice) {
        return quantity * currentPrice;
    } 

    public getUnrealisedPnL(double currentPrice) {
        return (currentPrice * avgCost) * quantity;
    }

    @Override String toString() {
        // Generate using IntelliJ
    }
}
