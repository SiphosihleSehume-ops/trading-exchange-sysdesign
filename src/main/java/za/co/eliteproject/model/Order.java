package za.co.eliteproject.model;

import za.co.eliteproject.enums.AssetType;
import za.co.eliteproject.enums.OrderSide;
import za.co.eliteproject.enums.OrderStatus;

public abstract class Order {

    /** Quantities within this tolerance of zero are treated as zero (floating-point safety). */
    public static final double QTY_EPSILON = 1e-9;

    protected final int orderId;
    protected final Trader trader;
    protected final AssetType assetType;
    protected final OrderSide side;
    protected final double quantity;
    protected double filledQty;
    protected OrderStatus status;
    protected final long timestamp;

    protected Order(int orderId, Trader trader, AssetType assetType, OrderSide side, double quantity) {
        if (trader == null) {
            throw new IllegalArgumentException("Trader cannot be null");
        }
        if (assetType == null) {
            throw new IllegalArgumentException("Asset type cannot be null");
        }
        if (side == null) {
            throw new IllegalArgumentException("Order side cannot be null");
        }
        if (!(quantity > 0)) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.orderId = orderId;
        this.trader = trader;
        this.assetType = assetType;
        this.side = side;
        this.quantity = quantity;
        this.filledQty = 0.0;
        this.status = OrderStatus.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    /** @return "LIMIT" or "MARKET". */
    public abstract String getOrderType();

    /** @return whether this order can be matched at the given price. */
    public abstract boolean canMatchAt(double price);

    public int getOrderId() {
        return orderId;
    }

    public Trader getTrader() {
        return trader;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public OrderSide getSide() {
        return side;
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

    /** @return true once nothing (within tolerance) remains to be filled. */
    public boolean isFullyFilled() {
        return getRemainingQty() <= QTY_EPSILON;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /** Adds qty to filledQty and moves the status to PARTIAL or FILLED. */
    public void fill(double qty) {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot fill an order in terminal status " + status);
        }
        if (!(qty > 0)) {
            throw new IllegalArgumentException("Fill quantity must be positive");
        }
        if (qty > getRemainingQty() + QTY_EPSILON) {
            throw new IllegalArgumentException("Cannot fill more than the remaining quantity");
        }
        filledQty += qty;
        if (isFullyFilled()) {
            filledQty = quantity;
            status = OrderStatus.FILLED;
        } else {
            status = OrderStatus.PARTIAL;
        }
    }

    /** Cancels the order unless it is already terminal (then does nothing). */
    public void cancel() {
        if (!status.isTerminal()) {
            status = OrderStatus.CANCELLED;
        }
    }

    /** Rejects the order unless it is already terminal (then does nothing). */
    public void reject() {
        if (!status.isTerminal()) {
            status = OrderStatus.REJECTED;
        }
    }

    @Override
    public String toString() {
        // Trader is printed by username only: Trader <-> Order must not recurse.
        return getClass().getSimpleName() + "{" +
                "orderId=" + orderId +
                ", trader=" + trader.getUsername() +
                ", assetType=" + assetType +
                ", side=" + side +
                ", quantity=" + quantity +
                ", filledQty=" + filledQty +
                ", status=" + status +
                ", timestamp=" + timestamp +
                '}';
    }
}
