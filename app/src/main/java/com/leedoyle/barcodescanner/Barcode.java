package com.leedoyle.barcodescanner;

import android.os.Bundle;

import com.google.zxing.integration.android.IntentResult;
import java.io.Serializable;

public class Barcode implements Serializable{

    private String number;                                                                          // String variable to store the barcode number
    private String format;                                                                          // String variable to store the barcode format

    /**
     * Creates a default Barcode object with attributes initialised to empty strings
     */
    public Barcode(){
        this.number = "";                                                                           // Initialises the barcode number to an empty string
        this.format = "";                                                                           // Initialises the barcode format to an empty string
    }

    /**
     * Creates a new Barcode object with attributes initialised
     * from a ZXing IntentResult
     * @param scanResult the IntentResult
     */
    public Barcode(IntentResult scanResult){
        number = scanResult.getContents();                                                          // Initialises the barcode number to the barcode number contained within scanResult
        format = scanResult.getFormatName();                                                        // Initialises the barcode format to the barcode format contained within scanResult
    }

    /**
     *  Creates a new Barcode object with the specified number and format
     *  @param number the barcode number
     *  @param format the barcode format
     */
    public Barcode(String number, String format){
        this.number = number;                                                                       // Initialises the barcode number to number, the value specified in the constructors argument
        this.format = format;                                                                       // Initialises the barcode format to format, the value specified in the constructors argument
    }

    /**
     * Returns the number attribute of the Barcode object
     * @return  the barcode number
      */
    public String getNumber(){
        return number;
    }

    /**
     * Returns the format attribute of the Barcode object
     * @return the barcode format
     */
    public String getFormat(){
        return format;
    }

    /**
     * Returns the simplified barcode format type
     * @return the simplified barcode format
     */
    public String getSimpleFormat(){
        if(getFormat().equals("EAN-8") || getFormat().equals("EAN-13")) return "EAN";               // Check the barcode format and return a String containing the simplified format type
        else if(getFormat().equals("UPC_A") || getFormat().equals("UPC_E")) return "UPC";
        else return null;
    }

    /**
     * Returns a Bundle containing the serialized Barcode object
     * @return  the bundle containing the serialized Barcode object
     */
    public Bundle getBundledBarcode(){
        Bundle bundle = new Bundle();                                                               // Create a new Bundle object to store this barcode
        bundle.putSerializable("Barcode", this);                                                    // Serialise this barcode object and add it to bundle, referenced by the string "Barcode"
        return bundle;                                                                              // Return the created Bundle object
    }

    @Override
    public boolean equals(Object object){                                                           // Override equals method
        if(object != null && object instanceof Barcode){                                            // If object is not null and is of type Barcode
            return this.getNumber().equals(((Barcode) object).getNumber());                         // Return true if objects number attribute is equal to this Barcode objects number attribute, otherwise return false
        } else return false;                                                                        // Otherwise return false
    }

    @Override
    public int hashCode(){  return Integer.parseInt(this.getNumber()); }                            // returns the barcode number as the hash code for this class

    @Override
    public String toString(){                                                                       // Override toString()
        return getNumber();                                                                         // Return the barcode number
    }

}
