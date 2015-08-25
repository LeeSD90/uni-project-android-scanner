package com.leedoyle.barcodescanner;

import junit.framework.TestCase;

import java.util.ArrayList;

public class EbayLookupTest extends TestCase {

    public void testParseResult() throws Exception {
        String xmlResponse = "<?xml version='1.0' encoding='UTF-8'?>" +                                                                // Sample Ebay XML response with dummy data for parse testing
                "<findItemsByKeywordsResponse xmlns=\"http://www.ebay.com/marketplace/search/v1/services\">" +
                "<ack>Success</ack>" +
                "<version>1.13.0</version>" +
                "<timestamp>2015-07-03T14:21:49.574Z</timestamp>" +
                "<searchResult count=\"2\">" +
                "<item><itemId>231473262021</itemId><title>Test Item 1</title>" +
                "<globalId>EBAY-GB</globalId>" +
                "<primaryCategory><categoryId>139973</categoryId><categoryName>Games</categoryName></primaryCategory>" +
                "<galleryURL>http://www.testItemUrl.com/image.jpg</galleryURL>" +
                "<viewItemURL>http://www.testItemUrl.com</viewItemURL>" +
                "<paymentMethod>PayPal</paymentMethod>" +
                "<autoPay>true</autoPay>" +
                "<location>United Kingdom</location><country>GB</country>" +
                "<shippingInfo><shippingServiceCost currencyId=\"GBP\">0.0</shippingServiceCost><shippingType>Free</shippingType>" +
                "<shipToLocations>GB</shipToLocations></shippingInfo>" +
                "<sellingStatus><currentPrice currencyId=\"GBP\">4.75</currentPrice>" +
                "<convertedCurrentPrice currencyId=\"GBP\">37.48</convertedCurrentPrice>" +
                "<sellingState>Active</sellingState><timeLeft>P3DT1H49M32S</timeLeft></sellingStatus>" +
                "<listingInfo><bestOfferEnabled>false</bestOfferEnabled><buyItNowAvailable>false</buyItNowAvailable>" +
                "<startTime>2015-02-06T16:06:21.000Z</startTime><endTime>2015-07-06T16:11:21.000Z</endTime>" +
                "<listingType>FixedPrice</listingType><gift>false</gift></listingInfo><condition><conditionId>1000</conditionId>" +
                "<conditionDisplayName>New</conditionDisplayName></condition>" +
                "<isMultiVariationListing>false</isMultiVariationListing><discountPriceInfo>" +
                "<originalRetailPrice currencyId=\"GBP\">15.45</originalRetailPrice><pricingTreatment>STP</pricingTreatment>" +
                "<soldOnEbay>false</soldOnEbay>" +
                "<soldOffEbay>false</soldOffEbay></discountPriceInfo><topRatedListing>true</topRatedListing></item>" +
                "<item><itemId>271623068710</itemId><title>Test Item 2</title><globalId>EBAY-GB</globalId>" +
                "<primaryCategory><categoryId>139973</categoryId><categoryName>Games</categoryName></primaryCategory>" +
                "<galleryURL>http://www.testItem2Url.com/image.jpg</galleryURL>" +
                "<viewItemURL>http://www.testItem2Url.com</viewItemURL>" +
                "<paymentMethod>PayPal</paymentMethod><autoPay>true</autoPay><location>United Kingdom</location><country>GB</country>" +
                "<shippingInfo><shippingServiceCost currencyId=\"GBP\">0.0</shippingServiceCost>" +
                "<shippingType>Free</shippingType><shipToLocations>EuropeanUnion</shipToLocations></shippingInfo>" +
                "<sellingStatus><currentPrice currencyId=\"GBP\">6.75</currentPrice>" +
                "<convertedCurrentPrice currencyId=\"GBP\">39.87</convertedCurrentPrice><sellingState>Active</sellingState>" +
                "<timeLeft>P24DT16H47M47S</timeLeft></sellingStatus><listingInfo><bestOfferEnabled>false</bestOfferEnabled>" +
                "<buyItNowAvailable>false</buyItNowAvailable><startTime>2014-10-01T07:04:36.000Z</startTime>" +
                "<endTime>2015-07-28T07:09:36.000Z</endTime><listingType>FixedPrice</listingType><gift>false</gift></listingInfo>" +
                "<condition><conditionId>1000</conditionId><conditionDisplayName>New</conditionDisplayName>" +
                "</condition><isMultiVariationListing>false</isMultiVariationListing><topRatedListing>false</topRatedListing></item>" +
                "</searchResult><paginationOutput><pageNumber>1</pageNumber><entriesPerPage>50</entriesPerPage>" +
                "<totalPages>1</totalPages><totalEntries>2</totalEntries></paginationOutput>" +
                "<itemSearchURL>http://www.testItemUrl.com/test.html</itemSearchURL></findItemsByKeywordsResponse>"; // Sample Ebay XML response with dummy data for parse testing

        Product test1, test2;
        test1 = new Product("Test Item 1", "4.75 GBP",                                              // Create two new products matching the data in the dummy XML response above
                "http://www.testItemUrl.com", "http://www.testItemUrl.com/image.jpg");
        test2 = new Product("Test Item 2", "6.75 GBP",
                "http://www.testItem2Url.com", "http://www.testItem2Url.com/image.jpg");
        ArrayList<Product> dummyList = new ArrayList<Product>();                                    // A new ArrayList to hold the dummy Products
        dummyList.add(test1);                                                                       // Add the dummy products
        dummyList.add(test2);
        EbayLookup lookup = new EbayLookup();                                                       // EbayLookup object to test with
        ArrayList<Product> realList = lookup.parseResult(xmlResponse);                              // Parse the dummy xml and store the result in a new ArrayList
        for(int i = 0; i < realList.size(); i++){                                                   // Test the contents of the hardcoded dummy ArrayList with the result
            assertEquals(realList.get(i).getName(), dummyList.get(i).getName());                    // of our parse method. Comparing each attribute manually here as the class
            assertEquals(realList.get(i).getPrice(), dummyList.get(i).getPrice());                  // is small. Alternatively I could compare both lists with assertEquals but
            assertEquals(realList.get(i).getUrl(), dummyList.get(i).getUrl());                      // it would require me to override the equals() method of my Product class
            assertEquals(realList.get(i).getImageURL(), dummyList.get(i).getImageURL());
        }

    }
}