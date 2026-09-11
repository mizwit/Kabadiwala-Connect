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
        
        transactionIdTextView.setText("Transaction ID: " + transactionId.substring(0, 8) + "...");
        amountTextView.setText("Amount Received: ₹" + String.format("%.2f", amount));
        paymentMethodTextView.setText("Payment Method: " + paymentMethod);
        
        viewEarningsButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionSuccessActivity.this, EarningsActivity.class);
            startActivity(intent);
        });
        
        newLotButton.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
