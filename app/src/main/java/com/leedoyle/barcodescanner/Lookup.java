package com.leedoyle.barcodescanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public abstract class Lookup {

    protected String appID;                                                                         // To hold the unique ID assigned for your application for a remote service API

    protected HttpClient client;                                                                    // Used to execute Http requests
    protected HttpGet request;                                                                      // Used to hold the Http request
    protected HttpResponse response;                                                                // Used to hold the Http response

    public abstract ArrayList<Product> lookup(Barcode b, String region) throws Exception;                               // Perform a lookup for a given Barcode
    protected abstract ArrayList<Product> parseResult(String responseXML) throws XmlPullParserException, IOException;   // Parse an XML response for Products
    protected abstract String getFormattedRegion(String region);                                                        // Returns the correct String for identifying the specified region for the sites API
}
