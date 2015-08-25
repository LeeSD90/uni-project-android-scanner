package com.leedoyle.barcodescanner;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);                                                                   // Creating with null instead of savedInstanceState ensures a fresh activity is created
        setContentView(R.layout.activity_main_menu);
        initiateIntentIntegrator();
    }

    @Override
    public void onActivityResult(int request, int result, Intent intent) {                      // Called when a result is returned from the IntentIntegrators scan activity
        switch(result) {
            case -1:                                                                                // If the scan was successful and has a result
                initiateViewBarcode(IntentIntegrator.parseActivityResult(request, result, intent)); // Passes the IntentResult to initiateViewBarcode()
                break;
            case 0:                                                                                 // If the scan was cancelled by the user
                Toast aToast = Toast.makeText(getApplicationContext(), "Scanning cancelled",        // Create a Toast with an appropriate message
                        Toast.LENGTH_LONG);
                aToast.show();                                                                      // Display the Toast
                this.finish();                                                                      // Finish the activity
                break;
            default:                                                                                // If there was an error scanning
                aToast = Toast.makeText(getApplicationContext(), "Error decoding barcode -"         // Set the toast text to an appropriate message
                        + " supports UPC-A/E and EAN-8/13 formats only", Toast.LENGTH_LONG);
                aToast.show();                                                                      // Display the Toast
                this.finish();                                                                      // Finish the activity
        }

    }

    private void initiateIntentIntegrator(){                                                    // Attempts to initiate a ZXing IntentIntegrator scan
        IntentIntegrator integrator = new IntentIntegrator(this);                               // Create a new ZXing IntentIntegrator
        integrator.setPrompt("Look at a barcode through the viewfinder to scan it");            // Set the user prompt message
        integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);                           // Initiate a scan activity to read in a product barcode
    }

    private void initiateViewBarcode(IntentResult scanResult){
            Barcode b = new Barcode(scanResult);                                                // Create a new Barcode object from the IntentResult
            Intent i = new Intent(this, ViewBarcodeActivity.class);                             // Create a new Intent using the ViewBarCodeActivity class
            i.putExtras(b.getBundledBarcode());                                                 // Package the Barcode object into a bundle, and add it to the Intent as an extra
            this.startActivity(i);                                                              // Begin the ViewBarcodeActivity
            this.finish();                                                                      // End this Activity
    }


}
