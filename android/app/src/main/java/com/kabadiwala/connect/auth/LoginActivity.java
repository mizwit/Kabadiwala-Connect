package com.kabadiwala.connect.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private TextView pinDisplay;
    private Button[] numberButtons;
    private Button clearButton;
    private Button loginButton;
    private Button registerButton;
    private SecureStorage secureStorage;
    private String deviceId;
    private String enteredPin = "";
    private String phone;
    
    @Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        secureStorage = new SecureStorage(this);
        deviceId = SecurityUtils.getDeviceId(this);
        
        // Check if already registered
        if (!secureStorage.contains("auth_token")) {
            // Not registered, go to registration
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
            return;
        }
        
        phone = secureStorage.getSecure("phone", "");
        
        // Initialize views
        pinDisplay = findViewById(R.id.pinDisplay);
        clearButton = findViewById(R.id.clearButton);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        
        // Number buttons
        numberButtons = new Button[10];
        numberButtons[0] = findViewById(R.id.button0);
        numberButtons[1] = findViewById(R.id.button1);
        numberButtons[2] = findViewById(R.id.button2);
        numberButtons[3] = findViewById(R.id.button3);
        numberButtons[4] = findViewById(R.id.button4);
        numberButtons[5] = findViewById(R.id.button5);
        numberButtons[6] = findViewById(R.id.button6);
        numberButtons[7] = findViewById(R.id.button7);
        numberButtons[8] = findViewById(R.id.button8);
        numberButtons[9] = findViewById(R.id.button9);
        
        // Set up number button listeners
        for (int i = 0; i < 10; i++) {
            final int digit = i;
            numberButtons[i].setOnClickListener(v -> addDigit(digit));
        }
        
        clearButton.setOnClickListener(v -> clearPin());
        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> {
            // Clear all data and go to registration
            secureStorage.clearAll();
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
        });
    }
    
    private void addDigit(int digit) {
        if (enteredPin.length() < 4) {
            enteredPin += String.valueOf(digit);
            updatePinDisplay();
        }
    }
    
    private void clearPin() {
        enteredPin = "";
        updatePinDisplay();
    }
    
    private void updatePinDisplay() {
        // Display as asterisks for security
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < enteredPin.length(); i++) {
            display.append("●");
        }
        pinDisplay.setText(display.toString());
    }
    
    private void attemptLogin() {
        if (enteredPin.length() != 4) {
            Toast.makeText(this, "Please enter 4-digit PIN", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verify PIN against local hash
        String storedHash = secureStorage.getSecure("pin_hash", "");
        if (storedHash.isEmpty()) {
            Toast.makeText(this, "No PIN found. Please register.", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (SecurityUtils.verifyPin(this, enteredPin, storedHash)) {
            // PIN matches, verify with backend and get fresh token
            authenticateWithBackend();
        } else {
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            clearPin();
        }
    }
    
    private void authenticateWithBackend() {
        pinDisplay.setText("Authenticating...");
        
        String storedHash = secureStorage.getSecure("pin_hash", "");
        
        // Initialize secure storage in RetrofitClient
        RetrofitClient.setSecureStorage(secureStorage);
        
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest request = new LoginRequest(phone, storedHash, deviceId);
        
        apiService.login_collector(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.success && authResponse.token != null) {
                        // Update token
                        secureStorage.storeSecure("auth_token", authResponse.token);
                        
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        
                        // Go to main app
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        pinDisplay.setText("●●●●");
                        Toast.makeText(LoginActivity.this, authResponse.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    pinDisplay.setText("●●●●");
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                pinDisplay.setText("●●●●");
                Toast.makeText(LoginActivity.this, "Connection failed. Using offline mode.", Toast.LENGTH_LONG).show();
                
                // For offline mode, allow login with just local PIN verification
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
