package com.kabadiwala.connect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.kabadiwala.connect.database.AppDatabase;
import com.kabadiwala.connect.entities.MaterialLot;
import com.kabadiwala.connect.entities.Collector;
import com.kabadiwala.connect.api.ApiService;
import com.kabadiwala.connect.api.RetrofitClient;
import com.kabadiwala.connect.api.CreateLotRequest;
import com.kabadiwala.connect.api.LotResponse;
import com.kabadiwala.connect.security.SecureStorage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateLotActivity extends AppCompatActivity {
    private AppDatabase database;
    private EditText weightEditText;
    private Spinner categorySpinner;
    private TextView estimatedValueTextView;
    private Button createLotButton;
    private Button homeButton;
    private ExecutorService executorService;
    private double estimatedValue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize executor service for database operations
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize database
        try {
            database = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "kabadiwala-db")
                    .fallbackToDestructiveMigration()
                    .build();
        } catch (Exception e) {
            Toast.makeText(this, "Database initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Initialize views
        weightEditText = findViewById(R.id.weightEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        estimatedValueTextView = findViewById(R.id.estimatedValueTextView);
        createLotButton = findViewById(R.id.createLotButton);
        homeButton = findViewById(R.id.homeButton);
        
        // Set up category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{
                        "Copper Cables", "PCBs", "CRT Monitors", "LCD Panels", 
                        "LED Monitors", "Lithium Batteries", "Lead Acid Batteries",
                        "Electric Motors", "Aluminum Scrap", "Mixed Plastics",
                        "Computer Cases", "Power Supplies", "Hard Drives",
                        "Motherboards", "RAM Modules"
                });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        
        // Add text watcher for weight to calculate estimated value
        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateEstimatedValue();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        createLotButton.setOnClickListener(v -> createLot());
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void calculateEstimatedValue() {
        String weightStr = weightEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        
        if (!weightStr.isEmpty() && !category.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                double rate = getRateForCategory(category);
                estimatedValue = weight * rate;
                estimatedValueTextView.setText(String.format("Estimated Value: ₹%.2f", estimatedValue));
            } catch (NumberFormatException e) {
                estimatedValue = 0.0;
                estimatedValueTextView.setText("Estimated Value: ₹0.00");
            }
        } else {
            estimatedValue = 0.0;
            estimatedValueTextView.setText("Estimated Value: ₹0.00");
        }
    }
    
    private double getRateForCategory(String category) {
        // Rates per kg
        switch (category) {
            case "Copper Cables": return 250.0;
            case "PCBs": return 150.0;
            case "CRT Monitors": return 50.0;
            case "LCD Panels": return 80.0;
            case "LED Monitors": return 90.0;
            case "Lithium Batteries": return 120.0;
            case "Lead Acid Batteries": return 80.0;
            case "Electric Motors": return 120.0;
            case "Aluminum Scrap": return 90.0;
            case "Mixed Plastics": return 30.0;
            case "Computer Cases": return 40.0;
            case "Power Supplies": return 60.0;
            case "Hard Drives": return 70.0;
            case "Motherboards": return 100.0;
            case "RAM Modules": return 45.0;
            default: return 50.0;
        }
    }
    
    private void createLot() {
        String weightStr = weightEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double weight;
        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (weight <= 0) {
            Toast.makeText(this, "Weight must be positive", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create lot in local database first
        createLocalLot(category, weight);
        
        // Try to sync with backend
        syncWithBackend(category, weight);
    }
    
    private void createLocalLot(String category, double weight) {
        executorService.execute(() -> {
            try {
                // Ensure collector exists
                SecureStorage secureStorage = new SecureStorage(CreateLotActivity.this);
                String collectorId = secureStorage.getSecure("collector_id", "");
                
                if (collectorId.isEmpty()) {
                    collectorId = UUID.randomUUID().toString();
                    secureStorage.storeSecure("collector_id", collectorId);
                }
                
                // Create collector if needed
                database.collectorDao().insert(new Collector(
                        collectorId,
                        "Collector",
                        "Unknown",
                        "en",
                        "Unknown"
                ));
                
                // Create lot
                String lotId = UUID.randomUUID().toString();
                
                MaterialLot lot = new MaterialLot(
                        lotId,
                        collectorId,
                        category,
                        weight,
                        estimatedValue
                );
                
                database.materialLotDao().insert(lot);
                
                runOnUiThread(() -> {
                    Toast.makeText(CreateLotActivity.this, "Lot created locally! ID: " + lotId, Toast.LENGTH_SHORT).show();
                    navigateToRecyclerSelection(lotId);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateLotActivity.this, "Error creating lot: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void syncWithBackend(String category, double weight) {
        SecureStorage secureStorage = new SecureStorage(this);
        RetrofitClient.setSecureStorage(secureStorage);
        
        ApiService apiService = RetrofitClient.getApiService();
        CreateLotRequest request = new CreateLotRequest(category, weight, null);
        
        apiService.createLot(request).enqueue(new Callback<LotResponse>() {
            @Override
            public void onResponse(Call<LotResponse> call, Response<LotResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LotResponse lotResponse = response.body();
                    Toast.makeText(CreateLotActivity.this, "Lot synced with backend: " + lotResponse.lot_id, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateLotActivity.this, "Backend sync failed, using local data", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<LotResponse> call, Throwable t) {
                Toast.makeText(CreateLotActivity.this, "Connection failed, using local data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToRecyclerSelection(String lotId) {
        Intent intent = new Intent(this, RecyclerSelectionActivity.class);
        intent.putExtra("lotId", lotId);
        intent.putExtra("estimatedValue", estimatedValue);
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
