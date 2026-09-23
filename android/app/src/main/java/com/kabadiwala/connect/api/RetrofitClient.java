package com.kabadiwala.connect.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.kabadiwala.connect.security.SecureStorage;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    
    // Try multiple connection options
    private static final String EMULATOR_URL = "http://10.0.2.2:8000/";
    private static final String LOCALHOST_URL = "http://localhost:8000/";
    private static final String LAN_URL = "http://192.168.1.3:8000/"; // Your computer's current IP
    
    private static Retrofit retrofit = null;
    private static String currentBaseUrl = LAN_URL; // Start with LAN URL for physical devices
    private static SecureStorage secureStorage;
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                // Create OkHttpClient with auth interceptor
                OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
                httpClientBuilder.addInterceptor(new AuthInterceptor());
                
                // Try the emulator URL first
                retrofit = new Retrofit.Builder()
                        .baseUrl(currentBaseUrl)
                        .client(httpClientBuilder.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Log.d(TAG, "Using base URL: " + currentBaseUrl);
            } catch (Exception e) {
                Log.e(TAG, "Failed to create Retrofit client: " + e.getMessage());
                // Fallback to localhost
                currentBaseUrl = LOCALHOST_URL;
                OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
                httpClientBuilder.addInterceptor(new AuthInterceptor());
                retrofit = new Retrofit.Builder()
                        .baseUrl(currentBaseUrl)
                        .client(httpClientBuilder.build())
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
    
    /**
     * Cycle through available URLs for testing different connection modes
     */
    public static void cycleBaseUrl() {
        if (currentBaseUrl.equals(LAN_URL)) {
            currentBaseUrl = EMULATOR_URL;
        } else if (currentBaseUrl.equals(EMULATOR_URL)) {
            currentBaseUrl = LOCALHOST_URL;
        } else {
            currentBaseUrl = LAN_URL;
        }
        retrofit = null; // Force recreation with new URL
        Log.d(TAG, "Cycled to base URL: " + currentBaseUrl);
    }
    
    public static void setSecureStorage(SecureStorage storage) {
        secureStorage = storage;
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
    
    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            
            // Add auth token if available
            if (secureStorage != null) {
                String token = secureStorage.getSecure("auth_token", "");
                if (!token.isEmpty()) {
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(original.method(), original.body());
                    original = requestBuilder.build();
                }
            }
            
            return chain.proceed(original);
        }
    }
}
