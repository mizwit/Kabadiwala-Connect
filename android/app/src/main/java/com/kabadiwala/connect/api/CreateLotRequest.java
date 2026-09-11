package com.kabadiwala.connect.api;

public class CreateLotRequest {
    public String category;
    public double weight_kg;
    public String source_type;
    
    public CreateLotRequest() {}
    
    public CreateLotRequest(String category, double weight_kg, String source_type) {
        this.category = category;
        this.weight_kg = weight_kg;
        this.source_type = source_type;
    }
}
