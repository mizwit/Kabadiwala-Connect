package com.kabadiwala.connect.api;

public class RegisterRequest {
    public String phone;
    public String name;
    public String device_id;
    public String pin_hash;
    
    public RegisterRequest() {}
    
    public RegisterRequest(String phone, String name, String device_id, String pin_hash) {
        this.phone = phone;
        this.name = name;
        this.device_id = device_id;
        this.pin_hash = pin_hash;
    }
    
    public RegisterRequest(String phone, String name, String device_id) {
        this.phone = phone;
        this.name = name;
        this.device_id = device_id;
        this.pin_hash = "";
    }
}
