package com.leedoyle.barcodescanner;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import java.util.ArrayList;

public class BarcodeHistoryTest extends InstrumentationTestCase {

    Context c;

    @Override
    public void setUp() throws Exception{
        super.setUp();
        c = getInstrumentation().getTargetContext();                           // Set the Context
        assertNotNull(c);                                                      // Test the Context is not null
    }

    public void testAddToHistory() throws Exception {
        Barcode b = new Barcode("1234567", "format");                          // Dummy barcode
        BarcodeHistory history = new BarcodeHistory(c);                        // New history
        history.clearHistory();                                                // Ensure history is clear
        assertEquals(new ArrayList<>(), history.getHistory());                 // Test that the history is empty
        history.addToHistory(b);                                               // Add dummy barcode to history
        assertNotSame(new ArrayList<>(), history.getHistory());                // Test that the history is not empty
        assertEquals(b, history.getHistory().get(0));                          // Test that the new element in the history list is equal to the dummy barcode
    }

}