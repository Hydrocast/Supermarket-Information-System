import java.util.HashMap;

/**
 * Generates comprehensive reports for supermarket operations.
 * Tracks and analyzes sales data, customer spending, and bonus point activity.
 * Maintains aggregated statistics across all transactions.
 */
public class GeneralReport {
    private static final FileHandler fileHandler = new FileHandler();
    private static final HashMap<String, Product> allProducts = fileHandler.getProducts();
    private static final HashMap<String, Double> cashierSales = new HashMap<String, Double>();
    private static final HashMap<String, Double> customerSpending = new HashMap<String, Double>();
    private static final HashMap<String, Integer> pointsEarned = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> pointsRedeemed = new HashMap<String, Integer>();
    private static int totalTransactions = 0;

    /* ------------------------------
       CONSTRUCTOR
       ------------------------------ */

    /**
     * Initializes a new GeneralReport instance.
     * Uses static data structures shared across all instances.
     */
    public GeneralReport() {}

    /* ------------------------------
       DATA RECORDING METHODS
       ------------------------------ */

    /**
     * Records transaction details for reporting purposes.
     * Updates cashier sales, customer spending, and bonus point statistics.
     */
    public void recordTransaction(Cashier cashier, Customer customer, double amount, int pointsUsed) {
        totalTransactions++;

        String cashierId = cashier.getPassportId();
        Double currentSales = cashierSales.get(cashierId);
        cashierSales.put(cashierId, currentSales == null ? amount : currentSales + amount);

        if (customer != null) {
            String phone = customer.getPhone();

            Double currentSpend = customerSpending.get(phone);
            customerSpending.put(phone, currentSpend == null ? amount : currentSpend + amount);

            int earned = (int)amount;
            Integer currentEarned = pointsEarned.get(phone);
            pointsEarned.put(phone, currentEarned == null ? earned : currentEarned + earned);

            Integer currentRedeemed = pointsRedeemed.get(phone);
            pointsRedeemed.put(phone, currentRedeemed == null ? pointsUsed : currentRedeemed + pointsUsed);
        }
    }

    /* ------------------------------
       REPORT GENERATION METHODS
       ------------------------------ */

    /**
     * Generates and displays the complete supermarket activity report.
     * Includes inventory status, sales by cashier, customer activity, and summary statistics.
     */
    public static void generateFinalReport() {
        System.out.println("\n=== SUPERMARKET FINAL REPORT ===\n");
        printInventory();
        printCashierSales();
        printCustomerDetails();
        printSummaryStatistics();
        System.out.println("\n=== END OF REPORT ===");
    }

    /**
     * Prints current inventory status including product details and quantities.
     */
    private static void printInventory() {
        System.out.println("CURRENT INVENTORY:");
        Storage storage = new Storage();
        HashMap<String, Integer> inventory = storage.getAllInventory();

        if (inventory.isEmpty()) {
            System.out.println("  No inventory available");
        } else {
            for (String productCode : inventory.keySet()) {
                Product product = allProducts.get(productCode);
                if (product != null) {
                    System.out.println(productCode + ": " + inventory.get(productCode) +
                            " units (" + product.getName() + " - " + product.getCategory() + ")");
                } else {
                    System.out.println(productCode + ": " + inventory.get(productCode) + " units");
                }
            }
        }
    }

    /**
     * Prints sales figures organized by cashier.
     * Includes name and total sales amount for each cashier.
     */
    private static void printCashierSales() {
        System.out.println("\nSALES BY CASHIER:");
        if (cashierSales.isEmpty()) {
            System.out.println("  No sales recorded");
        } else {
            for (String cashierId : cashierSales.keySet()) {
                Employee emp = Employee.findById(cashierId);
                if (emp != null) {
                    System.out.println(emp.getName() + " " + emp.getSurname() + ": " +
                            String.format("%.2f", cashierSales.get(cashierId)) + "€");
                }
            }
        }
    }

    /**
     * Prints detailed customer activity including:
     * - Total spending
     * - Bonus points earned
     * - Bonus points redeemed
     */
    private static void printCustomerDetails() {
        System.out.println("\nCUSTOMER SPENDING AND POINTS:");
        if (Customer.getAllCustomers().isEmpty()) {
            System.out.println("  No customers registered");
        } else {
            for (String phone : customerSpending.keySet()) {
                Customer customer = Customer.findCustomer(phone);
                System.out.println(customer.getName() + " " + customer.getSurname() + " (Phone: " + phone + ")");
                Double spent = customerSpending.get(phone);
                System.out.println("  Total spent: " + (spent == null ? "0.00" : String.format("%.2f", spent)) + "€");

                Integer earned = pointsEarned.get(phone);
                System.out.println("  Points earned: " + (earned == null ? "0" : earned));

                Integer redeemed = pointsRedeemed.get(phone);
                System.out.println("  Points redeemed: " + (redeemed == null ? "0" : redeemed));

                System.out.println();
            }
        }
    }

    /**
     * Prints aggregated sales statistics:
     * - Total sales amount
     * - Total transaction count
     */
    private static void printSummaryStatistics() {
        System.out.println("\nSUMMARY STATISTICS:");

        double totalSales = 0;
        for (Double sales : cashierSales.values()) {
            totalSales += sales;
        }
        System.out.println("Total Sales: " + String.format("%.2f", totalSales) + "€");
        System.out.println("Total Transactions: " + totalTransactions);
    }
}