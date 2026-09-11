package com.kabadiwala.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kabadiwala.connect.api.ApiService;
import com.kabadiwala.connect.api.RetrofitClient;
import com.kabadiwala.connect.api.RecyclerOffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecyclerSelectionActivity extends AppCompatActivity {
    private ListView recyclersListView;
    private TextView lotInfoTextView;
    private Button confirmHandoverButton;
    private String lotId;
    private double estimatedValue;
    private String selectedRecyclerName;
    private String selectedRecyclerId;
    private double selectedRecyclerAmount;
    
    // Helper class to store recycler info
    private static class RecyclerInfo {
        String id;
        String name;
        double amount;
        String displayString;

        RecyclerInfo(String id, String name, double amount, String displayString) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.displayString = displayString;
        }

        @Override
        public String toString() {
            return displayString;
        }
    }

    private List<RecyclerInfo> recyclerInfos = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_selection);
        
        // Get lot information from intent
        lotId = getIntent().getStringExtra("lotId");
        estimatedValue = getIntent().getDoubleExtra("estimatedValue", 0.0);
        
        recyclersListView = findViewById(R.id.recyclersListView);
        lotInfoTextView = findViewById(R.id.lotInfoTextView);
        confirmHandoverButton = findViewById(R.id.confirmHandoverButton);
        
        if (lotId != null) {
            lotInfoTextView.setText(String.format("Lot ID: %s\nEstimated Value: ₹%.2f", 
                    lotId.length() > 8 ? lotId.substring(0, 8) + "..." : lotId, estimatedValue));
        }
        
        // Load matched recyclers from API
        loadMatchedRecyclers();
        
        confirmHandoverButton.setOnClickListener(v -> confirmHandover());
    }
    
    private void loadMatchedRecyclers() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.matchRecyclers(lotId).enqueue(new Callback<List<RecyclerOffer>>() {
            @Override
            public void onResponse(Call<List<RecyclerOffer>> call, Response<List<RecyclerOffer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RecyclerOffer> offers = response.body();
                    displayRecyclersFromAPI(offers);
                } else {
                    // API failed, use fallback data
                    loadFallbackRecyclers();
                }
            }
            
            @Override
            public void onFailure(Call<List<RecyclerOffer>> call, Throwable t) {
                // Network failed, use fallback data
                loadFallbackRecyclers();
            }
        });
    }
    
    private void displayRecyclersFromAPI(List<RecyclerOffer> offers) {
        recyclerInfos.clear();
        
        for (int i = 0; i < offers.size(); i++) {
            RecyclerOffer offer = offers.get(i);
            String rateLabel = (i == 0) ? " (Best Rate)" : "";
            recyclerInfos.add(new RecyclerInfo(
                    offer.recycler_id, 
                    offer.name, 
                    offer.estimated_amount,
                    offer.name + " - " + offer.location + " - ₹" + 
                    String.format("%.2f", offer.estimated_amount) + rateLabel));
        }
        
        ArrayAdapter<RecyclerInfo> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, recyclerInfos);
        recyclersListView.setAdapter(adapter);
        recyclersListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        recyclersListView.setOnItemClickListener((parent, view, position, id) -> {
            RecyclerInfo selected = recyclerInfos.get(position);
            selectedRecyclerName = selected.name;
            selectedRecyclerId = selected.id;
            selectedRecyclerAmount = selected.amount;
        });
    }
    
    private void loadFallbackRecyclers() {
        // Fallback data for offline mode
        recyclerInfos.clear();
        
        recyclerInfos.add(new RecyclerInfo(UUID.randomUUID().toString(), "EcoRecyclers Pvt Ltd", estimatedValue * 1.10, 
                "EcoRecyclers Pvt Ltd - Mumbai - ₹" + String.format("%.2f", estimatedValue * 1.10) + " (Best Rate)"));
        recyclerInfos.add(new RecyclerInfo(UUID.randomUUID().toString(), "GreenWaste Solutions", estimatedValue * 0.95, 
                "GreenWaste Solutions - Pune - ₹" + String.format("%.2f", estimatedValue * 0.95)));
        recyclerInfos.add(new RecyclerInfo(UUID.randomUUID().toString(), "MetalMax Recycling", estimatedValue * 0.90, 
                "MetalMax Recycling - Nashik - ₹" + String.format("%.2f", estimatedValue * 0.90)));
        recyclerInfos.add(new RecyclerInfo(UUID.randomUUID().toString(), "CleanEarth E-Waste", estimatedValue * 1.05, 
                "CleanEarth E-Waste - Delhi - ₹" + String.format("%.2f", estimatedValue * 1.05)));
        recyclerInfos.add(new RecyclerInfo(UUID.randomUUID().toString(), "RecycleIndia Pvt Ltd", estimatedValue * 0.85, 
                "RecycleIndia Pvt Ltd - Bangalore - ₹" + String.format("%.2f", estimatedValue * 0.85)));
        
        ArrayAdapter<RecyclerInfo> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, recyclerInfos);
        recyclersListView.setAdapter(adapter);
        recyclersListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        recyclersListView.setOnItemClickListener((parent, view, position, id) -> {
            RecyclerInfo selected = recyclerInfos.get(position);
            selectedRecyclerName = selected.name;
            selectedRecyclerId = selected.id;
            selectedRecyclerAmount = selected.amount;
        });
        
        Toast.makeText(this, "Using offline recycler data", Toast.LENGTH_SHORT).show();
    }
    
    private void confirmHandover() {
        if (selectedRecyclerName == null) {
            Toast.makeText(this, "Please select a recycler", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable button to prevent double-click
        confirmHandoverButton.setEnabled(false);
        confirmHandoverButton.setText("Processing...");
        
        // Navigate to transaction confirmation
        Intent intent = new Intent(RecyclerSelectionActivity.this, TransactionConfirmationActivity.class);
        intent.putExtra("lotId", lotId);
        intent.putExtra("recyclerId", selectedRecyclerId);
        intent.putExtra("recyclerName", selectedRecyclerName);
        intent.putExtra("amount", selectedRecyclerAmount);
        startActivity(intent);
        
        finish();
    }
}
