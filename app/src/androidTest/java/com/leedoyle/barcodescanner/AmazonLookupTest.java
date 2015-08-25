package com.leedoyle.barcodescanner;

import junit.framework.TestCase;

public class AmazonLookupTest extends TestCase {

    public void testGenerateHMAC() throws Exception{
        String dataToSign = "The quick brown fox jumps over the lazy dog";                              // English-language pangram
        String expectedResult = "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8";     //The expected result of encrypting the above String using HMAC SHA-256 with the key, "key"

        AmazonLookup lookup = new AmazonLookup();                               // Create a new AmazonLookup object to test with
        assertEquals(expectedResult, lookup.generateHMAC(dataToSign, "key"));   // Test that the generateHMAC method returns a String matching the expectedResult String above
    }

    public void testLookup() throws Exception{
        AmazonLookup lookup = new AmazonLookup();
        Barcode b = new Barcode("5901234123457", "EAN-8");
        lookup.lookup(b);
    }
}