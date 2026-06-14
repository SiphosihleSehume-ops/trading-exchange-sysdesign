package za.co.eliteproject.model;

public abstract class Order {
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

  public abstract String getOrderType();

  public abstract boolean catchMatchAt(double price);

  public int getOrderId() {
    return orderId;
  }
  public Trader getTrader() {
    return trader;
  }

  public AssetType getAssetType() {
    return assetType;
  }

  public OrderSide getOrderSide() {
    return order;
  }

  public double getQuantity() {
    return quantity;
  }

  public double getFilledQty() {
    return filledQty;
  }

  public double getRemainingQty() {
    return quantity - filledQty;
  }

  public OrderStatus status() {
    return status;
  }

  public long getTimeStamp() {
    return timestamp;
  }

  public void cancel() {
    this.status = OrderStatus.CANCELLED;
  }

  public void reject() {
    this.status = OrderStatus.REJECTED;
  }

  @Override 
  public String toString() {
  }
}
