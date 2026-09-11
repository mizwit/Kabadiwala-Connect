package com.kabadiwala.connect.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    
    // Try multiple connection options
    private static final String EMULATOR_URL = "http://10.0.2.2:8000/";
    private static final String LOCALHOST_URL = "http://localhost:8000/";
    private static final String LAN_URL = "http://192.168.1.7:8000/"; // Your computer's IP from dashboard output
    
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = EMULATOR_URL;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                // Try the emulator URL first
                retrofit = new Retrofit.Builder()
                        .baseUrl(currentBaseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Log.d(TAG, "Using base URL: " + currentBaseUrl);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create Retrofit client: " + e.getMessage());
                // Fallback to localhost
                currentBaseUrl = LOCALHOST_URL;
                retrofit = new Retrofit.Builder()
                        .baseUrl(currentBaseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
        }
        return retrofit;
    }
    
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
    
    public static void setBaseUrl(String url) {
        currentBaseUrl = url;
        retrofit = null; // Force recreation with new URL
    }
    
    public static String getCurrentBaseUrl() {
        return currentBaseUrl;
    }
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}
