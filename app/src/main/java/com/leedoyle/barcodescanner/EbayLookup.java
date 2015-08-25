package com.leedoyle.barcodescanner;

import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;

public class EbayLookup extends Lookup{

    public EbayLookup(){
        appID = "LeeDoyle-497a-4f38-bbfe-8ccdab058eb6";                 // String to store our unique Ebay application identification key
        client = new DefaultHttpClient();                               // Create a new HttPClient object used to execute Http requests
        request = new HttpGet();                                        // Create a new HttpGet object used to hold the Http request
    }

    public ArrayList<Product> lookup(Barcode b, String region) throws Exception{
        String responseXML = "";                                            // A String to store the XML response to the request
        ArrayList<Product> productList = new ArrayList<>();                 // Create a new ArrayList to store the products parsed from the XML response

        request.setURI(new URI(("http://svcs.ebay.com/services/search/" +                                   // Set the HttpGet request, inserting the unique Ebay APP ID
                "FindingService/v1?OPERATION-NAME=findItemsByKeywords" +                                    // as well as the selected region and the barcode number
                "&SERVICE-VERSION=1.12.0&SECURITY-APPNAME=" + appID                                         // to be searched for
                + "&RESPONSE-DATA-FORMAT=XML&GLOBAL-ID=" + getFormattedRegion(region)
                + "&REST-PAYLOAD&paginationInput.entriesPerPage=50&keywords="
                + b.getNumber())));
        response = client.execute(request);                                                                   // Execute the request and store the HttpResponse
        responseXML = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();// Read the XML returned with the HttpResponse and store it in a String
        Log.d("Request ", request.getURI().toString());                                                       // Log the Http Get request made to the console for debugging
        Log.d("Response ", responseXML);                                                                      // Log the XML taken from the request response to the console for debugging
        productList = parseResult(responseXML);                                                               // Parse the resulting XML to extract the required information

        return productList;                                                                                 // Return the list of products
    }

    protected ArrayList<Product> parseResult(String responseXML) throws XmlPullParserException, IOException{
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();                      // Create a new XmlPullParserFactory to manage creation of new instances of XmlPullParser
        factory.setNamespaceAware(true);                                                        // Set support for XML namespaces
        XmlPullParser pullParser = factory.newPullParser();                                     // Create a new XmlPullParser using the factory
        ArrayList<Product> productList = new ArrayList<>();                                     // Create a new ArrayList to store the products that are parsed from the response

        pullParser.setInput(new StringReader(responseXML));                                     // Set the input source for the parser to the response XML String obtained in the request lookup
        int eventType = pullParser.getEventType();                                              // Integer storing the current event type of the XmlPullParser

        String text = null;                                                                     // String which will be used to reference the text between two XML tags

        Product aProduct = new Product();                                                       // Create a new Product object to store the details parsed from the XML

        Log.d("Parser ", "Attempting to parse");                                                // Debug log
        parsing : while(eventType != XmlPullParser.END_DOCUMENT){                               // Loop through the entire XML document.
                                                                                                // within the nested switch case statement below
            String tag = pullParser.getName();                                                  // String to reference the current XML tag we're working with

            switch(eventType){                                                                  // Switch statement for each of the XmlPullParser event types that we are interested in
                case XmlPullParser.START_TAG:                                                   // On reaching an opening tag
                    if(tag.equals("item")){                                                     // If the tag is <item>
                        aProduct = new Product();                                               // Create a new Product object which we will use to hold the information for this item
                    }
                    break;
                case XmlPullParser.TEXT:                                                        // On reaching the text after the opening tag
                    text = pullParser.getText();                                                // Store the text between the previous and next tags
                    break;
                case XmlPullParser.END_TAG:                                                     // On reaching an end tag
                    if(tag.equals("title")){                                                    // If the tag is </title>, the title of the product
                        aProduct.setName(text);                                                 // Set the name attribute of our Product object to the text within the tags
                        Log.d("Parser ", aProduct.getName());                                   // Debug log
                    }
                    else if(tag.equals("currentPrice")){                                        // If the tag is </currentPrice>, the current price of the product
                        aProduct.setPrice(text + " GBP");                                       // Set the price attribute of our Product object to the text within the tags
                        Log.d("Parser ", aProduct.getPrice());                                  // Debug log
                    }
                    else if(tag.equals("galleryURL")){                                          // If the tag is </galleryURL>, the URL of the items thumbnail image
                        aProduct.setImageURL(text);                                             // Set the imageURL attribute of our Product object to the text within the tags
                        Log.d("Parser ", aProduct.getImageURL());                               // Debug log
                    }
                    else if(tag.equals("viewItemURL")){                                         // If the tag is </viewItemURL>, the URL of the item on Ebay
                        aProduct.setUrl(text);                                                  // Set the url attribute of our Product object to the text within the tags
                        Log.d("Parser ", aProduct.getUrl());                                    // Debug log
                    }
                    else if(tag.equals("item")){                                                // If the tag is </item>, indicating the end of the data for this item
                        productList.add(aProduct);                                              // Add the modified Product object to the ArrayList of Products
                        Log.d("Parser ", "Done parsing item");                                  // Debug log
                        Log.d("Parser ", "Product name " + aProduct.getName());                 // Debug log
                        break;
                    }
                    break;
            }
            eventType = pullParser.next();                                                      // Get the next event type
        }
        Log.d("Parser ", "List contains " + String.valueOf(productList.size()) + " items.");    // Debug log
        Log.d("Parser ", "Completed parsing file");                                             // Debug log
        return productList;                                                                     // Return the list of products
    }

    @Override
    protected String getFormattedRegion(String region) {
        Log.d("Formatter ", region);
        switch(region){
            case "UK":
                return "EBAY-GB";
            case "US":
                return "EBAY-US";
            case "Germany":
                return "EBAY-DE";
            case "India":
                return "EBAY-IN";
            default:
                return null;
        }
    }
}
