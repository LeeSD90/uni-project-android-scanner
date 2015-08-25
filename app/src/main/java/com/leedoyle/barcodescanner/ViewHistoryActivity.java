package com.leedoyle.barcodescanner;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class ViewHistoryActivity extends ActionBarActivity{

    private ArrayList<Barcode> barcodes;
    private ArrayList<String> barcodeNumbers;
    private BarcodeHistory history;
    private ListView historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);

        historyList = (ListView) findViewById(R.id.historyList);        // Set the ListView object historyList to reference the UIs ListView widget
        history = new BarcodeHistory(getApplicationContext());          // Set history to reference a new BarcodeHistory object created using the current Context

        updateView();                                                   // Update the view to display the list of barcodes saved to the history
    }

    private void initiateViewBarcode(Barcode b){                        // Initiate a new ViewBarcodeActivity Intent for the specified Barcode object
        Intent i = new Intent(this, ViewBarcodeActivity.class);         // Create a new Intent using the ViewBarCodeActivity class
        i.putExtras(b.getBundledBarcode());                             // Package the Barcode object into a bundle, and add it to the Intent as an extra
        this.startActivityForResult(i, 1);                              // Begin the ViewBarcodeActivity with request code 1
    }

    private void updateView(){                                          // Update the UI view to reflect any changes made
        barcodes = history.getHistory();                                // Retrieve the ArrayList of Barcode objects in the barcode history file
        barcodeNumbers = new ArrayList<String>();                       // Create a new ArrayList of Strings to store the numbers of the Barcode objects

        for (Barcode b : barcodes) {                                    // Loop through each Barcode in the ArrayList barcodes
            barcodeNumbers.add(b.getNumber());                          // Add the number of the current Barcode object to the String ArrayList barcodeNumbers
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listrow, barcodeNumbers); // Create a new ArrayAdapter which will use the barcodeNumbers ArrayList

        historyList.setAdapter(adapter);                                                            // Set the UI ListView widget to use the new ArrayAdapter

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {                        // Listen for clicks on items in the ListView
                initiateViewBarcode(barcodes.get(position));                                        // Begin a new ViewBarcodeActivity for the clicked on barcode number
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){                  // When an activity returns a result
        if(requestCode == 1){                                                                       // If the activity returns requestCode == 1, I.E. the result is returned by ViewBarcodeActivity
            updateView();                                                                           // Update the view to reflect any changes made by ViewBarcodeActivity
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                                 // Create Android options menu
        getMenuInflater().inflate(R.menu.menu_view_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           // Options menu items
        int id = item.getItemId();

        if(id == R.id.clear_history){                                                               // Clear history button pressed
            if(history.clearHistory()) {                                                            // Clear the history file
                Toast aToast = Toast.makeText(this, "History cleared.", Toast.LENGTH_LONG);         // Create an appropriate toast message
                aToast.show();                                                                      // Display the toast message
                updateView();                                                                       // Update the view to reflect the fact that the history has been cleared
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
