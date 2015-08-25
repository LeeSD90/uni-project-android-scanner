package com.leedoyle.barcodescanner;

public class Product {

    private String name;                                                // String variables to store the product attributes
    private String price;
    private String url;
    private String imageURL;

    /**
     * Creates a default Product object with attributes
     * initialised to empty strings
     */
    public Product(){
        name = "";                                                      // Initialise the class String variables to empty strings
        price = "";
        url = "";
        imageURL = "";
    }

    /**
     * Creates a new Product object with attributes
     * initialised as specified
     * @param n The product name
     * @param p The product price
     * @param url   The Ebay URL of the product
     * @param imageURL  The Ebay thumnail image URL
     */
    public Product(String n, String p, String url, String imageURL){
        name = n;                                                       // Initialise the class String variables to those passed
        price = p;                                                      // in the constructors argument
        this.url = url;
        this.imageURL = imageURL;
    }

    public String getName() { return name; }                            // Getter and setter methods for each of the Product class attributes

    public String getPrice() { return price; }

    public String getUrl() { return url; }

    public String getImageURL() { return imageURL; }

    public void setName(String name) { this.name = name; }

    public void setPrice(String price) { this.price = price; }

    public void setUrl(String url) { this.url = url; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}
