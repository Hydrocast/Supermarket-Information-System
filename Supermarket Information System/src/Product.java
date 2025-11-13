import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a product in the supermarket inventory system.
 * Manages product details including pricing, categories, and promotional offers.
 * Enforces data validation through custom exceptions.
 */
public class Product {
    private String name;
    private String code; // Unique code
    private String category;
    private double basePrice;  // Store the original price
    private double currentPrice;  // Store the current price (with possible discount)
    private boolean isOnWeeklyOffer;
    private static final FileHandler fileHandler = new FileHandler();
    private static final HashMap<String, Product> allProducts = fileHandler.getProducts();

    /* ------------------------------
       EXCEPTION CLASS
       ------------------------------ */

    /**
     * Custom exception for invalid product data validation.
     */
    public static class InvalidProductDataException extends IllegalArgumentException {
        public InvalidProductDataException(String message) {
            super(message);
        }
    }

    /* ------------------------------
       CONSTRUCTORS
       ------------------------------ */

    /**
     * Creates a new product with full details.
     * Validates all parameters through setter methods.
     */
    public Product(String name, String code, String category, double pricePerUnit, boolean isOnWeeklyOffer) {
        setName(name);
        setCode(code);
        setCategory(category);
        setBasePrice(pricePerUnit);  // Set the base price
        setWeeklyOffer(isOnWeeklyOffer);  // This will set currentPrice
    }

    /**
     * Default constructor for empty product initialization.
     */
    public Product () {}

    /* ------------------------------
       GETTER METHODS
       ------------------------------ */

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getIsOnWeeklyOffer() {
        return isOnWeeklyOffer;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public boolean isOnWeeklyOffer() {
        return isOnWeeklyOffer;
    }

    /* ------------------------------
       SETTER METHODS WITH VALIDATION
       ------------------------------ */

    /**
     * Sets product name with validation:
     * - Not null/empty
     * - Maximum 100 characters
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidProductDataException("Product name cannot be null or empty.");
        }
        if (name.length() > 100) {
            throw new InvalidProductDataException("Product name cannot be longer than 100 characters.");
        }
        this.name = name.trim();
    }

    /**
     * Sets unique product code with validation:
     * - Not null/empty
     * - Must be unique across all products
     */
    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidProductDataException("Product code cannot be null or empty.");
        }
        String trimmedCode = code.trim();
        // Check if another product already has this code (excluding current product)
        for (Product product : allProducts.values()) {
            if (product != this && product.getCode().equals(trimmedCode)) {
                throw new InvalidProductDataException("Product code '" + trimmedCode + "' already exists in the system.");
            }
        }
        this.code = code;
    }

    /**
     * Sets product category with validation:
     * - Not null/empty
     */
    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new InvalidProductDataException("Product category cannot be null or empty.");
        }
        this.category = category.trim();
    }

    /**
     * Sets base price with validation:
     * - Must be positive value
     * - Automatically updates current price
     */
    public void setBasePrice(double price) {
        if (price <= 0) {
            throw new InvalidProductDataException("Price per unit must be a positive value.");
        }
        this.basePrice = price;
        updateCurrentPrice();
    }

    /**
     * Sets weekly offer status and updates current price accordingly.
     * Applies 10% discount when enabled.
     */
    public void setWeeklyOffer(boolean isOnWeeklyOffer) {
        this.isOnWeeklyOffer = isOnWeeklyOffer;
        updateCurrentPrice();
    }

    /* ------------------------------
       PRICE CALCULATION
       ------------------------------ */

    /**
     * Updates current price based on weekly offer status.
     * Applies 10% discount if product is on offer.
     */
    private void updateCurrentPrice() {
        this.currentPrice = isOnWeeklyOffer ? basePrice * 0.90 : basePrice;
    }

    /* ------------------------------
       PRODUCT DISPLAY METHODS
       ------------------------------ */

    /**
     * Displays all products in specified category.
     * Case-insensitive category matching.
     */
    public static void displayProductsByCategory(String category) {
        System.out.println("\nProducts in category '" + category + "':");
        for (Product product : allProducts.values()) {
            if (product.getCategory().equalsIgnoreCase(category)) {
                product.displayProductInfo();
            }
        }
    }

    /**
     * Displays complete product catalog with details.
     * Shows total product count.
     */
    public static void displayAllProducts() {
        if (allProducts.isEmpty()) {
            System.out.println("\nNo products available in the system.");
            return;
        }

        System.out.println("\n=== ALL PRODUCTS ===");
        for (Product product : allProducts.values()) {
            System.out.println("Code: " + product.getCode());
            System.out.println("Name: " + product.getName());
            System.out.println("Category: " + product.getCategory());
            System.out.println("Price per unit: " + product.getBasePrice());
            System.out.println("Weekly Offer: " + (product.getIsOnWeeklyOffer() ? "Yes (10% discount)" : "No"));
            System.out.println("------------------------------------");
        }
        System.out.println("Total products: " + allProducts.size());
    }

    /**
     * Displays product details by unique code.
     * Shows error message if product not found.
     */
    public static void displayProductByCode(String code) {
        Product product = allProducts.get(code);
        if (product != null) {
            System.out.println("\n=== PRODUCT FOUND ===");
            product.displayProductInfo();
        } else {
            System.out.println("\nNo product found with code: " + code);
        }
    }

    /**
     * Displays formatted product information.
     * Includes all relevant product attributes.
     */
    public void displayProductInfo() {
        System.out.println("Code: " + code);
        System.out.println("Name: " + name);
        System.out.println("Category: " + category);
        System.out.println("Price per unit: " + basePrice);
        System.out.println("Weekly Offer: " + (isOnWeeklyOffer ? "Yes (10% discount)" : "No"));
        System.out.println("------------------------------------");
    }

    /* ------------------------------
       OBJECT OVERRIDES
       ------------------------------ */

    /**
     * Returns string representation of product.
     * Format: "Product: [name] | Code: [code] | Category: [category] | Price: [price] | Offer: [status]"
     */
    @Override
    public String toString() {
        return "Product: " + name +
                " | Code: " + code +
                " | Category: " + category +
                " | Price: " + basePrice +
                " | Offer: " + (isOnWeeklyOffer ? "Yes (10% off)" : "No");
    }

    /**
     * Compares products for equality based on:
     * - Code (unique identifier)
     * - Name, category, base price, and offer status
     */
    @Override
    public boolean equals(Object o) {
        // 1. Check if comparing with itself
        if (this == o) return true;

        // 2. Check if object is null or different class
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Cast to Product and compare significant fields
        Product product = (Product) o;
        return Double.compare(product.basePrice, basePrice) == 0 &&
                isOnWeeklyOffer == product.isOnWeeklyOffer &&
                Objects.equals(code, product.code) &&  // Code is unique identifier
                Objects.equals(name, product.name) &&
                Objects.equals(category, product.category);
    }
}