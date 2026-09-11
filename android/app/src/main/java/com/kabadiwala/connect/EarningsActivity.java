package com.kabadiwala.connect;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.kabadiwala.connect.database.AppDatabase;
import com.kabadiwala.connect.entities.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EarningsActivity extends AppCompatActivity {
    private AppDatabase database;
    private ListView transactionsListView;
    private TextView totalEarningsTextView;
    private TextView transactionCountTextView;
    private Button backButton;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings);
        
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
        
        transactionsListView = findViewById(R.id.transactionsListView);
        totalEarningsTextView = findViewById(R.id.totalEarningsTextView);
        transactionCountTextView = findViewById(R.id.transactionCountTextView);
        backButton = findViewById(R.id.backButton);
        
        loadEarnings();
        
        backButton.setOnClickListener(v -> finish());
    }
    
    private void loadEarnings() {
        executorService.execute(() -> {
            try {
                List<Transaction> transactions = database.transactionDao().getAll();
                
                List<String> transactionStrings = new ArrayList<>();
                double totalEarnings = 0.0;
                
                for (Transaction transaction : transactions) {
                    String txInfo = String.format("ID: %s\nAmount: ₹%.2f\nStatus: %s\nDate: %s",
                            transaction.transactionId.substring(0, 8) + "...",
                            transaction.finalPrice,
                            transaction.status,
                            transaction.transactionDatetime.substring(0, 10));
                    transactionStrings.add(txInfo);
                    totalEarnings += transaction.finalPrice;
                }
                
                final double finalTotalEarnings = totalEarnings;
                final int finalCount = transactions.size();
                
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, transactionStrings);
                    transactionsListView.setAdapter(adapter);
                    
                    totalEarningsTextView.setText(String.format("Total Earnings: ₹%.2f", finalTotalEarnings));
                    transactionCountTextView.setText(String.format("Total Transactions: %d", finalCount));
                    
                    if (transactions.isEmpty()) {
                        Toast.makeText(this, "No transactions found yet", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading earnings: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
