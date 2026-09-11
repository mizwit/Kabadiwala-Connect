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
import com.kabadiwala.connect.api.HealthResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private AppDatabase database;
    private EditText weightEditText;
    private Spinner categorySpinner;
    private TextView estimatedValueTextView;
    private Button createLotButton;
    private Button viewLotsButton;
    private Button apiTestButton;
    private Button viewEarningsButton;
    private ExecutorService executorService;
    
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
        viewLotsButton = findViewById(R.id.viewLotsButton);
        apiTestButton = findViewById(R.id.apiTestButton);
        viewEarningsButton = findViewById(R.id.viewEarningsButton);
        
        // Setup category spinner with enhanced categories
        String[] categories = {"Copper Cables", "PCBs", "CRT Monitors", "LCD Panels", "LED Monitors", 
                              "Lithium Batteries", "Lead Acid Batteries", "Electric Motors", "Aluminum Scrap", 
                              "Mixed Plastics", "Computer Cases", "Power Supplies", "Hard Drives", 
                              "Motherboards", "RAM Modules"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        
        // Calculate estimated value when weight changes
        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                calculateEstimatedValue();
            }
        });
        
        // Calculate when category changes
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                calculateEstimatedValue();
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Create lot button click handler
        createLotButton.setOnClickListener(v -> createMaterialLot());
        
        // View lots button click handler
        viewLotsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LotsListActivity.class);
            startActivity(intent);
        });
        
        // API test button click handler
        apiTestButton.setOnClickListener(v -> testBackendConnection());
        
        // View earnings button click handler
        viewEarningsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EarningsActivity.class);
            startActivity(intent);
        });
    }
    
    private void calculateEstimatedValue() {
        String weightStr = weightEditText.getText().toString();
        if (weightStr.isEmpty()) {
            estimatedValueTextView.setText("Estimated Value: ₹0");
            return;
        }
        
        try {
            double weight = Double.parseDouble(weightStr);
            String category = categorySpinner.getSelectedItem().toString();
            
            // Simple rate calculation (matching backend rates)
            double rate = getRateForCategory(category);
            double estimatedValue = weight * rate;
            
            estimatedValueTextView.setText(String.format("Estimated Value: ₹%.2f", estimatedValue));
        } catch (NumberFormatException e) {
            estimatedValueTextView.setText("Estimated Value: ₹0");
        }
    }
    
    private double getRateForCategory(String category) {
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
    
    private void createMaterialLot() {
        String weightStr = weightEditText.getText().toString();
        if (weightStr.isEmpty()) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double weight = Double.parseDouble(weightStr);
            String category = categorySpinner.getSelectedItem().toString();
            double rate = getRateForCategory(category);
            double estimatedValue = weight * rate;
            
            // Disable button to prevent double-click
            createLotButton.setEnabled(false);
            createLotButton.setText("Creating...");
            
            // Create lot via API
            ApiService apiService = RetrofitClient.getApiService();
            CreateLotRequest request = new CreateLotRequest(category, weight, "collected");
            
            apiService.createLot(request).enqueue(new Callback<LotResponse>() {
                @Override
                public void onResponse(Call<LotResponse> call, Response<LotResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LotResponse lotResponse = response.body();
                        
                        // Also save to local database for offline support
                        executorService.execute(() -> {
                            try {
                                String collectorId = UUID.randomUUID().toString();
                                
                                // Create collector if not exists
                                Collector collector = new Collector(
                                        collectorId, 
                                        "Demo Collector", 
                                        "1234567890", 
                                        "en", 
                                        "Mumbai");
                                database.collectorDao().insert(collector);
                                
                                // Save lot locally
                                MaterialLot localLot = new MaterialLot(
                                        lotResponse.lot_id, 
                                        collectorId, 
                                        "collected", 
                                        weight, 
                                        lotResponse.estimated_value);
                                localLot.status = lotResponse.status;
                                localLot.synced = true;
                                database.materialLotDao().insert(localLot);
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, 
                                            "Lot created successfully! ID: " + lotResponse.lot_id.substring(0, 8) + "...", 
                                            Toast.LENGTH_LONG).show();
                                    
                                    // Reset form
                                    weightEditText.setText("");
                                    estimatedValueTextView.setText("Estimated Value: ₹0");
                                    createLotButton.setEnabled(true);
                                    createLotButton.setText("Create Digital Lot");
                                    
                                    // Navigate to recycler selection
                                    Intent intent = new Intent(MainActivity.this, RecyclerSelectionActivity.class);
                                    intent.putExtra("lotId", lotResponse.lot_id);
                                    intent.putExtra("estimatedValue", lotResponse.estimated_value);
                                    startActivity(intent);
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    // API succeeded but local save failed - still proceed
                                    Toast.makeText(MainActivity.this, 
                                            "Lot created via API! Local save failed: " + e.getMessage(), 
                                            Toast.LENGTH_LONG).show();
                                    
                                    weightEditText.setText("");
                                    estimatedValueTextView.setText("Estimated Value: ₹0");
                                    createLotButton.setEnabled(true);
                                    createLotButton.setText("Create Digital Lot");
                                    
                                    Intent intent = new Intent(MainActivity.this, RecyclerSelectionActivity.class);
                                    intent.putExtra("lotId", lotResponse.lot_id);
                                    intent.putExtra("estimatedValue", lotResponse.estimated_value);
                                    startActivity(intent);
                                });
                            }
                        });
                    } else {
                        // API failed, try local fallback
                        createLocalLotFallback(weight, category, estimatedValue);
                    }
                }
                
                @Override
                public void onFailure(Call<LotResponse> call, Throwable t) {
                    // API call failed, try local fallback
                    createLocalLotFallback(weight, category, estimatedValue);
                }
            });
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show();
            createLotButton.setEnabled(true);
            createLotButton.setText("Create Digital Lot");
        }
    }
    
    private void createLocalLotFallback(double weight, String category, double estimatedValue) {
        executorService.execute(() -> {
            try {
                String lotId = UUID.randomUUID().toString();
                String collectorId = UUID.randomUUID().toString();
                
                // Create collector if not exists
                Collector collector = new Collector(
                        collectorId, 
                        "Demo Collector", 
                        "1234567890", 
                        "en", 
                        "Mumbai");
                database.collectorDao().insert(collector);
                
                MaterialLot lot = new MaterialLot(lotId, collectorId, "collected", weight, estimatedValue);
                lot.synced = false; // Mark as not synced
                database.materialLotDao().insert(lot);
                
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                            "Offline mode: Lot saved locally! ID: " + lotId.substring(0, 8) + "...", 
                            Toast.LENGTH_LONG).show();
                    
                    weightEditText.setText("");
                    estimatedValueTextView.setText("Estimated Value: ₹0");
                    createLotButton.setEnabled(true);
                    createLotButton.setText("Create Digital Lot");
                    
                    Intent intent = new Intent(MainActivity.this, RecyclerSelectionActivity.class);
                    intent.putExtra("lotId", lotId);
                    intent.putExtra("estimatedValue", estimatedValue);
                    startActivity(intent);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                            "Error creating lot: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    createLotButton.setEnabled(true);
                    createLotButton.setText("Create Digital Lot");
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    private void testBackendConnection() {
        Toast.makeText(this, "Testing backend connection...", Toast.LENGTH_SHORT).show();
        
        // Check network availability
        if (!RetrofitClient.isNetworkAvailable(this)) {
            Toast.makeText(this, "✗ No network connection available", Toast.LENGTH_LONG).show();
            return;
        }
        
        String currentUrl = RetrofitClient.getCurrentBaseUrl();
        Toast.makeText(this, "Trying: " + currentUrl, Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                // Test HTTP connection directly first
                java.net.URL url = new java.net.URL(currentUrl + "health");
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                
                int responseCode = connection.getResponseCode();
                String finalCurrentUrl = currentUrl;
                
                runOnUiThread(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(MainActivity.this, 
                                "✓ Direct HTTP test succeeded! URL: " + finalCurrentUrl, 
                                Toast.LENGTH_LONG).show();
                        // Now try Retrofit
                        testRetrofitConnection();
                    } else {
                        Toast.makeText(MainActivity.this, 
                                "✗ HTTP test failed: " + responseCode, 
                                Toast.LENGTH_LONG).show();
                        tryFallbackUrl();
                    }
                });
                connection.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                            "✗ Direct HTTP test failed: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    tryFallbackUrl();
                });
            }
        }).start();
    }
    
    private void testRetrofitConnection() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getHealth().enqueue(new Callback<HealthResponse>() {
            @Override
            public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HealthResponse health = response.body();
                    Toast.makeText(MainActivity.this, 
                            "✓ Retrofit connected! Status: " + health.status, 
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, 
                            "✗ Retrofit error: " + response.code(), 
                            Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<HealthResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, 
                        "✗ Retrofit failed: " + t.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void tryFallbackUrl() {
        String currentUrl = RetrofitClient.getCurrentBaseUrl();
        String fallbackUrl;
        
        if (currentUrl.equals("http://10.0.2.2:8000/")) {
            fallbackUrl = "http://192.168.1.7:8000/"; // Try LAN IP
        } else if (currentUrl.equals("http://192.168.1.7:8000/")) {
            fallbackUrl = "http://localhost:8000/"; // Try localhost
        } else {
            fallbackUrl = "http://10.0.2.2:8000/"; // Try emulator
        }
        
        Toast.makeText(this, "Trying fallback: " + fallbackUrl, Toast.LENGTH_SHORT).show();
        RetrofitClient.setBaseUrl(fallbackUrl);
        
        // Retry connection
        testBackendConnection();
    }
}
