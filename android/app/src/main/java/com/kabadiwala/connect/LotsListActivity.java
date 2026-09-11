package com.kabadiwala.connect;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.kabadiwala.connect.database.AppDatabase;
import com.kabadiwala.connect.entities.MaterialLot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LotsListActivity extends AppCompatActivity {
    private AppDatabase database;
    private ListView lotsListView;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lots_list);
        
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
        
        lotsListView = findViewById(R.id.lotsListView);
        
        loadLots();
    }
    
    private void loadLots() {
        executorService.execute(() -> {
            try {
                List<MaterialLot> lots = database.materialLotDao().getAll();
                
                List<String> lotStrings = new ArrayList<>();
                for (MaterialLot lot : lots) {
                    String lotInfo = String.format("ID: %s\nWeight: %.2f kg\nValue: ₹%.2f\nStatus: %s",
                            lot.lotId.substring(0, 8) + "...",
                            lot.totalWeight,
                            lot.estimatedValue,
                            lot.status);
                    lotStrings.add(lotInfo);
                }
                
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, lotStrings);
                    lotsListView.setAdapter(adapter);
                    
                    if (lots.isEmpty()) {
                        Toast.makeText(this, "No lots found. Create some lots first!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading lots: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
