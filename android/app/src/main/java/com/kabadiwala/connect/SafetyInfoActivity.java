package com.kabadiwala.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SafetyInfoActivity extends AppCompatActivity {
    private Button homeButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_info);
        
        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
