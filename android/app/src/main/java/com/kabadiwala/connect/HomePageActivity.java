package com.kabadiwala.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kabadiwala.connect.api.ApiService;
import com.kabadiwala.connect.api.RetrofitClient;
import com.kabadiwala.connect.api.HealthResponse;
import com.kabadiwala.connect.security.SecureStorage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePageActivity extends AppCompatActivity {
    private TextView userNameText;
    private TextView userPhoneText;
    private LinearLayout safetyInfoButton;
    private LinearLayout enterLotButton;
    private LinearLayout transactionHistoryButton;
    private LinearLayout totalEarningsButton;
    private TextView testConnectionLink;
    private SecureStorage secureStorage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        secureStorage = new SecureStorage(this);
        
        // Initialize views
        userNameText = findViewById(R.id.userNameText);
        userPhoneText = findViewById(R.id.userPhoneText);
        safetyInfoButton = findViewById(R.id.safetyInfoButton);
        enterLotButton = findViewById(R.id.enterLotButton);
        transactionHistoryButton = findViewById(R.id.transactionHistoryButton);
        totalEarningsButton = findViewById(R.id.totalEarningsButton);
        testConnectionLink = findViewById(R.id.testConnectionLink);
        
        // Display user info
        String name = secureStorage.getSecure("collector_name", "User");
        String phone = secureStorage.getSecure("phone", "+91 XXXXX XXXXX");
        userNameText.setText(name);
        userPhoneText.setText(phone);
        
        // Set up button listeners
        safetyInfoButton.setOnClickListener(v -> openSafetyInfo());
        enterLotButton.setOnClickListener(v -> openEnterLot());
        transactionHistoryButton.setOnClickListener(v -> openTransactionHistory());
        totalEarningsButton.setOnClickListener(v -> openTotalEarnings());
        testConnectionLink.setOnClickListener(v -> testBackendConnection());
    }
    
    private void openSafetyInfo() {
        // Navigate to safety information activity
        Intent intent = new Intent(this, SafetyInfoActivity.class);
        startActivity(intent);
    }
    
    private void openEnterLot() {
        // Navigate to create lot activity
        Intent intent = new Intent(this, CreateLotActivity.class);
        startActivity(intent);
    }
    
    private void openTransactionHistory() {
        // Navigate to transaction history activity
        Intent intent = new Intent(this, LotsListActivity.class);
        startActivity(intent);
    }
    
    private void openTotalEarnings() {
        // Navigate to earnings activity
        Intent intent = new Intent(this, EarningsActivity.class);
        startActivity(intent);
    }
    
    private void testBackendConnection() {
        // Test backend connection
        RetrofitClient.setSecureStorage(secureStorage);
        ApiService apiService = RetrofitClient.getApiService();
        
        apiService.getHealth().enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(HomePageActivity.this, "Backend connection successful!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomePageActivity.this, "Backend connection failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                Toast.makeText(HomePageActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user info when returning to home
        String name = secureStorage.getSecure("collector_name", "User");
        String phone = secureStorage.getSecure("phone", "+91 XXXXX XXXXX");
        userNameText.setText(name);
        userPhoneText.setText(phone);
    }
}
