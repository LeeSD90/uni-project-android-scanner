package com.leedoyle.barcodescanner;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;

/*
The intent starting this activity should also contain a serialized Barcode object, using the key "Barcode"
 */
public class ViewBarcodeActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Barcode b;
    private BarcodeHistory history;
    private Button saveButton, removeButton, findOnlineButton, nextButton, prevButton;
    private TextView productName, productPrice, productURL, resultText, listCountText;
    private Spinner searchSpinner, regionSpinner;
    private ImageView productImage;
    private ArrayList<Product> productList;
    private int slideIndex;
    private String selectedSearchItem, selectedRegionItem;
    private Bitmap bmp;
    private Boolean searchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bar_code);                                            // Set the view to the XML layout we've defined

        productList = new ArrayList<Product>();                                                     // Create a new ArrayList to store the Products retrieved for the Barcode
        slideIndex = 0;                                                                             // Set slideIndex to 0 initially to ensure that we start at the first product
                                                                                                    // in the slide

        productName = (TextView) findViewById(R.id.productName);                                    // Set the Button and View objects to reference the UI widgets
        productPrice = (TextView) findViewById(R.id.productPrice);
        productURL = (TextView) findViewById(R.id.productURL);
        resultText = (TextView) findViewById(R.id.resultText);
        listCountText = (TextView) findViewById(R.id.listCountText);
        productImage = (ImageView) findViewById(R.id.productImageView);
        saveButton = (Button) findViewById(R.id.saveButton);
        removeButton = (Button) findViewById(R.id.removeButton);
        findOnlineButton = (Button) findViewById(R.id.findOnlineButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        prevButton = (Button) findViewById(R.id.prevButton);
        searchSpinner = (Spinner) findViewById(R.id.searchSpinner);
        regionSpinner = (Spinner) findViewById(R.id.regionSpinner);


        saveButton.setOnClickListener(this);                                                        // Set the click listener for UI Elements
        removeButton.setOnClickListener(this);
        findOnlineButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        searchSpinner.setOnItemSelectedListener(this);
        regionSpinner.setOnItemSelectedListener(this);

        nextButton.setVisibility(View.GONE);                                                        // Set the initial visibility of the product search
        prevButton.setVisibility(View.GONE);                                                        // UI elements.
        productImage.setVisibility(View.GONE);
        productURL.setVisibility(View.GONE);
        productPrice.setVisibility(View.GONE);
        productName.setVisibility(View.GONE);
        resultText.setVisibility(View.GONE);
        listCountText.setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,                 // Create an ArrayAdapter for the dropdown list of sites to search
                R.array.search_online_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);            // Specify the type of resource to use for the drop down view
        searchSpinner.setAdapter(adapter1);                                                         // Set the UI dropdown list widgets adapter

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,                 // Create an ArrayAdapter for the dropdown list of regions to search
                R.array.search_region_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);            // Specify the type of resource to use for the drop down view
        regionSpinner.setAdapter(adapter2);                                                         // Set the UI dropdown list widgets adapter

        updateView();                                                                               // Update the view to display the barcode and buttons
    }

    @Override
    public void onClick(View v) {                                                                   // When the UI is clicked
        switch(v.getId()) {                                                                         // Switch to identify which UI element was clicked
            case R.id.saveButton:                                                                   // Save Barcode button
                saveBarcode();                                                                      // Add the Barcode to the history file
                break;
            case R.id.removeButton:                                                                 // Remove Barcode button
                removeBarcode();                                                                    // Remove the Barcode from the history file
                break;
            case R.id.findOnlineButton:                                                             // Find online button
                searchVisible = false;                                                              // Set searchVisible to false so that the product slider will not be drawn
                updateView();                                                                       // Update the view
                lookup();                                                                           // Perform a product search online using the Barcode
                break;
            case R.id.nextButton:                                                                   // Next button for product slider
                if(slideIndex >= productList.size() - 1) slideIndex = 0;                            // Reset slideIndex to 0 if we have reached the end of productList
                else slideIndex++;                                                                  // otherwise increment slideIndex
                updateView();                                                                       // update the view to show the new slide
                break;
            case R.id.prevButton:                                                                   // Previous button for product slider
                if(slideIndex <= 0) slideIndex = productList.size() - 1;                            // If slideIndex has reached 0 then set it to the index of the last element in productList
                else slideIndex--;                                                                  // otherwise decrement slideIndex
                updateView();                                                                       // update the view to show the new slide
                break;
        }
    }

    private void updateView(){                                                                      // Update the UI view to reflect any changes made
        history = new BarcodeHistory(getApplicationContext());                                      // Create a new BarcodeHistory object with the current context

        if(getIntent().getExtras().getSerializable("Barcode") != null){                             // Check to ensure a Barcode object was passed with the intent
            b = (Barcode) getIntent().getExtras().getSerializable("Barcode");                       // Try to retrieve the barcode passed with the intent
            TextView resultFormat = (TextView) findViewById(R.id.resultFormat);                     // References to activity_var_bar_code widgets
            TextView contentFormat = (TextView) findViewById(R.id.contentFormat);
            resultFormat.setText(b.getFormat());                                                    // Modify widgets to display the scan results
            contentFormat.setText(b.getNumber());

            if(history.getHistory().contains(b)){                                                   // If the history file contains the Barcode we're viewing
                removeButton.setEnabled(true);                                                      // Enable the remove barcode button
                removeButton.getBackground().setColorFilter(null);                                  // Remove the color filter from the remove barcode button
                saveButton.setEnabled(false);                                                       // Disable the save barcode button
                saveButton.setText("Saved");                                                        // Set the text indicating the barcode is already saved to file
                saveButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.DARKEN);      // Set the color filter for the save barcode button, greying it out
            }
            else{                                                                                   // If the history file does not contain the Barcode we're viewing
                saveButton.setEnabled(true);                                                        // Enable the save barcode button
                saveButton.setText("Save Barcode");                                                 // Set the save button text indicating the barcode can be saved
                saveButton.getBackground().setColorFilter(null);                                    // Remove the color filter from the save barcode button
                removeButton.setEnabled(false);                                                     // Disable the remove barcode button
                removeButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.DARKEN);    // Set the color filter for the remove barcode button, greying it out

            }
        }
        else this.finish();                                                                         // If no Barcode object was passed with the intent, finish the activity

        if(searchVisible){
            if(productList.size() != 0) {                                                           // If the productList contains items for display

                Thread thread = new Thread(new Runnable() {
                    public void run() {                                       // Create a new Thread  (Generally required for network operations in Android)
                        try {
                            URL url = new URL(productList.get(slideIndex).getImageURL());           // Create a new URL object from the imageURL attribute of the currently selected item in productList
                            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());// Assign a bitmap of the image at the URL to bmp
                        } catch (Exception e) {
                            Log.e("URL bitmap ", "Error obtaining image bitmap - " + e.toString()); // Display error message
                        }
                    }
                });
                thread.start();                                                                     // Start the thread
                try {
                    thread.join();                                                                  // Wait for the thread to complete
                    productImage.setImageBitmap(bmp);                                               // Display the selected product image bitmap on the UIs ImageView widget
                    productName.setText(Html.fromHtml("<b>Name:</b> " + productList.get(slideIndex).getName() + "<br />"));     // Set the product name, price and url UI Text widgets to those of the
                    productPrice.setText(Html.fromHtml("<b>Price:</b> " + productList.get(slideIndex).getPrice() + "<br />"));  // selected product
                    productURL.setText(Html.fromHtml("<b>URL:</b> " + productList.get(slideIndex).getUrl()));
                    listCountText.setText("Result " + (slideIndex + 1) + " of " + productList.size());  // Set the text to display which product in the list we are viewing
                    nextButton.setVisibility(View.VISIBLE);                                         // Set the visibility of the search
                    prevButton.setVisibility(View.VISIBLE);                                         // product slider UI elements
                    productImage.setVisibility(View.VISIBLE);
                    productURL.setVisibility(View.VISIBLE);
                    productPrice.setVisibility(View.VISIBLE);
                    productName.setVisibility(View.VISIBLE);
                    listCountText.setVisibility(View.VISIBLE);
                    findOnlineButton.setText("Search again");
                    resultText.setVisibility(View.GONE);                                            // Stop drawing the no result text
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{                                                                                   // If the productList does not contain items for display
                nextButton.setVisibility(View.GONE);                                                // Set the visibility of the product search
                prevButton.setVisibility(View.GONE);                                                // UI elements, hiding them
                productImage.setVisibility(View.GONE);
                productURL.setVisibility(View.GONE);
                productPrice.setVisibility(View.GONE);
                productName.setVisibility(View.GONE);
                resultText.setVisibility(View.GONE);
                listCountText.setVisibility(View.GONE);
                resultText.setText("No matching item found on the selected site(s).");              // Set the result text informing the user
                findOnlineButton.setText("Search again");                                           // Update the search button text
                resultText.setVisibility(View.VISIBLE);                                             // Set the visibility of the result text widget
            }
        }

    }

    private void removeBarcode(){                                                                   // Removes this activities barcode from the internal history file
        history.removeFromHistory(b);                                                               // Remove the barcode object b from the history file
        Toast aToast = Toast.makeText(getApplicationContext(), "Barcode removed from file", Toast.LENGTH_SHORT);    // Create a Toast object with a message confirming the barcode is removed
        aToast.show();                                                                              // Display the toast
        updateView();                                                                               // Update the view to reflect the changes
    }

    private void saveBarcode(){                                                                     // Adds this activities barcode to the internal history file
        history.addToHistory(b);                                                                    // Add the barcode object b to the history file
        Toast aToast = Toast.makeText(getApplicationContext(), "Barcode saved to file.", Toast.LENGTH_SHORT);   // Create a Toast object with a message confirming the barcode has been added
        aToast.show();                                                                              // Display the toast
        updateView();                                                                               // Update the view to reflect the changes
    }

    private void lookup(){                                                                          // Performs a search online using this activities barcode and the selected online shops
        Thread lookupThread = new Thread(new Runnable(){
            public void run(){                                         // Create a new Thread  (Generally required for network operations in Android)
                try {
                    EbayLookup ebay = new EbayLookup();                                             // Create a new EbayLookup object
                    AmazonLookup amazon = new AmazonLookup();                                       // Create a new Amazonlookup object
                    switch(selectedSearchItem){
                        case "Ebay":
                            productList = ebay.lookup(b, selectedRegionItem);                       // Obtain results from Ebay for the current barcode
                            break;
                        case "Amazon":
                            productList = amazon.lookup(b, selectedRegionItem);                     // Obtain results from Amazon for the current barcode
                            break;
                        case "All":
                            productList = new ArrayList<>();                                        // Empty the product list for the new search
                            ArrayList<Product> amazonList = amazon.lookup(b, selectedRegionItem);   // Obtain results from Amazon for the current barcode
                            ArrayList<Product> ebayList = ebay.lookup(b, selectedRegionItem);       // Obtain results from Ebay for the current barcode
                            productList.addAll(amazonList);                                         // Add the Amazon results to the list
                            productList.addAll(ebayList);                                           // Add the Ebay results to the list
                            break;
                    }
                }
                catch(Exception e){
                    runOnUiThread(new Runnable() {                                                                  // Run this code on the UI thread as it updates the UI
                        @Override
                        public void run() {
                            Toast aToast = Toast.makeText(getApplicationContext(), "Error performing lookup" +
                                                        "- Do you have internet access?", Toast.LENGTH_LONG);   // Create an appropriate Toast message
                            aToast.show();                                                          // Display the Toast
                        }
                    });
                    Log.e("Lookup Error:", "Http connection error " + e.toString());                // Log an error message and stack trace
                }
            }
        });
        lookupThread.start();                                                                       // Start the thread
        try {
            lookupThread.join();                                                                    // Wait for thread to complete
            searchVisible = true;                                                                   // Set searchVisible to true, allowing the display slider to be drawn when the view is updated
            slideIndex = 0;                                                                         // Reset the slide index
            updateView();                                                                           // Update the view
        }
        catch(Exception e){                                                                         // If an exception occurs
            e.printStackTrace();                                                                    // Print a stack trace to the console
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {           // When the selected item in the list changes
        switch(parent.getId()){
            case R.id.searchSpinner:                                                                // Selected search site is changed
                selectedSearchItem = parent.getItemAtPosition(position).toString();                 // Set the selected search site to the selected item in the list
                break;
            case R.id.regionSpinner:                                                                // Selected region is changed
                selectedRegionItem = parent.getItemAtPosition(position).toString();                 // Set the selected region to the selected item in the list
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
