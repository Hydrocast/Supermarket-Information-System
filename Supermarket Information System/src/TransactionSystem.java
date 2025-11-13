import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Handles all transaction processing for the supermarket system.
 * Manages shopping carts, pricing calculations, discounts, and receipt generation.
 * Integrates with inventory, customer, and reporting systems.
 */
public class TransactionSystem {

    private double amount;
    private boolean transactionInProgress;
    private double totalBeforeDiscount;
    private double totalDiscount;
    private double finalAmount;
    private ArrayList<Product> currentCart;
    private Cashier currentCashier;
    private Customer currentCustomer;
    private static final FileHandler fileHandler = new FileHandler();
    private static final HashMap<String, Product> products = fileHandler.getProducts();
    private static final HashMap<String, ArrayList<TransactionLabel>> allTransactionHistory = fileHandler.getCustomerTransactions();
    private static final Storage storage = new Storage();
    private static final GeneralReport report = new GeneralReport();

    /* ------------------------------
       CONSTRUCTOR
       ------------------------------ */

    /**
     * Creates a new transaction system tied to a specific cashier.
     * Initializes with empty cart and inactive transaction state.
     */
    public TransactionSystem(Cashier cashier) {
        this.currentCashier = cashier;
        this.currentCart = new ArrayList<>();
        this.transactionInProgress = false;
    }

    /* ------------------------------
       TRANSACTION CONTROL METHODS
       ------------------------------ */

    /**
     * Begins a new transaction session.
     * Resets all transaction variables and clears previous cart.
     * Throws IllegalStateException if transaction already in progress.
     */
    public void startNewTransaction() {
        if (transactionInProgress) {
            throw new IllegalStateException("A transaction is already in progress");
        }
        currentCart.clear();
        currentCustomer = null;
        amount = 0;
        totalBeforeDiscount = 0;
        totalDiscount = 0;
        finalAmount = 0;
        transactionInProgress = true;
    }

    /**
     * Completes the current transaction and generates receipt.
     * Updates customer bonus points and system reports.
     * Throws IllegalStateException if no active transaction.
     */
    public void completeTransaction() {
        if (!transactionInProgress) {
            throw new IllegalStateException("No transaction in progress");
        }

        if (finalAmount == 0) {
            finalAmount = amount;
        }

        int pointsUsed = 0;

        if (currentCustomer != null) {
            TransactionLabel transaction = new TransactionLabel(finalAmount);
            currentCustomer.addTransaction(transaction);

            // Calculate points used if bonus points were applied
            pointsUsed = (int)((amount - finalAmount) * 100);

            // Record transaction with actual spent amount (finalAmount) and correct points
            report.recordTransaction(currentCashier, currentCustomer, finalAmount, pointsUsed);
            int earnedPoints = (int)finalAmount;
            currentCustomer.addBonusPoints(earnedPoints);
            allTransactionHistory.put(currentCustomer.getPhone(), currentCustomer.getTransactionHistory());
        } else {
            report.recordTransaction(currentCashier, null, finalAmount, 0);
        }

        printReceipt();
        transactionInProgress = false;
    }

    /**
     * Cancels the current transaction and restores inventory.
     * Throws IllegalStateException if no active transaction.
     */
    public void cancelTransaction() {
        if (!transactionInProgress) {
            throw new IllegalStateException("No transaction in progress");
        }

        // Restore stock
        HashMap<String, Integer> productsToRestore = new HashMap<>();
        for (Product product : currentCart) {
            String code = product.getCode();
            Integer count = productsToRestore.get(code);
            productsToRestore.put(code, (count == null) ? 1 : count + 1);
        }

        for (String code : productsToRestore.keySet()) {
            storage.updateQuantity(code, productsToRestore.get(code));
        }

        transactionInProgress = false;
        System.out.println("Transaction cancelled. Stock restored.");
    }

    /* ------------------------------
       PRODUCT MANAGEMENT
       ------------------------------ */

