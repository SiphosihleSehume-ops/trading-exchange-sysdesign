package za.co.eliteproject.enums;

public enum AssetType {
    ELECTRICITY("Electricity"), GAS("Natural gas"), CARBON("Carbon"), COAL("Coal");

    private final String label;
    
    AssetType(String label) {
        this.label = getLabel();
}
