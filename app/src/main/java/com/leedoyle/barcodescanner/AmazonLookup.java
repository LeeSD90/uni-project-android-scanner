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
    private String secretKey = "8DMnkFHY2n6DUDthFFHGLNF3RrhW2BPNdB9lohTi";                     // Amazon secret key used to calculate the Hmac signature
    private String appID = "AKIAJHFTUSH2JUTQ7X6A";                                             // Store the unique Amazon identification key

    public AmazonLookup(){
        client = new DefaultHttpClient();                                                                   // Create a new HttPClient object used to execute Http requests
        request = new HttpGet();                                                                            // Create a new HttpGet object used to hold the Http request
    }

    @Override
    public ArrayList<Product> lookup(Barcode b, String region) throws Exception {
        String canonicalizedString = getCanonicalizedRequest(b);                                            // Get the canonicalized request parameters
        String requestToSign = "GET" + "\n"                                                                 // The full canonicalized request String to sign
                                + "webservices.amazon.co.uk" + "\n"
                                + "/onca/xml" + "\n"
                                + canonicalizedString;
        Log.d("Unsigned String", requestToSign);
        String hmacSignature = encode(generateHMAC(requestToSign, secretKey));                              // Get the encoded Hmac signature
        String requestURL = "http://webservices.amazon" + getFormattedRegion(region) +                      // Construct the full hmac signed request URL
                            "/onca/xml?" + canonicalizedString
                            + "&Signature=" + hmacSignature;
        String responseXML = "";                                                                            // A String to store the XML response to the request
        ArrayList<Product> productList;                                                                     // Create a new ArrayList to store the products parsed from the XML response


        request.setURI(new URI((requestURL)));  // Set the HttpGet request, inserting the unique Amazon ID
                                                // as well as the selected region and the barcode number
                                                // to be searched for

        response = client.execute(request);                                                                   // Execute the request and store the HttpResponse
        responseXML = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine();// Read the XML returned with the HttpResponse and store it in a String
        Log.d("RESTRequest ", request.getURI().toString());                                                       // Log the Http Get request made to the console for debugging
        Log.d("RESTResponse ", responseXML);                                                                      // Log the XML taken from the request response to the console for debugging
        productList = parseResult(responseXML);                                                               // Parse the resulting XML to extract the required information

        return productList;                                                                                 // Return the list of products
    }

    private String getCanonicalizedRequest(Barcode b){
        StringBuffer canonicalizedString = new StringBuffer();
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("AWSAccessKeyId", appID);
        params.put("AssociateTag", "dummyTag");
        params.put("Operation", "ItemLookup");
        params.put("ItemId", b.getNumber());
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");
        params.put("SearchIndex", "All");
        params.put("IdType", b.getSimpleFormat());
        params.put("Timestamp", getTimeStamp());

        Iterator<Map.Entry<String,String>> iterator = params.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, String> pair = iterator.next();
            canonicalizedString.append(encode(pair.getKey()));
            canonicalizedString.append("=");
            canonicalizedString.append(encode(pair.getValue()));
            if(iterator.hasNext()){
                canonicalizedString.append("&");
            }
        }
        return canonicalizedString.toString();
    }

    private String encode(String s){
        String result = "";
        try{
            result = URLEncoder.encode(s, "UTF-8").replace("*", "%20")
                                                  .replace("+", "%2A")
                                                  .replace("%7E", "~");
        }
        catch(Exception e){
            Log.d("Encoder", "Error encoding string");
        }
        return result;
    }

    private String getTimeStamp(){
        String timeStamp;                                                               // String to hold the time stamp
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");       // Create a new DateFormat object defining the date format as required by the Amazon request
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));                            // Sets the timezone to GMT as recommended in the Amazon API developer documentation
        timeStamp = dateFormat.format(Calendar.getInstance().getTime());                // Get the current time and format it using the above DateFormat
        return timeStamp;                                                               // Return the correctly formatted time stamp as required in the Amazon request
    }

    private String generateHMAC(String dataToSign, String key ) throws SignatureException{
        try{
            SecretKeySpec hmacKey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);
            byte[] byteHmac = mac.doFinal(dataToSign.getBytes("UTF-8"));
            Base64 encoder = new Base64();
            return new String(encoder.encode(byteHmac));
        }
        catch(Exception e){
            throw new SignatureException("Failed to calculate Hmac: " + e.toString());
        }
    }

    @Override
    protected ArrayList<Product> parseResult(String responseXML) throws XmlPullParserException, IOException {
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
                    if(tag.equals("Item")){                                                     // If the tag is <item>
                        Log.d("Parser ", "Creating new product for item");                      // Debug log
                        aProduct = new Product();                                               // Create a new Product object which we will use to hold the information for this item
                    }
                    else if(tag.equals("MediumImage") && aProduct.getImageURL().equals("")){   // If the tag is <MediumImage> and an image url for the Product has not already been obtained
                        int eventType2 = pullParser.next();
                        nestedElement : while(eventType2 != XmlPullParser.END_DOCUMENT){
                            String tag2 = pullParser.getName();
                            switch(eventType2){
                                case XmlPullParser.TEXT:
                                    text = pullParser.getText();
                                    break;
                                case XmlPullParser.END_TAG:
                                    if(tag2.equals("URL")){
                                        aProduct.setImageURL(text);
                                        break nestedElement;
                                    }
                                    break;
                            }
                            eventType2 = pullParser.next();
                        }
                    }
                    break;
                case XmlPullParser.TEXT:                                                        // On reaching the text after the opening tag
                    text = pullParser.getText();                                                // Store the text between the previous and next tags
                    break;
                case XmlPullParser.END_TAG:                                                     // On reaching an end tag
                    if(tag.equals("DetailPageURL")){                                            // If the tag is </DetailPageURL>, the URL of the item on Amazon
                        aProduct.setUrl(text);                                                  // Set the url attribute of our Product object to the text within the tags
                    }
                    else if(tag.equals("Title")){                                               // If the tag is </Title>, the title/name of the item
                        aProduct.setName(text);                                                 // Set the name attribute of our Product object to the text within the tags
                    }
                    else if(tag.equals("FormattedPrice")){                                      // If the tag is </FormattedPrice>, the price of the item
                        aProduct.setPrice(text);                                                // Set the price attribute of our Product object to the text within the tags
                    }
                    else if(tag.equals("Item")){                                                // If the tag is </Item>, indicating the end of the data for this item
                        if(aProduct.getPrice().equals("")) aProduct.setPrice("Currently Unavailable");
                        productList.add(aProduct);                                              // Add the modified Product object to the ArrayList of Products
                        Log.d("Parser ", "Done parsing item");                                  // Debug log
                        Log.d("Parser ", aProduct.getName());                                   // Debug log
                        Log.d("Parser ", aProduct.getPrice());                                  // Debug log
                        Log.d("Parser ", aProduct.getUrl());                                    // Debug log
                        Log.d("Parser ", aProduct.getImageURL());                               // Debug log
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
        switch(region){
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
