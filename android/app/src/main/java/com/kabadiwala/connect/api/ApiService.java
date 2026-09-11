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
}
