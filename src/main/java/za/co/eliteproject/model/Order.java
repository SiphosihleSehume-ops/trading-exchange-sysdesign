package za.co.eliteproject.model;

public class Order {
  protected int orderId;
  protected Trader trader;
  protected AssetType assetType;
  protected OrderSide order;
  protected double quantity;
  protected double filledQty;
  protected OrderStatus status;
  protected long timestamp;
}
