package com.leedoyle.barcodescanner;

import android.util.Log;

import org.apache.commons.codec.binary.Base64;
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
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AmazonLookup extends Lookup {
    private String secretKey = "*";                          // Amazon secret key used to calculate the Hmac signature
    private String appID = "*";                                                  // Store the unique Amazon identification key

    public AmazonLookup(){                                                                          // Constructor
        client = new DefaultHttpClient();                                                           // Create a new HttPClient object used to execute Http requests
        request = new HttpGet();                                                                    // Create a new HttpGet object used to hold the Http request
    }

    @Override
    public ArrayList<Product> lookup(Barcode b, String region) throws Exception {                   // Perform a remote lookup of the specified barcode and region on Amazon
        String canonicalizedString = getCanonicalizedRequest(b);                                    // Get the canonicalized request parameters
        String requestToSign = "GET" + "\n"                                                         // The full canonicalized request String to sign
                                + "webservices.amazon.co.uk" + "\n"
                                + "/onca/xml" + "\n"
                                + canonicalizedString;
        Log.d("Unsigned String", requestToSign);
        String hmacSignature = encode(generateHMAC(requestToSign, secretKey));                      // Get the encoded Hmac signature
        String requestURL = "http://webservices.amazon" + getFormattedRegion(region) +              // Construct the full hmac signed request URL
                            "/onca/xml?" + canonicalizedString
                            + "&Signature=" + hmacSignature;
        String responseXML = "";                                                                    // A String to store the XML response to the request
        ArrayList<Product> productList;                                                             // Create a new ArrayList to store the products parsed from the XML response


        request.setURI(new URI((requestURL)));                                                      // Set the HttpGet request, inserting the unique Amazon ID
                                                                                                    // as well as the selected region and the barcode number
                                                                                                    // to be searched for

        response = client.execute(request);                                                         // Execute the request and store the HttpResponse
        responseXML = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();  // Read the XML returned with the HttpResponse and store it in a String
        Log.d("RESTRequest ", request.getURI().toString());                                         // Log the Http Get request made to the console for debugging
        Log.d("RESTResponse ", responseXML);                                                        // Log the XML taken from the request response to the console for debugging
        productList = parseResult(responseXML);                                                     // Parse the resulting XML to extract the required information

        return productList;                                                                         // Return the list of products
    }

    private String getCanonicalizedRequest(Barcode b){                                              // Forms a request String standardised as required by Amazons API, ready for signing
        StringBuffer canonicalizedString = new StringBuffer();                                      // StringBuffer used to hold our standardised request String as we form it
        SortedMap<String, String> parameters = new TreeMap<String, String>();                                    // Map to hold the desired request parameters to add to the request String
        parameters.put("AWSAccessKeyId", appID);                                                    // Add each of the required parameters to the Map
        parameters.put("AssociateTag", "dummyTag");
        parameters.put("Operation", "ItemLookup");
        parameters.put("ItemId", b.getNumber());
        parameters.put("ResponseGroup", "Images,ItemAttributes,Offers");
        parameters.put("SearchIndex", "All");
        parameters.put("IdType", b.getSimpleFormat());
        parameters.put("Timestamp", getTimeStamp());

        Iterator<Map.Entry<String,String>> iterator = parameters.entrySet().iterator();             // Create an Iterator for the set of pairs in the parameter Map
        while(iterator.hasNext()){                                                                  // Loop through each element with the iterator
            Map.Entry<String, String> pair = iterator.next();                                       // Create a String pair for each element in the iterator
            canonicalizedString.append(encode(pair.getKey()));                                      // Encode and append the key (I.e. the parameter) to the request String
            canonicalizedString.append("=");                                                        // Append an equals between the Key and the Value
            canonicalizedString.append(encode(pair.getValue()));                                    // Encode and append the value (I.e. the parameter value) to the request String
            if(iterator.hasNext()){                                                                 // If there are more elements
                canonicalizedString.append("&");                                                    // Append an ampersand between the parameters
            }
        }
        return canonicalizedString.toString();                                                      // Return the standardised request String
    }

    private String encode(String s){                                                                // Encodes the given String as required by the Amazon API
        String result = "";                                                                         // Empty String to store the encoded result
        try{
            result = URLEncoder.encode(s, "UTF-8").replace("*", "%20")                              // Encode and store the given String, replacing "*", "+" and "%7E"
                                                  .replace("+", "%2A")                              // with "%20", "%2A" and "~" respectively
                                                  .replace("%7E", "~");
        }
        catch(Exception e){                                                                         // If an exception occurs
            Log.d("Encoder", "Error encoding string");                                              // Log the error
        }
        return result;                                                                              // Return the result
    }

    private String getTimeStamp(){                                                                  // Return a timestamp of the current time, formatted as required for Amazon API requests
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");                   // Create a new DateFormat object defining the date format as required by the Amazon request
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));                                        // Sets the timezone to GMT as recommended in the Amazon API developer documentation
        return dateFormat.format(Calendar.getInstance().getTime());                                 // Return the correctly formatted time stamp as required in the Amazon request
    }

    private String generateHMAC(String dataToSign, String key ) throws SignatureException{          // Given a correctly formatted, standardised Amazon request String and a valid Amazon secret key
                                                                                                    // generates a HMAC signature as specified by Amazons API
        try{
            SecretKeySpec hmacKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");         // Create a secret key specification using the specified secret key and the HMAC-SHA256 algorithmn
            Mac mac = Mac.getInstance("HmacSHA256");                                                // Create a new MAC instance providing the HMAC-SHA256 algorithmn
            mac.init(hmacKey);                                                                      // Initialises the MAC instance with the secret key specification
            byte[] byteHmac = mac.doFinal(dataToSign.getBytes("UTF-8"));                            // Compute the HMAC digest and store it in a byte array
            Base64 encoder = new Base64();                                                          // Create a new Base64 codec
            return new String(encoder.encode(byteHmac));                                            // Encode the hmac binary byte array into character data
        }
        catch(Exception e){                                                                         // If an exception occurs
            throw new SignatureException("Failed to calculate Hmac: " + e.toString());              // Throw a SignatureException error with a message and stack trace
        }
    }

    @Override
    protected ArrayList<Product> parseResult(String responseXML) throws XmlPullParserException, IOException {   // Parse Amazons response XML, returning an array of Products containing the obtained information
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();                          // Create a new XmlPullParserFactory to manage creation of new instances of XmlPullParser
        factory.setNamespaceAware(true);                                                            // Set support for XML namespaces
        XmlPullParser pullParser = factory.newPullParser();                                         // Create a new XmlPullParser using the factory
        ArrayList<Product> productList = new ArrayList<>();                                         // Create a new ArrayList to store the products that are parsed from the response

        pullParser.setInput(new StringReader(responseXML));                                         // Set the input source for the parser to the response XML String obtained in the request lookup
        int eventType = pullParser.getEventType();                                                  // Integer storing the current event type of the XmlPullParser

        String text = null;                                                                         // String which will be used to reference the text between two XML tags

        Product aProduct = new Product();                                                           // Create a new Product object to store the details parsed from the XML

        Log.d("Parser ", "Attempting to parse");                                                    // Debug log
        parsing : while(eventType != XmlPullParser.END_DOCUMENT){                                   // Loop through the entire XML document.
                                                                                                    // within the nested switch case statement below
            String tag = pullParser.getName();                                                      // String to reference the current XML tag we're working with

            switch(eventType){                                                                      // Switch statement for each of the XmlPullParser event types that we are interested in
                case XmlPullParser.START_TAG:                                                       // On reaching an opening tag
                    if(tag.equals("Item")){                                                         // If the tag is <item>
                        Log.d("Parser ", "Creating new product for item");                          // Debug log
                        aProduct = new Product();                                                   // Create a new Product object which we will use to hold the information for this item
                    }
                    else if(tag.equals("MediumImage") && aProduct.getImageURL().equals("")){        // If the tag is <MediumImage> and an image url for the Product has not already been obtained
                        int eventType2 = pullParser.next();                                         // Another integer to store the current event type for identifying the nested tags
                        nestedElement : while(eventType2 != XmlPullParser.END_DOCUMENT){            // Loop through the remaining document
                            String tag2 = pullParser.getName();                                     // Reference to the current XML tag we're working with
                            switch(eventType2){                                                     // Switch statement for each of the event types we're interested in
                                case XmlPullParser.TEXT:                                            // On reaching the text after an opening tag
                                    text = pullParser.getText();                                    // Store the text
                                    break;
                                case XmlPullParser.END_TAG:                                         // On reaching an end tag
                                    if(tag2.equals("URL")){                                         // If the tag is </URL>, the URL of the MediumImage thumbnail
                                        aProduct.setImageURL(text);                                 // Set the ImageURL attribute of our Product object
                                        break nestedElement;                                        // Leave the loop having obtained an image URL
                                    }
                                    break;
                            }
                            eventType2 = pullParser.next();                                         // Get the next event type
                        }
                    }
                    break;
                case XmlPullParser.TEXT:                                                            // On reaching the text after the opening tag
                    text = pullParser.getText();                                                    // Store the text between the previous and next tags
                    break;
                case XmlPullParser.END_TAG:                                                         // On reaching an end tag
                    if(tag.equals("DetailPageURL")){                                                // If the tag is </DetailPageURL>, the URL of the item on Amazon
                        aProduct.setUrl(text);                                                      // Set the url attribute of our Product object to the text within the tags
                    }
                    else if(tag.equals("Title")){                                                   // If the tag is </Title>, the title/name of the item
                        aProduct.setName(text);                                                     // Set the name attribute of our Product object to the text within the tags
                    }
                    else if(tag.equals("FormattedPrice")){                                          // If the tag is </FormattedPrice>, the price of the item
                        aProduct.setPrice(text);                                                    // Set the price attribute of our Product object to the text within the tags
                    }
                    else if(tag.equals("Item")){                                                    // If the tag is </Item>, indicating the end of the data for this item
                        if(aProduct.getPrice().equals("")) aProduct.setPrice("Currently Unavailable");  // If no price was obtained for the item (I.e. it is unavailable), set text informing the user
                        productList.add(aProduct);                                                  // Add the modified Product object to the ArrayList of Products
                        Log.d("Parser ", "Done parsing item");                                      // Debug log
                        Log.d("Parser ", aProduct.getName());                                       // Debug log
                        Log.d("Parser ", aProduct.getPrice());                                      // Debug log
                        Log.d("Parser ", aProduct.getUrl());                                        // Debug log
                        Log.d("Parser ", aProduct.getImageURL());                                   // Debug log
                        break;
                    }
                    break;
            }
            eventType = pullParser.next();                                                          // Get the next event type
        }
        Log.d("Parser ", "List contains " + String.valueOf(productList.size()) + " items.");        // Debug log
        Log.d("Parser ", "Completed parsing file");                                                 // Debug log
        return productList;                                                                         // Return the list of products
    }

    @Override
    protected String getFormattedRegion(String region) {                                            // Takes the region string provided by the UI
        switch(region){                                                                             // and sets the request domain appropriately
            case "UK":
                return ".co.uk";
            case "US":
                return ".com";
            case "Germany":
                return ".de";
            case "India":
                return ".in";
            default:
                return null;
        }
    }
}
