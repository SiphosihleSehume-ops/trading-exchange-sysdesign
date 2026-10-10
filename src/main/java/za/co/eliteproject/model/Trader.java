package za.co.eliteproject.model;

import za.co.eliteproject.enums.OrderSide;

import java.util.ArrayList;
import java.util.List;

public class Trader {

    private final int traderId;
    private final String username;
    private final Portfolio portfolio;
    private boolean isBlacklisted;
    private final List<Order> openOrders;

    public Trader(int traderId, String username, Portfolio portfolio) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio cannot be null");
        }
        this.traderId = traderId;
        this.username = username;
        this.portfolio = portfolio;
        this.isBlacklisted = false;
        this.openOrders = new ArrayList<>();
    }

    public int getTraderId() {
        return traderId;
    }

    public String getUsername() {
        return username;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void addOpenOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        openOrders.add(order);
    }

    /** @return true if an order with that ID was found and removed. */
    public boolean removeOpenOrder(int orderId) {
        return openOrders.removeIf(order -> order.getOrderId() == orderId);
    }

    public List<Order> getOpenOrders() {
        return new ArrayList<>(openOrders);
    }

    public void blacklist() {
        isBlacklisted = true;
    }

    public boolean isBlacklisted() {
        return isBlacklisted;
    }

    /** @return sum of quantity * limitPrice across all open BUY LimitOrders. */
    public double getTotalExposure() {
        double exposure = 0.0;
        for (Order order : openOrders) {
            if (order instanceof LimitOrder limitOrder
                    && order.getSide() == OrderSide.BUY
                    && !order.getStatus().isTerminal()) {
                exposure += limitOrder.getQuantity() * limitOrder.getLimitPrice();
            }
        }
        return exposure;
    }

    @Override
    public String toString() {
        // Deliberately omits the portfolio's contents and the order list (Order.toString()
        // references the trader, so printing them here would recurse).
        return "Trader{" +
                "traderId=" + traderId +
                ", username='" + username + '\'' +
                ", isBlacklisted=" + isBlacklisted +
                ", openOrders=" + openOrders.size() +
                '}';
    }
}
