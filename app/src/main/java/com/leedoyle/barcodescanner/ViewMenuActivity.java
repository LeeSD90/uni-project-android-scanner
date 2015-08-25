package com.leedoyle.barcodescanner;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class ViewMenuActivity extends ActionBarActivity implements View.OnClickListener {   // Implements View.OnClickListener to listen for button click events

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button scanButton = (Button) findViewById(R.id.scanButton);         // Creates Button objects and assign them references to the UIs main menu button widgets
        Button closeButton = (Button) findViewById(R.id.closeButton);
        Button historyButton = (Button) findViewById(R.id.historyButton);

        scanButton.setOnClickListener(this);                                // Attaches the event click listener to the new Button objects
        closeButton.setOnClickListener(this);
        historyButton.setOnClickListener(this);
    }

    public void onClick(View v){        // Triggered when the UI is pressed
        switch(v.getId()){              // Attempt to identify the UI button pressed
            case R.id.scanButton:       // Scan button was pressed
                initiateScan();         // Attempt to initiate a new scan
                break;
            case R.id.historyButton:    // View History button was pressed
                viewHistory();
                break;
            case R.id.closeButton:      // Close button was pressed
                this.finish();          // Finish the ViewMenuActivity, closing the application
                break;
        }
    }

    private void initiateScan(){                                    // Attempts create an intent beginning a ScanActivity
        Intent scanIntent = new Intent(this, ScanActivity.class);   // Create a new Intent in the current context using the ScanActivity class
        this.startActivity(scanIntent);                             // Start the activity
    }

    private void viewHistory(){                                             // Begins a new ViewHistoryActivity
        Intent historyIntent = new Intent(this, ViewHistoryActivity.class); // Create a new Intent in the current context using the ViewHistoryActivity class
        this.startActivity(historyIntent);                                  // Start the activity
    }
}

