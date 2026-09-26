package com.kabadiwala.connect.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kabadiwala.connect.MainActivity;
import com.kabadiwala.connect.R;
import com.kabadiwala.connect.api.ApiService;
import com.kabadiwala.connect.api.RetrofitClient;
import com.kabadiwala.connect.api.RegisterRequest;
import com.kabadiwala.connect.api.VerifyOTPRequest;
import com.kabadiwala.connect.api.OTPResponse;
import com.kabadiwala.connect.api.AuthResponse;
import com.kabadiwala.connect.security.SecurityUtils;
import com.kabadiwala.connect.security.SecureStorage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {
    private EditText phoneEditText;
    private EditText otpEditText;
    private EditText nameEditText;
    private EditText pinEditText;
    private EditText confirmPinEditText;
    private Button sendOtpButton;
    private Button verifyOtpButton;
    private Button completeRegistrationButton;
    private TextView loginLink;
    private TextView headingText;
    private ProgressBar progressBar;
    private SecureStorage secureStorage;
    private String deviceId;
    private String phone;
    private String generatedOtp;
    private int currentStep = 1; // 1: Initial Registration, 2: OTP Verification, 3: PIN Creation
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        
        secureStorage = new SecureStorage(this);
        deviceId = SecurityUtils.getDeviceId(this);
        
        // Don't check for existing auth token - allow fresh registration
        // This supports reinstallation and mobile number changes
        
        // Initialize views
        phoneEditText = findViewById(R.id.phoneEditText);
        otpEditText = findViewById(R.id.otpEditText);
        nameEditText = findViewById(R.id.nameEditText);
        pinEditText = findViewById(R.id.pinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        completeRegistrationButton = findViewById(R.id.completeRegistrationButton);
        loginLink = findViewById(R.id.loginLink);
        headingText = findViewById(R.id.headingText);
        progressBar = findViewById(R.id.progressBar);
        
        setupStep1();
    }
    
    private void setupStep1() {
        currentStep = 1;
        headingText.setText("Registration");
        
        // Show initial registration fields
        nameEditText.setVisibility(View.VISIBLE);
        phoneEditText.setVisibility(View.VISIBLE);
        sendOtpButton.setVisibility(View.VISIBLE);
        loginLink.setVisibility(View.VISIBLE);
        
        // Hide other fields
        otpEditText.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        pinEditText.setVisibility(View.GONE);
        confirmPinEditText.setVisibility(View.GONE);
        completeRegistrationButton.setVisibility(View.GONE);
        
        sendOtpButton.setOnClickListener(v -> sendOtp());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
    
    private void setupStep2() {
        currentStep = 2;
        headingText.setText("Enter OTP");
        
        // Show OTP verification fields
        otpEditText.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.VISIBLE);
        
        // Hide other fields
        nameEditText.setVisibility(View.GONE);
        phoneEditText.setVisibility(View.GONE);
        sendOtpButton.setVisibility(View.GONE);
        loginLink.setVisibility(View.GONE);
        pinEditText.setVisibility(View.GONE);
        confirmPinEditText.setVisibility(View.GONE);
        completeRegistrationButton.setVisibility(View.GONE);
        
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }
    
    private void setupStep3() {
        currentStep = 3;
        headingText.setText("Create PIN");
        
        // Show PIN creation fields
        nameEditText.setVisibility(View.VISIBLE);
        pinEditText.setVisibility(View.VISIBLE);
        confirmPinEditText.setVisibility(View.VISIBLE);
        completeRegistrationButton.setVisibility(View.VISIBLE);
        
        // Hide other fields
        phoneEditText.setVisibility(View.GONE);
        otpEditText.setVisibility(View.GONE);
        sendOtpButton.setVisibility(View.GONE);
        loginLink.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        
        completeRegistrationButton.setOnClickListener(v -> completeRegistration());
    }
    
    private void sendOtp() {
        String name = nameEditText.getText().toString().trim();
        phone = phoneEditText.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (phone.isEmpty() || phone.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        sendOtpButton.setEnabled(false);
        
        // Initialize secure storage in RetrofitClient
        RetrofitClient.setSecureStorage(secureStorage);
        
        // Show current URL for debugging
        String currentUrl = RetrofitClient.getCurrentBaseUrl();
        Toast.makeText(this, "Connecting to: " + currentUrl, Toast.LENGTH_SHORT).show();
        
        ApiService apiService = RetrofitClient.getApiService();
        RegisterRequest request = new RegisterRequest(phone, name, deviceId);
        
        apiService.register_collector(request).enqueue(new Callback<OTPResponse>() {
            @Override
            public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                progressBar.setVisibility(View.GONE);
                sendOtpButton.setEnabled(true);
                
                Log.d("RegistrationActivity", "Response code: " + response.code());
                Log.d("RegistrationActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    OTPResponse otpResponse = response.body();
                    Log.d("RegistrationActivity", "OTP Response success: " + otpResponse.success);
                    Log.d("RegistrationActivity", "OTP Response message: " + otpResponse.message);
                    
                    if (otpResponse.success) {
                        // Extract OTP from message (for prototype)
                        String message = otpResponse.message;
                        if (message.contains("OTP sent:")) {
                            generatedOtp = message.split("OTP sent: ")[1].split(" ")[0];
                            Toast.makeText(RegistrationActivity.this, 
                                    "OTP: " + generatedOtp + " (Prototype: Enter this OTP)", 
                                    Toast.LENGTH_LONG).show();
                        }
                        setupStep2();
                    } else {
                        // If phone already registered, offer to go to login
                        if (otpResponse.message.contains("already registered")) {
                            Toast.makeText(RegistrationActivity.this, "Phone number already registered. Please login.", Toast.LENGTH_LONG).show();
                            loginLink.postDelayed(() -> {
                                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                finish();
                            }, 2000);
                        } else {
                            Toast.makeText(RegistrationActivity.this, otpResponse.message, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    String errorMsg = "Registration failed. HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("RegistrationActivity", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(RegistrationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("RegistrationActivity", errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<OTPResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                sendOtpButton.setEnabled(true);
                String errorMsg = "Connection failed: " + t.getMessage();
                Toast.makeText(RegistrationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e("RegistrationActivity", "Connection error", t);
            }
        });
    }
    
    private void verifyOtp() {
        String enteredOtp = otpEditText.getText().toString().trim();
        
        if (enteredOtp.isEmpty() || enteredOtp.length() != 4) {
            Toast.makeText(this, "Please enter 4-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        verifyOtpButton.setEnabled(false);
        
        ApiService apiService = RetrofitClient.getApiService();
        VerifyOTPRequest request = new VerifyOTPRequest(phone, enteredOtp, deviceId);
        
        apiService.verify_otp(request).enqueue(new Callback<OTPResponse>() {
            @Override
            public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                progressBar.setVisibility(View.GONE);
                verifyOtpButton.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    OTPResponse otpResponse = response.body();
                    if (otpResponse.success) {
                        if (otpResponse.verification_required) {
                            // New user - need to complete registration with PIN
                            setupStep3();
                        } else {
                            // Existing user - could go directly to PIN setup
                            Toast.makeText(RegistrationActivity.this, "Already registered. Please login.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, otpResponse.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<OTPResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                verifyOtpButton.setEnabled(true);
                Toast.makeText(RegistrationActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void completeRegistration() {
        String pin = pinEditText.getText().toString().trim();
        String confirmPin = confirmPinEditText.getText().toString().trim();
        
        if (!SecurityUtils.isValidPin(pin)) {
            Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!pin.equals(confirmPin)) {
            Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        completeRegistrationButton.setEnabled(false);
        
        // Hash the PIN locally
        String pinHash = SecurityUtils.hashPin(RegistrationActivity.this, pin);
        
        // Complete registration with backend
        ApiService apiService = RetrofitClient.getApiService();
        RegisterRequest request = new RegisterRequest(phone, nameEditText.getText().toString().trim(), deviceId, pinHash);
        
        apiService.complete_registration(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);
                completeRegistrationButton.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.success && authResponse.token != null) {
                        // Store auth token and other data locally
                        secureStorage.storeSecure("auth_token", authResponse.token);
                        secureStorage.storeSecure("collector_id", authResponse.collector_id);
                        secureStorage.storeSecure("phone", phone);
                        secureStorage.storeSecure("pin_hash", pinHash);
                        
                        Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_LONG).show();
                        
                        // Go to main app
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, authResponse.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                completeRegistrationButton.setEnabled(true);
                Toast.makeText(RegistrationActivity.this, "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
