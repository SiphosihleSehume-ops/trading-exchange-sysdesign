package za.co.eliteproject.model;

import java.util.*;

public class Trader {
    private int traderId;
    private String username;
    private Portfolio portfolio;
    private boolean isBlacklisted;
    private List<Order> openOrders;


    Trader(int traderId, String username, Portfolio portfolio) {
        this.traderId = traderId;
        this.username = username;
        this.portfolio = portfolio;
        this.openOrders = new ArrayList<>();
        this.isBlacklisted = false;
    }

    public int getTraderId() {
        return traderId;
    }

    public String getUsername() {
        return username;
    }

    public Portfolio portfolio() {
        return portfolio;
    }

    public void addOpenOrder(Order order) {
        openOrders.add(order);
    }

    public boolean removeOpenOrder(int orderId) {

        for (Order order : openOrders) {
            if (order.orderId == orderId) {
                return true;
            }
        }
        return false;
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
}