package com.adyen.checkout.models;

public class InventoryModel {
    public String name;
    public long amount;
    public String currency;

    public InventoryModel(String name, long amount, String currency) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
