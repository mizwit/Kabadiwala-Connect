package com.kabadiwala.connect.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    
    @GET("health")
    Call<HealthResponse> getHealth();
    
    @GET("categories")
    Call<List<MaterialCategory>> getCategories();
    
    @POST("lots")
    Call<LotResponse> createLot(@Body CreateLotRequest request);
    
    @GET("recyclers/match")
    Call<List<RecyclerOffer>> matchRecyclers(@Query("lot_id") String lotId);
    
    @POST("transactions")
    Call<TransactionResponse> createTransaction(@Body TransactionRequest request);
    
    @GET("transactions")
    Call<List<TransactionResponse>> getTransactions();
    
    // Authentication endpoints
    @POST("auth/register")
    Call<OTPResponse> register_collector(@Body RegisterRequest request);
    
    @POST("auth/verify-otp")
    Call<OTPResponse> verify_otp(@Body VerifyOTPRequest request);
    
    @POST("auth/complete-registration")
    Call<AuthResponse> complete_registration(@Body RegisterRequest request);
    
    @POST("auth/login")
    Call<AuthResponse> login_collector(@Body LoginRequest request);
}
