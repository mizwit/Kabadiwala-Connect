package com.kabadiwala.connect.api;

public class VerifyOTPRequest {
    public String phone;
    public String otp;
    public String device_id;
    
    public VerifyOTPRequest() {}
    
    public VerifyOTPRequest(String phone, String otp, String device_id) {
        this.phone = phone;
        this.otp = otp;
        this.device_id = device_id;
    }
}
