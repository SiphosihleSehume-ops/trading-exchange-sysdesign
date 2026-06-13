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

  protected Order(int orderId, Trader trader, AssetType assetType,
                  OrderSide order, double quantity) {
    this.orderId = orderId;
    this.trader = trader;
    this.assetType = assetType;
    this.order = order;
    this.quantity = quantity;
    this.filledQty = 0;
    this.status = OrderStatus.PENDING;
    this.timestamp = System.currentTimeMillis();
  }
}
