package com.zypher.expensemanager.models;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String id;
    private String type;
    private String date;
    private double amount;
    private String category;
    private String account;
    private String note;

    public Transaction() {}

    public Transaction(String type, String date, double amount, String category, String account, String note) {
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.account = account;
        this.note = note;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}