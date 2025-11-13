import java.util.HashMap;
import java.util.Scanner;

/**
 * Represents a Cashier employee in the supermarket system.
 * Handles customer transactions, product scanning, and bonus card operations.
 * Extends the base Employee class with transaction-specific functionality.
 */
public class Cashier extends Employee {
    private final TransactionSystem transactionSystem;
    private static final FileHandler fileHandler = new FileHandler();
    private static final HashMap<String, Product> products = fileHandler.getProducts();
    private static final Storage storage = new Storage();  // Changed to Storage instance

    /* ------------------------------
       CONSTRUCTORS
       ------------------------------ */

    /**
     * Creates a new Cashier with employee details and initializes transaction system.
     * Sets role automatically to CASHIER and active status to true.
     */
    public Cashier(String passportId, String username, String password,
                   String name, String surname) {
        super(passportId, username, password, name, surname, EmployeeRole.CASHIER.getCode(), true);
        this.transactionSystem = new TransactionSystem(this);
    }

    /* ------------------------------
       TRANSACTION OPERATIONS
       ------------------------------ */

    /**
     * Initiates a new sales transaction.
     * Prints confirmation message with cashier's name.
     */
    public void startNewSale() {
        transactionSystem.startNewTransaction();
        System.out.println("New transaction started by " + getName());
    }

