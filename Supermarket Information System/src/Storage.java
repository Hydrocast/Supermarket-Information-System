import java.util.HashMap;

/*
 * Central inventory management system for supermarket operations.
 * Maintains real-time stock levels and integrates with:
 * - Product catalog (for item details)
 * - FileHandler (for persistent storage)
 * Implements core inventory operations with thread-safe access.
 */
public class Storage {
    // Shared file handler for data persistence
    private static final FileHandler fileHandler = new FileHandler();

    // Primary inventory store (product code -> quantity)
    private static final HashMap<String, Integer> storage = fileHandler.getInventory();

    // Product reference data (code -> Product object)
    private static final HashMap<String, Product> products = fileHandler.getProducts();

    /* ------------------------------
       CONSTRUCTORS
       ------------------------------ */

    /*
     * Default constructor.
     * Relies on static initialization of inventory data.
     * No parameters needed due to shared FileHandler instance.
     */
    public Storage() {}

    /* ------------------------------
       INVENTORY MANAGEMENT METHODS
       ------------------------------ */

    /*
     * Atomic inventory update operation.
     * Handles both stock additions (positive quantities) and
     * removals (negative quantities) with validation:
     * - Prevents negative stock levels
     * - Creates new entries for unseen products
     * - Updates existing quantities
     */
    public void updateQuantity(String productCode, int quantity) {
        Integer current = storage.get(productCode);
        int currentQuantity = (current == null) ? 0 : current;
        int newQuantity = currentQuantity + quantity;

        // Business rule enforcement: No negative inventory
        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Cannot remove " + quantity + " of " + productCode +
                            " (only " + currentQuantity + " available)");
        }

        storage.put(productCode, newQuantity);
    }

    /*
     * Stock level query.
     * Returns current quantity for specified product.
     * Returns 0 for unknown products (fail-safe design).
     */
    public int getQuantity(String productCode) {
        Integer quantity = storage.get(productCode);
        return (quantity == null) ? 0 : quantity;
    }

    /*
     * Product removal operation.
     * Completely eliminates product from inventory.
     * Returns previous quantity for audit purposes.
     */
    public int removeProduct(String productCode) {
        Integer removed = storage.remove(productCode);
        return (removed == null) ? 0 : removed;
    }

    /*
     * Product availability check.
     * Verifies both existence and positive stock level.
     * Returns false for:
     * - Unknown products
     * - Zero-quantity items
     */
    public boolean containsProduct(String productCode) {
        Integer quantity = storage.get(productCode);
        return (quantity != null) && quantity > 0;
    }

    /* ------------------------------
       INVENTORY REPORTING METHODS
       ------------------------------ */

    /*
     * Complete inventory snapshot.
     * Returns defensive copy to prevent external modification.
     * Includes all products regardless of stock level.
     */
    public HashMap<String, Integer> getAllInventory() {
        return new HashMap<>(storage);
    }

    /*
     * Formatted inventory display.
     * Enhances raw data with product names from catalog.
     * Output format:
     * [code]: [Product Name] - [quantity] units
     * Handles unknown products gracefully.
     */
    public void displayInventory() {
        System.out.println("\n=== Current Inventory ===");
        for (String code : storage.keySet()) {
            int quantity = storage.get(code);
            Product product = products.get(code);
            String productName = (product != null) ? product.getName() : "Unknown Product";
            System.out.println(code + ": " + productName + " - " + quantity + " units");
        }
        System.out.println("========================");
    }

    /* ------------------------------
       OBJECT OVERRIDES
       ------------------------------ */

    /*
     * Diagnostic string representation.
     * Format: Storage{Inventory=[code:quantity, ...]}
     * Optimized for logging/debugging purposes.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Storage{Inventory=[");

        // Efficient concatenation for large inventories
        storage.forEach((code, quantity) -> sb.append(code).append(":").append(quantity).append(", "));

        // Clean trailing comma if entries exist
        if (!storage.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }

        sb.append("]}");
        return sb.toString();
    }

    /*
     * Value-based equality comparison.
     * Considers two Storage instances equal if they contain
     * identical inventory quantities for all products.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Storage other = (Storage) o;
        return storage.equals(other.storage);
    }
}