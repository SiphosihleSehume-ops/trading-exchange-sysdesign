package za.co.eliteproject.enums;

public enum AssetType {
<<<<<<< HEAD

    ELECTRICITY("Electricity"), GAS("Natural gas"), CARBON("Carbon"), COAL("Coal");

    private final String label;


    AssetType(String label) {
        this.label = getLabel();
=======
    ELECTRICITY("Electricity"), GAS("Gas"), CARBON("Carbon"), COAL("Coal");

    private String label;

    AssetType(String label) {
        this.label = label;
>>>>>>> main
    }

    public String getLabel() {
        return label;
    }
}
