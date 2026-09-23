package com.kabadiwala.connect.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private ProgressBar progressBar;
    private TextView stepIndicator;
    
    private SecureStorage secureStorage;
    private String deviceId;
    private String phone;
    private String generatedOtp;
    private int currentStep = 1; // 1: Phone, 2: OTP, 3: Complete Registration
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        
        secureStorage = new SecureStorage(this);
        deviceId = SecurityUtils.getDeviceId(this);
        
        // Initialize views
        phoneEditText = findViewById(R.id.phoneEditText);
        otpEditText = findViewById(R.id.otpEditText);
        nameEditText = findViewById(R.id.nameEditText);
        pinEditText = findViewById(R.id.pinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        completeRegistrationButton = findViewById(R.id.completeRegistrationButton);
        progressBar = findViewById(R.id.progressBar);
        stepIndicator = findViewById(R.id.stepIndicator);
        
        // Check if already registered
        if (secureStorage.contains("auth_token")) {
            // Already registered, go to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setupStep1();
    }
    
    private void setupStep1() {
        currentStep = 1;
        stepIndicator.setText("Step 1/3: Enter Phone Number");
        
        phoneEditText.setVisibility(View.VISIBLE);
        otpEditText.setVisibility(View.GONE);
        nameEditText.setVisibility(View.GONE);
        pinEditText.setVisibility(View.GONE);
        confirmPinEditText.setVisibility(View.GONE);
        sendOtpButton.setVisibility(View.VISIBLE);
        verifyOtpButton.setVisibility(View.GONE);
        completeRegistrationButton.setVisibility(View.GONE);
        
        sendOtpButton.setOnClickListener(v -> sendOtp());
    }
    
    private void setupStep2() {
        currentStep = 2;
        stepIndicator.setText("Step 2/3: Verify OTP");
        
        phoneEditText.setVisibility(View.GONE);
        otpEditText.setVisibility(View.VISIBLE);
        nameEditText.setVisibility(View.GONE);
        pinEditText.setVisibility(View.GONE);
        confirmPinEditText.setVisibility(View.GONE);
        sendOtpButton.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.VISIBLE);
        completeRegistrationButton.setVisibility(View.GONE);
        
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }
    
    private void setupStep3() {
        currentStep = 3;
        stepIndicator.setText("Step 3/3: Complete Registration");
        
        phoneEditText.setVisibility(View.GONE);
        otpEditText.setVisibility(View.GONE);
        nameEditText.setVisibility(View.VISIBLE);
        pinEditText.setVisibility(View.VISIBLE);
        confirmPinEditText.setVisibility(View.VISIBLE);
        sendOtpButton.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);
        completeRegistrationButton.setVisibility(View.VISIBLE);
        
        completeRegistrationButton.setOnClickListener(v -> completeRegistration());
    }
    
    private void sendOtp() {
        phone = phoneEditText.getText().toString().trim();
        
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
        RegisterRequest request = new RegisterRequest(phone, "", deviceId);
        
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
                        Toast.makeText(RegistrationActivity.this, otpResponse.message, Toast.LENGTH_LONG).show();
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
                            // New user - need to complete registration
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
        String name = nameEditText.getText().toString().trim();
        String pin = pinEditText.getText().toString().trim();
        String confirmPin = confirmPinEditText.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        
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
        
        // Complete registration with backend
        ApiService apiService = RetrofitClient.getApiService();
        RegisterRequest request = new RegisterRequest(phone, name, deviceId);
        
        apiService.complete_registration(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);
                completeRegistrationButton.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.success && authResponse.token != null) {
                        // Store auth token and hash PIN locally
                        secureStorage.storeSecure("auth_token", authResponse.token);
                        secureStorage.storeSecure("collector_id", authResponse.collector_id);
                        secureStorage.storeSecure("phone", phone);
                        secureStorage.storeSecure("pin_hash", SecurityUtils.hashPin(RegistrationActivity.this, pin));
                        
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
