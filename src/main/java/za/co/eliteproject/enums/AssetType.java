package za.co.eliteproject.enums;

public enum AssetType {
    ELECTRICITY("Electricity"), GAS("Gas"), CARBON("Carbon"), COAL("Coal");

    private String label;

    AssetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
