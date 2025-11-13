import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Represents a Manager employee in the supermarket system.
 * Extends Employee class with additional inventory, customer, and reporting functionalities.
 * Handles product management, customer registration, and system reporting.
 */
public class Manager extends Employee {
    private static final FileHandler fileHandler = new FileHandler();
    private static final Storage storage = new Storage();
    private static final HashMap<String, Product> products = fileHandler.getProducts();
    private static final HashMap<String, Customer> allCustomers = fileHandler.getCustomers();
    private static final HashMap<String, ArrayList<TransactionLabel>> allTransactionHistory = fileHandler.getCustomerTransactions();

    /* ------------------------------
       CONSTRUCTORS
       ------------------------------ */

    /**
     * Creates a new Manager with employee details.
     * Sets role automatically to MANAGER and active status to true.
     */
    public Manager(String passportId, String username, String password,
                   String name, String surname) {
        super(passportId, username, password, name, surname, EmployeeRole.MANAGER.getCode(), true);
    }

    /* ------------------------------
       MAIN MENU SYSTEM
       ------------------------------ */

    /**
     * Displays and manages the primary menu interface for manager operations.
     * Provides access to product management, customer management, and reporting features.
     */
    public void showManagerMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== MANAGER MENU ===");
            System.out.println("1. Product Management");
            System.out.println("2. Customer Management");
            System.out.println("3. Reports");
            System.out.println("4. Exit");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    showProductManagementMenu(scanner);
                    break;
                case 2:
                    showCustomerManagementMenu(scanner);
                    break;
                case 3:
                    showReportsMenu(scanner);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /* ------------------------------
       PRODUCT MANAGEMENT
       ------------------------------ */