    /**
     * Adds a product to the current transaction with specified quantity.
     * Handles IllegalArgumentException for invalid quantities/product codes.
     */
    public void scanProduct(String productCode, int quantity) {
        try {
            transactionSystem.addProductToCart(productCode, quantity);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Completes the current transaction, optionally applying bonus points.
     * Processes payment and updates inventory.
     */
    public void finalizeSale(boolean useBonusPoints) {
        transactionSystem.applyBonusPoints(useBonusPoints);
        transactionSystem.completeTransaction();
    }

    /**
     * Cancels the current transaction without completing the sale.
     * Clears all temporary transaction data.
     */
    public void cancelSale() {
        transactionSystem.cancelTransaction();
    }

    /* ------------------------------
       CUSTOMER OPERATIONS
       ------------------------------ */

    /**
     * Displays customer details by phone number lookup.
     * Shows name, surname, phone, and current bonus points.
     */
    public void viewCustomer(Scanner scanner) {
        System.out.print("Enter customer phone number: ");
        String phoneNumber = scanner.nextLine().trim();
        Customer customer = Customer.findCustomer(phoneNumber);
        if (customer != null) {
            System.out.println("\n--- Customer Details ---");
            System.out.println("Name: " + customer.getName());
            System.out.println("Surname: " + customer.getSurname());
            System.out.println("Phone Number: " + customer.getPhone());
            System.out.println("Bonus Points: " + customer.getBonusPoints());
        } else {
            System.out.println("Customer not found");
        }
    }

    /* ------------------------------
       PRODUCT OPERATIONS
       ------------------------------ */

    /**
     * Checks and displays product stock information.
     * Shows product details, current stock level, and price.
     */
    public void checkStock(String productCode) {
        Product product = products.get(productCode);
        if (product != null) {
            System.out.println("\n--- Product Info ---");
            System.out.println(product);  // Uses Product's toString()
            System.out.println("In Stock: " + storage.getQuantity(productCode));
            System.out.println("Price: " + product.getCurrentPrice() + "€");
        } else {
            System.out.println("Product not found");
        }
    }

    /* ------------------------------
       MENU SYSTEM
       ------------------------------ */

    /**
     * Displays and processes the cashier's interactive menu system.
     * Handles all cashier operations through console input.
     */
    public void showCashierMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== CASHIER MENU ===");
            System.out.println("1. Start New Transaction");
            System.out.println("2. Scan Product");
            System.out.println("3. Lookup Customer");
            System.out.println("4. Finalize Sale");
            System.out.println("5. Cancel Transaction");
            System.out.println("6. View Customer Bonus Card");
            System.out.println("7. Check Product Stock");
            System.out.println("8. Exit");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    handleStartTransaction();
                    break;
                case 2:
                    handleScanProduct(scanner);
                    break;
                case 3:
                    viewCustomer(scanner);
                    break;
                case 4:
                    handleFinalizeSale(scanner);
                    break;
                case 5:
                    handleCancelTransaction();
                    break;
                case 6:
                    handleViewCustomer(scanner);
                    break;
                case 7:
                    handleCheckStock(scanner);
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------
       PRIVATE MENU HANDLERS
       ------------------------------ */

    private void handleStartTransaction() {
        try {
            if (transactionSystem.isTransactionInProgress()) {
                throw new IllegalStateException("A transaction is already in progress!");
            }
            startNewSale();
            System.out.println("New transaction started successfully");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleScanProduct(Scanner scanner) {
        try {
            if (!transactionSystem.isTransactionInProgress()) {
                throw new IllegalStateException("No active transaction. Please start a new transaction first.");
            }

            System.out.print("Enter product code: ");
            String productCode = scanner.nextLine().trim();
            if (productCode.isEmpty()) {
                throw new IllegalArgumentException("Product code cannot be empty");
            }

            System.out.print("Enter quantity: ");
            if (!scanner.hasNextInt()) {
                throw new IllegalArgumentException("Quantity must be a number");
            }
            int quantity = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            scanProduct(productCode, quantity);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleFinalizeSale(Scanner scanner) {
        try {
            if (!transactionSystem.isTransactionInProgress()) {
                throw new IllegalStateException("No active transaction to finalize.");
            }

            // Step 1: Ask if customer has bonus card
            System.out.print("Does customer have a bonus card? (y/n): ");
            String hasCard = scanner.nextLine().trim().toLowerCase();

            // Validate input
            while (!hasCard.equals("y") && !hasCard.equals("n")) {
                System.out.println("Invalid input. Please enter 'y' or 'n'");
                System.out.print("Does customer have a bonus card? (y/n): ");
                hasCard = scanner.nextLine().trim().toLowerCase();
            }

            if (hasCard.equals("y")) {
                // Step 2: Look up customer
                Customer customer = handleViewCustomer(scanner);
                transactionSystem.setCurrentCustomer(customer);

                if (customer != null) {
                    if (customer.getBonusPoints() >= 100) {
                        // Step 3: Ask if they want to use points
                        System.out.print("Use bonus points? (y/n): ");
                        String usePoints = scanner.nextLine().trim().toLowerCase();

                        while (!usePoints.equals("y") && !usePoints.equals("n")) {
                            System.out.println("Invalid input. Please enter 'y' or 'n'");
                            System.out.print("Use bonus points? (y/n): ");
                            usePoints = scanner.nextLine().trim().toLowerCase();
                        }

                        transactionSystem.applyBonusPoints(usePoints.equals("y"));
                    }
                }
                else {
                    System.out.println("Proceeding without bonus points");
                    transactionSystem.applyBonusPoints(false);
                }
            } else {
                // No bonus card - proceed normally
                transactionSystem.applyBonusPoints(false);
            }

            // Complete the transaction
            transactionSystem.completeTransaction();
            System.out.println("Transaction completed successfully");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleCancelTransaction() {
        try {
            if (!transactionSystem.isTransactionInProgress()) {
                throw new IllegalStateException("No active transaction to cancel.");
            }
            cancelSale();
            System.out.println("Transaction cancelled successfully");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private Customer handleViewCustomer(Scanner scanner) {
        try {
            System.out.print("Enter customer phone number: ");
            String phone = scanner.nextLine().trim();
            if (phone.isEmpty()) {
                throw new IllegalArgumentException("Phone number cannot be empty");
            }
            Customer customer = Customer.findCustomer(phone.trim());
            System.out.println("Customer is registered. Bonus card balance: " + customer.getBonusPoints());
            return customer;
        } catch (Exception e) {
            System.out.println("Customer is not registered");
            return null;
        }
    }

    private void handleCheckStock(Scanner scanner) {
        try {
            System.out.print("Enter product code: ");
            String productCode = scanner.nextLine().trim();
            if (productCode.isEmpty()) {
                throw new IllegalArgumentException("Product code cannot be empty");
            }
            checkStock(productCode);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /* ------------------------------
       UTILITY METHODS
       ------------------------------ */

    /**
     * Returns string representation using Employee's toString().
     */
    @Override
    public String toString() {
        return super.toString();  // Reuses Employee's toString implementation
    }

    /**
     * Displays cashier information including current transaction status.
     */
    public void displayCashierInfo() {
        System.out.println("\n=== Cashier ===");
        System.out.println(this);  // Uses the overridden toString()
        System.out.println("Active Transactions: " +
                (transactionSystem.isTransactionInProgress() ? "Yes" : "No"));
    }

    /**
     * Compares Cashier objects based on Employee equality.
     */
    @Override
    public boolean equals(Object o) {
        // 1. Check if comparing with itself
        if (this == o) return true;

        // 2. Check if object is null or different class
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Delegate to parent class (Employee) for equality check
        return super.equals(o);
    }
}