    /**
     * Adds product to current transaction with specified quantity.
     * Validates product existence and available stock.
     * Updates running totals and inventory.
     */
    public void addProductToCart(String productCode, int quantity) {
        if (!transactionInProgress) {
            throw new IllegalStateException("No transaction in progress");
        }

        Product product = products.get(productCode);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productCode);
        }

        int availableQuantity = storage.getQuantity(productCode);
        if (availableQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + availableQuantity);
        }

        // Add the product to cart (multiple times for quantity)
        for (int i = 0; i < quantity; i++) {
            currentCart.add(product);
        }

        // Update stock in memory
        storage.updateQuantity(productCode, -quantity);

        // Calculate price
        double price = product.getCurrentPrice();
        double totalForItem = price * quantity;

        amount += totalForItem;
        totalBeforeDiscount += product.getBasePrice() * quantity;
        // Calculate discount as difference between base price and actual price
        totalDiscount += (product.getBasePrice() - price) * quantity;

        System.out.println("Added " + quantity + " x " + product.getName() +
                " (" + productCode + ") at " + price + " each");
    }

    /* ------------------------------
       CUSTOMER OPERATIONS
       ------------------------------ */

    /**
     * Associates a customer with the current transaction.
     * Enables bonus point redemption and earning.
     */
    public void setCurrentCustomer(Customer currentCustomer) {
        this.currentCustomer = currentCustomer;
    }

    /**
     * Applies customer bonus points to current transaction if requested.
     * Calculates discount based on available points (1 point = 1€).
     */
    public void applyBonusPoints(boolean useBonusPoints) {
        if (currentCustomer == null) return;

        if (useBonusPoints) {
            double discount = currentCustomer.useBonusPoints(amount);
            totalDiscount += discount;
            finalAmount = amount - discount;
            System.out.println("Applied " + discount + "€ discount from bonus points");
        } else {
            finalAmount = amount;
        }
    }

    /* ------------------------------
       RECEIPT GENERATION
       ------------------------------ */

    /**
     * Generates and prints formatted receipt for current transaction.
     * Includes product details, discounts, and final amount.
     */
    private void printReceipt() {
        System.out.println("\n=== RECEIPT ===");
        System.out.println("Cashier: " + currentCashier.getName());
        if (currentCustomer != null) {
            System.out.println("Customer: " + currentCustomer.getName());
        }

        // Group products by code
        HashMap<String, Integer> productQuantities = new HashMap<>();
        HashMap<String, Product> productDetails = new HashMap<>();

        for (Product product : currentCart) {
            String code = product.getCode();
            Integer count = productQuantities.get(code);
            productQuantities.put(code, (count == null) ? 1 : count + 1);
            if (!productDetails.containsKey(code)) {
                productDetails.put(code, product);
            }
        }

        // Print each product line
        for (String code : productQuantities.keySet()) {
            Product p = productDetails.get(code);
            int qty = productQuantities.get(code);
            double price = p.getCurrentPrice();
            double total = price * qty;

            System.out.printf("%s %d x %.2f€ = %.2f€%n",
                    p.getName(), qty, price, total);
        }

        System.out.println("----------------------------");
        System.out.printf("Subtotal: %.2f€%n", totalBeforeDiscount);
        System.out.printf("Discounts: %.2f€%n", totalDiscount);
        System.out.printf("TOTAL: %.2f€%n", finalAmount);
        System.out.println("============================");
    }

    /* ------------------------------
       GETTER METHODS
       ------------------------------ */

    public double getAmount() {
        return amount;
    }

    public boolean isTransactionInProgress() {
        return transactionInProgress;
    }

    public double getTotalBeforeDiscount() {
        return totalBeforeDiscount;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    /**
     * Returns defensive copy of current cart contents.
     */
    public ArrayList<Product> getCurrentCart() {
        return new ArrayList<>(currentCart); // Return defensive copy
    }

    public Cashier getCurrentCashier() {
        return currentCashier;
    }

    /* ------------------------------
       OBJECT OVERRIDES
       ------------------------------ */

    /**
     * Returns string representation of transaction state.
     * Includes amounts, status, and participant details.
     */
    @Override
    public String toString() {
        return "TransactionSystem{" +
                "amount=" + amount +
                ", transactionInProgress=" + transactionInProgress +
                ", totalBeforeDiscount=" + totalBeforeDiscount +
                ", totalDiscount=" + totalDiscount +
                ", finalAmount=" + finalAmount +
                ", currentCustomer=" + (currentCustomer != null ? currentCustomer.getName() : "null") +
                ", currentCartSize=" + currentCart.size() +
                ", currentCashier=" + currentCashier.getName() +
                '}';
    }

    /**
     * Compares transaction systems based on all significant fields.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionSystem that = (TransactionSystem) o;
        return Double.compare(that.amount, amount) == 0 &&
                transactionInProgress == that.transactionInProgress &&
                Double.compare(that.totalBeforeDiscount, totalBeforeDiscount) == 0 &&
                Double.compare(that.totalDiscount, totalDiscount) == 0 &&
                Double.compare(that.finalAmount, finalAmount) == 0 &&
                Objects.equals(currentCustomer, that.currentCustomer) &&
                Objects.equals(currentCart, that.currentCart) &&
                Objects.equals(currentCashier, that.currentCashier);
    }
}