    /**
     * Displays product management submenu with inventory control options.
     */
    private void showProductManagementMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== PRODUCT MANAGEMENT ===");
            System.out.println("1. Add New Product");
            System.out.println("2. Update Stock");
            System.out.println("3. Set Weekly Offer");
            System.out.println("4. View Products by Code");
            System.out.println("5. View Products by Category");
            System.out.println("6. View All Products");
            System.out.println("7. View Inventory");
            System.out.println("8. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleAddProduct(scanner);
                    break;
                case 2:
                    handleUpdateStock(scanner);
                    break;
                case 3:
                    handleSetWeeklyOffer(scanner);
                    break;
                case 4:
                    handleViewByCode(scanner);
                    break;
                case 5:
                    handleViewByCategory(scanner);
                    break;
                case 6:
                    handleViewAllProducts();
                    break;
                case 7:
                    storage.displayInventory();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /**
     * Displays product details by searching with product code.
     * Handles invalid code exceptions gracefully.
     */
    private void handleViewByCode(Scanner scanner) {
        System.out.print("Enter product code: ");
        String code = scanner.nextLine();
        String trimmedCode = code.trim();
        try {
            Product.displayProductByCode(trimmedCode);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Guides user through new product creation process.
     * Validates all inputs and handles potential exceptions.
     */
    private void handleAddProduct(Scanner scanner) {
        Product newProduct = new Product();
        System.out.println("\n=== ADD NEW PRODUCT ===");

        // Name
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        try {
            newProduct.setName(name);
        } catch (Product.InvalidProductDataException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Code
        System.out.print("Enter product code: ");
        String code = scanner.nextLine();
        try {
            newProduct.setCode(code);
        } catch (Product.InvalidProductDataException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Category
        System.out.print("Enter product category: ");
        String category = scanner.nextLine();
        try {
            newProduct.setCategory(category);
        } catch (Product.InvalidProductDataException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Price
        System.out.print("Enter price per unit: ");
        String priceInput = scanner.nextLine();
        try {
            double price = Double.parseDouble(priceInput);
            newProduct.setBasePrice(price);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid price format");
            return;
        } catch (Product.InvalidProductDataException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Weekly Offer - Updated to use y/n
        boolean validOfferInput = false;
        boolean isOnOffer = false;
        while (!validOfferInput) {
            System.out.print("Is this product on weekly offer? (y/n): ");
            String offerInput = scanner.nextLine().trim().toLowerCase();

            if (offerInput.equals("y") || offerInput.equals("yes")) {
                isOnOffer = true;
                validOfferInput = true;
            } else if (offerInput.equals("n") || offerInput.equals("no")) {
                isOnOffer = false;
                validOfferInput = true;
            } else {
                System.out.println("Error: Please enter 'y' or 'n'");
            }
        }
        newProduct.setWeeklyOffer(isOnOffer);

        // Quantity
        System.out.print("Enter initial quantity: ");
        String quantityInput = scanner.nextLine();
        try {
            int quantity = Integer.parseInt(quantityInput);
            if (quantity < 0) {
                System.out.println("Error: Quantity cannot be negative");
                return;
            }
            storage.updateQuantity(newProduct.getCode(), quantity);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid quantity format");
            return;
        }

        products.put(newProduct.getCode(), newProduct);
        System.out.println("Product added successfully!");
    }

    /**
     * Modifies product stock levels with validation against negative quantities.
     */
    private void handleUpdateStock(Scanner scanner) {
        System.out.println("\n=== UPDATE STOCK ===");
        try {
            System.out.print("Enter product code: ");
            String productCode = scanner.nextLine();

            if (!products.containsKey(productCode)) {
                System.out.println("Error: Product not found");
                return;
            }

            System.out.print("Enter quantity to add (positive to add, negative to remove): ");
            String quantityInput = scanner.nextLine();
            try {
                int quantity = Integer.parseInt(quantityInput);
                int currentStock = storage.getQuantity(productCode);

                // Check if removing more than available
                if (quantity < 0 && Math.abs(quantity) > currentStock) {
                    System.out.println("Error: Cannot remove " + Math.abs(quantity) +
                            " units. Only " + currentStock + " available.");
                    return;
                }

                // Check if result would be negative
                if ((currentStock + quantity) < 0) {
                    System.out.println("Error: Adjustment would result in negative inventory");
                    return;
                }

                storage.updateQuantity(productCode, quantity);
                System.out.println("Stock updated successfully! New quantity: " +
                        (currentStock + quantity));
            } catch (NumberFormatException e) {
                System.out.println("Error: Quantity must be a whole number");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Toggles weekly offer status for specified products.
     */
    private void handleSetWeeklyOffer(Scanner scanner) {
        System.out.println("\n=== SET WEEKLY OFFER ===");
        try {
            System.out.print("Enter product code: ");
            String productCode = scanner.nextLine();

            Product product = products.get(productCode);
            if (product == null) {
                System.out.println("Error: Product not found");
                return;
            }

            System.out.print("Set as weekly offer? (true/false): ");
            String offerInput = scanner.nextLine().trim().toLowerCase();
            if (!offerInput.equals("true") && !offerInput.equals("false")) {
                System.out.println("Error: Please enter 'true' or 'false'");
                return;
            }

            boolean isOffer = Boolean.parseBoolean(offerInput);
            product.setWeeklyOffer(isOffer);
            System.out.println("Weekly offer status updated successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays products filtered by category.
     */
    private void handleViewByCategory(Scanner scanner) {
        System.out.println("\n=== VIEW BY CATEGORY ===");
        System.out.print("Enter category to view: ");
        String category = scanner.nextLine();
        Product.displayProductsByCategory(category);
    }

    /**
     * Displays complete product catalog.
     */
    private void handleViewAllProducts() {
        Product.displayAllProducts();
    }

    /* ------------------------------
       CUSTOMER MANAGEMENT
       ------------------------------ */

    /**
     * Displays customer management submenu with registration and search options.
     */
    private void showCustomerManagementMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== CUSTOMER MANAGEMENT ===");
            System.out.println("1. Register New Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. View Customer Statistics");
            System.out.println("4. Update Customer Details");
            System.out.println("5. Find Customer by Phone");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleRegisterCustomer(scanner);
                    break;
                case 2:
                    handleViewAllCustomers();
                    break;
                case 3:
                    handleViewCustomerStats(scanner);
                    break;
                case 4:
                    handleUpdateCustomerDetails(scanner);
                    break;
                case 5:
                    handleFindCustomerByPhone(scanner);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /**
     * Guides user through new customer registration process.
     * Validates all inputs and checks for duplicate phone numbers.
     */
    private void handleRegisterCustomer(Scanner scanner) {
        System.out.println("\n=== REGISTER NEW CUSTOMER ===");
        Customer newCustomer = new Customer();

        try {
            // Name
            System.out.print("Enter customer name: ");
            String name = scanner.nextLine();
            newCustomer.setName(name);

            // Surname
            System.out.print("Enter customer surname: ");
            String surname = scanner.nextLine();
            newCustomer.setSurname(surname);

            // Phone (with uniqueness check)
            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine();
            if (Customer.findCustomer(phone) != null) {
                throw new IllegalArgumentException("Phone number already registered");
            }
            newCustomer.setPhone(phone);

            // Email
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            newCustomer.setEmail(email);

            allCustomers.put(newCustomer.getPhone(), newCustomer);
            ArrayList<TransactionLabel> transactionHistory = new ArrayList<TransactionLabel>();
            allTransactionHistory.put(phone, transactionHistory);
            System.out.println("Customer registered successfully!");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Error registering customer: " + e.getMessage());
        }
    }

    /**
     * Displays all registered customers in the system.
     */
    private void handleViewAllCustomers() {
        Customer.displayAllCustomers();
    }

    /**
     * Shows detailed statistics for a specific customer including transaction history.
     */
    private void handleViewCustomerStats(Scanner scanner) {
        System.out.println("\n=== CUSTOMER STATISTICS ===");
        System.out.print("Enter customer phone number: ");
        String phone = scanner.nextLine();

        Customer customer = Customer.findCustomer(phone);
        if (customer != null) {
            displayCustomerDetails(customer);
            displayTransactionHistory(customer);
        } else {
            System.out.println("Customer not found!");
        }
    }

    /**
     * Provides interface for modifying existing customer records.
     */
    private void handleUpdateCustomerDetails(Scanner scanner) {
        System.out.println("\n=== UPDATE CUSTOMER ===");
        System.out.print("Enter customer phone number: ");
        String phone = scanner.nextLine();

        Customer customer = Customer.findCustomer(phone);
        if (customer == null) {
            System.out.println("Customer not found!");
            return;
        }

        while (true) {
            System.out.println("\nCurrent customer details:");
            displayCustomerDetails(customer);
            System.out.println("\nWhat would you like to update?");
            System.out.println("1. Name");
            System.out.println("2. Surname");
            System.out.println("3. Email");
            System.out.println("4. Phone number");
            System.out.println("5. Exit");
            System.out.print("Select option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number 1-4.");
                continue;
            }

            switch (choice) {
                case 1:
                    updateCustomerName(scanner, customer);
                    break;
                case 2:
                    updateCustomerSurname(scanner, customer);
                    break;
                case 3:
                    updateCustomerEmail(scanner, customer);
                    break;
                case 4:
                    updateCustomerPhone(scanner, customer);
                    return;
                case 5:
                    System.out.println("Returning to customer menu...");
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-4.");
            }
        }
    }

    /**
     * Locates customer by phone number and displays their details.
     */
    private void handleFindCustomerByPhone(Scanner scanner) {
        System.out.println("\n=== FIND CUSTOMER BY PHONE ===");
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();

        Customer customer = Customer.findCustomer(phone);
        if (customer != null) {
            displayCustomerDetails(customer);
        } else {
            System.out.println("Customer not found!");
        }
    }

    /* ------------------------------
       CUSTOMER HELPER METHODS
       ------------------------------ */

    /**
     * Displays basic customer information.
     */
    private void displayCustomerDetails(Customer customer) {
        System.out.println(customer);
    }

    /**
     * Displays complete transaction history for specified customer.
     */
    private void displayTransactionHistory(Customer customer) {
        System.out.println("Transaction History:");
        for (TransactionLabel transaction : customer.getTransactionHistory()) {
            System.out.println(" - " + transaction);
        }
    }

    /**
     * Updates customer phone number with uniqueness validation.
     */
    private void updateCustomerPhone(Scanner scanner, Customer customer) {
        System.out.print("Enter new phone number: ");
        String newPhone = scanner.nextLine();
        String cleanPhone = newPhone.trim();
        try {
            if (allCustomers.containsKey(cleanPhone)) {
                throw new InvalidPersonDataException("Phone already registered");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        try {
            customer.setPhone(cleanPhone);
            System.out.println("Phone number updated successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates customer surname with validation.
     */
    private void updateCustomerSurname(Scanner scanner, Customer customer) {
        System.out.print("Enter new surname: ");
        String newSurname = scanner.nextLine();
        try {
            customer.setSurname(newSurname);
            System.out.println("Surname updated successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates customer email with format validation.
     */
    private void updateCustomerEmail(Scanner scanner, Customer customer) {
        System.out.print("Enter new email: ");
        String newEmail = scanner.nextLine();
        try {
            customer.setEmail(newEmail);
            System.out.println("Email updated successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Updates customer name with validation.
     */
    private void updateCustomerName(Scanner scanner, Customer customer) {
        System.out.print("Enter new phone number: ");
        String newName = scanner.nextLine();
        try {
            customer.setName(newName);
            System.out.println("Name updated successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /* ------------------------------
       REPORTING SYSTEM
       ------------------------------ */

    /**
     * Displays reporting submenu with inventory and customer statistics options.
     */
    private void showReportsMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== REPORTS ===");
            System.out.println("1. Generate Inventory Report");
            System.out.println("2. Generate Customer Statistics");
            System.out.println("3. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    generateInventoryReport();
                    break;
                case 2:
                    handleCustomerStatistics(scanner);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /**
     * Handles customer statistics generation by phone number lookup.
     */
    private void handleCustomerStatistics(Scanner scanner) {
        System.out.println("\nEnter customer phone number: ");
        String phone = scanner.nextLine();
        displayCustomerStats(phone);
    }

    /**
     * Displays comprehensive customer statistics including transaction history.
     */
    public void displayCustomerStats(String phone) {
        Customer customer = Customer.findCustomer(phone);
        if (customer == null) {
            System.out.println("Customer not found");
            return;
        }

        System.out.println("\n=== CUSTOMER STATISTICS ===");
        System.out.println(customer); // Uses Customer's toString()

        ArrayList<TransactionLabel> transactions = customer.getTransactionHistory();
        System.out.println("Transaction History:");
        if (transactions.isEmpty()) {
            System.out.println("No Transaction History");
        }
        else {
            System.out.println(transactions);
        }
    }

    /**
     * Generates detailed inventory report including product details and stock levels.
     */
    public void generateInventoryReport() {
        System.out.println("\n=== INVENTORY REPORT ===");
        HashMap<String, Integer> inventory = storage.getAllInventory();

        for (String productCode : inventory.keySet()) {
            Product product = products.get(productCode);
            int quantity = inventory.get(productCode);
            if (product == null) {
                System.out.println("Warning: Product with code " + productCode +
                        " exists in inventory but not in product catalog. Quantity: " + quantity);
                continue;
            }
            System.out.printf("Product: %s | Code: %s | Stock: %d | Price: %.2f€%s%n",
                    product.getName(),
                    productCode,
                    quantity,
                    product.getBasePrice(),
                    (product.isOnWeeklyOffer() ? " (ON OFFER)" : ""));
        }
    }

    /* ------------------------------
       OBJECT OVERRIDES
       ------------------------------ */

    /**
     * Compares Manager objects based on Employee equality.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        return true;
    }

    /**
     * Returns string representation using Employee's toString().
     */
    @Override
    public String toString() {
        return super.toString();
    }
}