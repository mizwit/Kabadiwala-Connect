package com.kabadiwala.connect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kabadiwala.connect.security.SecureStorage;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Check authentication
        SecureStorage secureStorage = new SecureStorage(this);
        if (!secureStorage.contains("auth_token")) {
            // Not authenticated, redirect to login
            Intent intent = new Intent(this, com.kabadiwala.connect.auth.LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Initialize secure storage in RetrofitClient for auth headers
        com.kabadiwala.connect.api.RetrofitClient.setSecureStorage(secureStorage);
        
        // Navigate to home page after successful authentication
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
        finish();
    }
}
