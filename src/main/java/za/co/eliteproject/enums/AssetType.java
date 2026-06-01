package za.co.eliteproject.enums;

public enum AssetType {
    public String label;

    ELECTRICITY, GAS, CARBON, COAL;


    public AssetType(String label) {
        this.label = getLabel();
    }

    public AssetType getLabel() {
        //
    }
}
