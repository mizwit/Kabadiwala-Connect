package com.kabadiwala.connect.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kabadiwala.connect.MainActivity;
import com.kabadiwala.connect.R;
import com.kabadiwala.connect.api.ApiService;
import com.kabadiwala.connect.api.RetrofitClient;
import com.kabadiwala.connect.api.LoginRequest;
import com.kabadiwala.connect.api.AuthResponse;
import com.kabadiwala.connect.security.SecurityUtils;
import com.kabadiwala.connect.security.SecureStorage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText phoneEditText;
    private EditText pinEditText;
    private Button loginButton;
    private TextView registerLink;
    private SecureStorage secureStorage;
    private String deviceId;
    private String phone;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        secureStorage = new SecureStorage(this);
        deviceId = SecurityUtils.getDeviceId(this);
        
        // Initialize views
        phoneEditText = findViewById(R.id.phoneEditText);
        pinEditText = findViewById(R.id.pinEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        
        // Don't check for existing auth token - allow fresh login with phone+PIN
        // This supports reinstallation and mobile number changes
        
        loginButton.setOnClickListener(v -> attemptLogin());
        registerLink.setOnClickListener(v -> {
            // Clear all data and go to registration
            secureStorage.clearAll();
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
    }
    
    private void attemptLogin() {
        String enteredPhone = phoneEditText.getText().toString().trim();
        String enteredPin = pinEditText.getText().toString().trim();
        
        if (enteredPhone.isEmpty() || enteredPhone.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (enteredPin.isEmpty() || enteredPin.length() != 4) {
            Toast.makeText(this, "Please enter 4-digit PIN", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Authenticate with backend using phone + PIN
        authenticateWithBackend(enteredPhone, enteredPin);
    }
    
    private void authenticateWithBackend(String phoneNumber, String enteredPin) {
        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        
        // Initialize secure storage in RetrofitClient
        RetrofitClient.setSecureStorage(secureStorage);
        
        // Hash the PIN locally for backend verification
        String pinHash = SecurityUtils.hashPin(this, enteredPin);
        
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest request = new LoginRequest(phoneNumber, pinHash, deviceId);
        
        apiService.login_collector(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                loginButton.setEnabled(true);
                loginButton.setText("Log In");
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.success && authResponse.token != null) {
                        // Store auth token and phone number locally
                        secureStorage.storeSecure("auth_token", authResponse.token);
                        secureStorage.storeSecure("collector_id", authResponse.collector_id);
                        secureStorage.storeSecure("phone", phoneNumber);
                        secureStorage.storeSecure("pin_hash", pinHash);
                        
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        
                        // Go to main app
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, authResponse.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                loginButton.setEnabled(true);
                loginButton.setText("Log In");
                Toast.makeText(LoginActivity.this, "Connection failed. Please check your internet connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
