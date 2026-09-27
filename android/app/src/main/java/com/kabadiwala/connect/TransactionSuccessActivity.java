package com.kabadiwala.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TransactionSuccessActivity extends AppCompatActivity {
    private TextView transactionIdTextView;
    private TextView amountTextView;
    private TextView paymentMethodTextView;
    private TextView messageTextView;
    private Button viewEarningsButton;
    private Button newLotButton;
    private Button homeButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_success);
        
        String transactionId = getIntent().getStringExtra("transactionId");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        
        transactionIdTextView = findViewById(R.id.transactionIdTextView);
        amountTextView = findViewById(R.id.amountTextView);
        paymentMethodTextView = findViewById(R.id.paymentMethodTextView);
        messageTextView = findViewById(R.id.messageTextView);
        viewEarningsButton = findViewById(R.id.viewEarningsButton);
        newLotButton = findViewById(R.id.newLotButton);
        homeButton = findViewById(R.id.homeButton);
        
        transactionIdTextView.setText("Transaction ID: " + transactionId.substring(0, 8) + "...");
        amountTextView.setText("Amount Received: ₹" + String.format("%.2f", amount));
        paymentMethodTextView.setText("Payment Method: " + paymentMethod);
        
        viewEarningsButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionSuccessActivity.this, EarningsActivity.class);
            startActivity(intent);
        });
        
        newLotButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionSuccessActivity.this, CreateLotActivity.class);
            startActivity(intent);
            finish();
        });
        
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionSuccessActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
