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

  public int getOrderType() {
    
  }
  public Trader getTrader() {
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
  }

  public OrderStatus status() {
    return status;
  }

  public long getTimeStamp() {
  }

  public void cancel() {
  }

  public void reject() {
  }

  @Override 
  public String toString() {
  }
}
