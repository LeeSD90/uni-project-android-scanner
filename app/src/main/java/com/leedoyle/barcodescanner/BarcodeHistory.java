package com.leedoyle.barcodescanner;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class BarcodeHistory {

    private static String fileName = "BarcodeHistory";          // To reference the name of the file where the history will be saved

    Context c;                                                      // To reference the Context used when initialising the BarcodeHistory object
    ArrayList<Barcode> barcodeList = new ArrayList<Barcode>();            // To reference the list of Barcode objects

    /**
     * Create a new BarcodeHistory
     * object using the specified Context
     * @param c An Android Context
     */
    public BarcodeHistory(Context c){
        this.c = c;                                         // Sets the local Context variable c to reference the Context passed in the constructors argument
        barcodeList = this.getHistory();                    // Sets barcodeList to reference a list of Barcodes stored in in the barcode history file
    }

    /**
     *  Attempts to add the specified Barcode object to
     *  the barcode history file
     * @param b A Barcode Object
     * @return boolean
     */
    public boolean addToHistory(Barcode b){
        barcodeList = getHistory();                         // Retrieve the current ArrayList of Barcodes from the history file

        if(!barcodeList.contains(b)){                       // Check that the list does not already contain the specified Barcode object
            barcodeList.add(b);                             // Add the specified Barcode to the ArrayList
            this.saveHistory();                             // Save the history to file, writing the ArrayList to the barcode history file
            return true;                                    // return true indicating successful addition of the barcode
        }
        else return false;                                  // Otherwise return false indicating the specified Barcode already exists in the file
    }

    /**
     * Retrieves the contents of the barcode history file and returns
     * an ArrayList of the Barcode objects it reads
     * @return ArrayList<Barcode>
     */
    public ArrayList<Barcode> getHistory(){
        ArrayList<Barcode> bList;                                                   // Create a new ArrayList to store the Barcode objects
        try{
            FileInputStream fileInStream = c.openFileInput(fileName);               // Open a new FileInputStream using the barcode history filename
            ObjectInputStream objectInStream = new ObjectInputStream(fileInStream); // Open a new ObjectInputStream for the new FileInputStream, allowing us to read Java Objects through the stream
            bList = (ArrayList<Barcode>) objectInStream.readObject();               // Read the Barcode list from the file into the local ArrayList
            objectInStream.close();                                                 // Close the ObjectInputStream
            }
            catch (Exception e) {                                                   // If an exception occurs
                Log.e("File ", "Error loading or reading the file" + e);            // Log an error message and the exception
                return new ArrayList<>();                                           // Return an empty ArrayList
            }
        return bList;                                                               // If successful, return the ArrayList containing the barcodes
    }

    /**
     * Attempts to write the ArrayList barcodeList to the barcode history file
     * @return boolean
     */
    private boolean saveHistory(){
        try{
            FileOutputStream fileOutStream = c.openFileOutput(fileName, c.MODE_PRIVATE);    // Open a new FileOutputStream to the barcode history file using the MODE_PRIVATE file
                                                                                            // creation mode, ensuring the file is stored on internal storage and accessible only by the calling application
            ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);     // Open a new ObjectOutputStream for the new FileOutputStream, allowing us to write serialized Java Objects through the stream

            try{
                objectOutStream.writeObject(barcodeList);                                   // Write the barcodeList to the barcode history file
                objectOutStream.close();                                                    // Close the ObjectOutputStream
                return true;                                                                // Return true, indicating success writing the barcodeList to the history file
            }
            catch(Exception e){                                                             // If an exception occurs while writing to the file
                Log.e("File ", "Error writing to file" + e);                                // Log an error message and the exception
                return false;                                                               // Return false, indicating failure to save the barcodeList to the history file
            }
        }
        catch(Exception e){                                                                 // If an exception occurs while opening the barcode history file
            Log.e("File ", "Error opening the file" + e);                                   // Log an error message and the exception
            return false;                                                                   // Return false, indicating failure to save the barcodeList to the history file
        }
    }

    /**
     * Remove the specified Barcode object from the barcode history file
     * @param b A Barcode object
     * @return boolean
     */
    public boolean removeFromHistory(Barcode b){
        barcodeList = this.getHistory();                                // Retrieve the current ArrayList of Barcodes from the history file
        if(this.clearHistory()) {                                       // Attempt to clear the barcode history file, and, if it is successful, continue
            if(barcodeList.remove(b)) {                                 // Attempt to remove the specified Barcode from the ArrayList barcodeList
                return this.saveHistory();                              // Attempt to save the history to file, writing the ArrayList barcodeList to the
                                                                        // barcode history file and returning the result to indicate success or failure
            } else return false;                                        // Otherwise, the file does not contain the barcode and the method should return false, making no changes
        } else return false;                                            // Otherwise return false indicating the history file could not be cleared
    }

    /**
     * Attempt to delete the barcode history file
     * @return boolean
     */
    public boolean clearHistory(){
        File history = new File(c.getFilesDir(), fileName);         // Create a new File object referencing the barcode history file
        if(history.delete()){                                       // Attempt to delete the barcode history file
            return true;                                            // If the file was successfully deleted, return true
        } else return false;                                        // Otherwise return false
    }

}
