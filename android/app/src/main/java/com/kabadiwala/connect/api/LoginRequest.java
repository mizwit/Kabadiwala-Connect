package com.kabadiwala.connect.api;

public class LoginRequest {
    public String phone;
    public String pin_hash;
    public String device_id;
    
    public LoginRequest() {}
    
    public LoginRequest(String phone, String pin_hash, String device_id) {
        this.phone = phone;
        this.pin_hash = pin_hash;
        this.device_id = device_id;
    }
}
