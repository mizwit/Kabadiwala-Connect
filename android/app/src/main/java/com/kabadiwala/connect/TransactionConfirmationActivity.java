package com.kabadiwala.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.kabadiwala.connect.database.AppDatabase;
import com.kabadiwala.connect.entities.MaterialLot;
import com.kabadiwala.connect.entities.Transaction;
import com.kabadiwala.connect.entities.Recycler;
import com.kabadiwala.connect.api.ApiService;
import com.kabadiwala.connect.api.RetrofitClient;
import com.kabadiwala.connect.api.TransactionRequest;
import com.kabadiwala.connect.api.TransactionResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionConfirmationActivity extends AppCompatActivity {
    private AppDatabase database;
    private TextView lotIdTextView;
    private TextView recyclerNameTextView;
    private TextView amountTextView;
    private RadioGroup paymentMethodGroup;
    private Button confirmTransactionButton;
    private Button backButton;
    private ExecutorService executorService;
    
    private String lotId;
    private String recyclerId;
    private String recyclerName;
    private double amount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation);
        
        executorService = Executors.newSingleThreadExecutor();
        
        try {
            database = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "kabadiwala-db")
                    .fallbackToDestructiveMigration()
                    .build();
        } catch (Exception e) {
            Toast.makeText(this, "Database initialization failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Get data from intent
        lotId = getIntent().getStringExtra("lotId");
        recyclerId = getIntent().getStringExtra("recyclerId");
        recyclerName = getIntent().getStringExtra("recyclerName");
        amount = getIntent().getDoubleExtra("amount", 0.0);
        
        lotIdTextView = findViewById(R.id.lotIdTextView);
        recyclerNameTextView = findViewById(R.id.recyclerNameTextView);
        amountTextView = findViewById(R.id.amountTextView);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        confirmTransactionButton = findViewById(R.id.confirmTransactionButton);
        backButton = findViewById(R.id.backButton);
        
        // Display transaction details
        lotIdTextView.setText("Lot ID: " + (lotId != null && lotId.length() > 8 ? lotId.substring(0, 8) + "..." : lotId));
        recyclerNameTextView.setText("Recycler: " + recyclerName);
        amountTextView.setText("Final Amount: ₹" + String.format("%.2f", amount));
        
        confirmTransactionButton.setOnClickListener(v -> confirmTransaction());
        backButton.setOnClickListener(v -> finish());
    }
    
    private void confirmTransaction() {
        int selectedPaymentMethod = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedPaymentMethod == -1) {
            Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
            return;
        }
        
        RadioButton selectedRadioButton = findViewById(selectedPaymentMethod);
        String paymentMethod = selectedRadioButton.getText().toString();
        
        // Disable button to prevent double-click
        confirmTransactionButton.setEnabled(false);
        confirmTransactionButton.setText("Processing...");
        
        // Create transaction via API
        ApiService apiService = RetrofitClient.getApiService();
        TransactionRequest request = new TransactionRequest(lotId, recyclerName, amount, paymentMethod.toLowerCase());
        
        apiService.createTransaction(request).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TransactionResponse txResponse = response.body();
                    
                    // Save to local database
                    saveTransactionLocally(txResponse.transaction_id, paymentMethod, true);
                } else {
                    // API failed, try local fallback
                    saveTransactionLocally(UUID.randomUUID().toString(), paymentMethod, false);
                }
            }
            
            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                // Network failed, try local fallback
                saveTransactionLocally(UUID.randomUUID().toString(), paymentMethod, false);
            }
        });
    }
    
    private void saveTransactionLocally(String transactionId, String paymentMethod, boolean synced) {
        executorService.execute(() -> {
            try {
                // Ensure recycler exists in database to satisfy foreign key constraint
                Recycler recycler = new Recycler(
                        recyclerId,
                        recyclerName,
                        "Mumbai", // Default for prototype
                        "contact@kabadiwala.com",
                        true);
                database.recyclerDao().insert(recycler);

                Transaction transaction = new Transaction(
                        transactionId, 
                        lotId, 
                        recyclerId, 
                        amount, 
                        amount);
                transaction.status = "confirmed";
                transaction.synced = synced;
                
                database.transactionDao().insert(transaction);
                
                // Update lot status
                MaterialLot lot =
                        database.materialLotDao().getById(lotId);
                if (lot != null) {
                    lot.status = "confirmed";
                    database.materialLotDao().update(lot);
                }
                
                final String finalPaymentMethod = paymentMethod;
                final String finalTransactionId = transactionId;
                final boolean finalSynced = synced;
                
                runOnUiThread(() -> {
                    String message = finalSynced ? 
                            "Transaction confirmed and synced! Payment: " + finalPaymentMethod :
                            "Transaction confirmed locally! Payment: " + finalPaymentMethod;
                    Toast.makeText(TransactionConfirmationActivity.this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate to success/activity completion
                    Intent intent = new Intent(TransactionConfirmationActivity.this, TransactionSuccessActivity.class);
                    intent.putExtra("transactionId", finalTransactionId);
                    intent.putExtra("amount", amount);
                    intent.putExtra("paymentMethod", finalPaymentMethod);
                    startActivity(intent);
                    
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(TransactionConfirmationActivity.this, 
                            "Error confirming transaction: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    confirmTransactionButton.setEnabled(true);
                    confirmTransactionButton.setText("Confirm Transaction");
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
}
