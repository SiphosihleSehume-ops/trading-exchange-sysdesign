package za.co.eliteproject.enums;

public enum AssetType {
    ELECTRICITY("Electricity (MWh)"),
    GAS("Natural Gas (MMBtu)"),
    CARBON("Carbon Credits (t)"),
    COAL("Coal (tonne)");

    private final String label;

    AssetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
