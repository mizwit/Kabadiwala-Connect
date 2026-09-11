package com.kabadiwala.connect.api;

public class TransactionRequest {
    public String lot_id;
    public String recycler_name;
    public double amount;
    public String payment_method;
    
    public TransactionRequest() {}
    
    public TransactionRequest(String lot_id, String recycler_name, double amount, String payment_method) {
        this.lot_id = lot_id;
        this.recycler_name = recycler_name;
        this.amount = amount;
        this.payment_method = payment_method;
    }
}
