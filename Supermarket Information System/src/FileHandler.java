/*
 * Central data persistence manager for supermarket system.
 * Handles all file I/O operations for:
 * - Employee records
 * - Customer profiles
 * - Transaction history
 * - Product catalog
 * - Inventory levels
 * Implements robust error handling and status tracking.
 */
import java.io.*;
import java.util.*;
import java.time.LocalDate;

class FileHandler {

    /*
     * In-memory data stores for all business entities.
     * Collections chosen for specific access patterns:
     * - LinkedHashSet maintains employee insertion order
     * - HashMaps enable O(1) lookups by key fields
     */
    private static final LinkedHashSet<Employee> employees = new LinkedHashSet<>();
    private static final HashMap<String, Customer> customers = new HashMap<>();
    private static final HashMap<String, ArrayList<TransactionLabel>> customerTransactions = new HashMap<>();
    private static final HashMap<String, Product> products = new HashMap<>();
    private static final HashMap<String, Integer> inventory = new HashMap<>();
    private ArrayList<String> DATA_FILES;

    /*
     * Load operation status flags.
     * Track successful initialization of each data type.
     */
    private boolean employeesLoaded = false;
    private boolean customersLoaded = false;
    private boolean transactionsLoaded = false;
    private boolean productsLoaded = false;
    private boolean inventoryLoaded = false;

    /*
     * Save operation status flags.
     * Track successful persistence of each data type.
     */
    private boolean employeesSaved = false;
    private boolean customersSaved = false;
    private boolean transactionsSaved = false;
    private boolean productsSaved = false;
    private boolean inventorySaved = false;

    /*
     * Custom configuration constructor.
     * Accepts list of data file paths for flexible deployment.
     */
    public FileHandler(ArrayList<String> DATA_FILES) {
        this.DATA_FILES = DATA_FILES;
    }

    /*
     * Default constructor.
     * Assumes standard file names in application directory.
     */
    public FileHandler() {}

    /*
     * Bulk data loader.
     * Processes all configured files sequentially.
     * Maintains system-wide data consistency.
     */
    public void loadAllFiles() {
        for (String fileName : DATA_FILES) {
            loadData(fileName);
        }
        checkLoadStatus();
    }

    /*
     * Bulk data saver.
     * Persists all data types sequentially.
     * Ensures atomic operation per file.
     */
    public void saveAllFiles() {
        for (String fileName : DATA_FILES) {
            saveData(fileName);
        }
        checkSavedStatus();
    }

    /*
     * File loading dispatcher.
     * Routes file processing based on naming convention.
     * Implements graceful error handling.
     */
    private void loadData(String fileName) {
        try {
            switch (fileName) {
                case "employees.txt":
                    loadEmployeeData();
                    break;
                case "customers.txt":
                    loadCustomerData();
                    break;
                case "customer_transactions.txt":
                    loadCustomerTransactionData();
                    break;
                case "products.txt":
                    loadProductData();
                    break;
                case "inventory.txt":
                    loadInventoryData();
                    break;
                default:
                    System.out.println("Unknown file type: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("Error loading " + fileName + ": " + e.getMessage());
        }
    }

    /*
     * File saving dispatcher.
     * Routes persistence based on naming convention.
     * Implements graceful error handling.
     */
    public void saveData(String fileName) {
        try {
            switch (fileName) {
                case "employees.txt":
                    saveEmployeeData();
                    break;
                case "customers.txt":
                    saveCustomerData();
                    break;
                case "customer_transactions.txt":
                    saveCustomerTransactions();
                    break;
                case "products.txt":
                    saveProductData();
                    break;
                case "inventory.txt":
                    saveInventoryData();
                    break;
                default:
                    System.out.println("Unknown file type: " + fileName);
            }
        } catch (Exception e) {
            System.out.println("Error saving " + fileName + ": " + e.getMessage());
        }
    }

    /*
     * Load operation integrity checker.
     * Verifies all data types loaded successfully.
     * Provides user feedback on initialization status.
     */
    private void checkLoadStatus() {
        boolean allLoaded = employeesLoaded && customersLoaded &&
                transactionsLoaded && productsLoaded && inventoryLoaded;

        if (!allLoaded) {
            System.out.println("Warning: Not all files loaded successfully");
        }
        else {
            System.out.println("All data loaded successfully!");
        }
    }

    /*
     * Save operation integrity checker.
     * Verifies all data types persisted successfully.
     * Provides user feedback on persistence status.
     */
    private void checkSavedStatus() {
        boolean allSaved = employeesSaved && customersSaved &&
                transactionsSaved && productsSaved && inventorySaved;

        if (!allSaved) {
            System.out.println("Warning: Not all files saved successfully");
        }
        else {
            System.out.println("All data saved successfully!");
        }
    }

    /*
     * Employee data loader.
     * File format: id,username,password,name,surname,role,active
     * Maintains insertion order for reporting consistency.
     */
    private void loadEmployeeData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("employees.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 7) {
                    String id = data[0].trim();
                    String username = data[1].trim();
                    String password = data[2].trim();
                    String name = data[3].trim();
                    String surname = data[4].trim();
                    String role = data[5].trim();
                    String active = data[6].trim();
                    Boolean isActive = Boolean.valueOf(active);

                    Employee employee = new Employee(id, username, password, name, surname, role, isActive);
                    employees.add(employee);
                }
            }
            employeesLoaded = true;
        } catch (FileNotFoundException e) {
            System.out.println("Employee file not found. A new file will be created.");
            employeesLoaded = false;
        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
            employeesLoaded = false;
        }
    }

    /*
     * Employee data saver.
     * Maintains CSV format consistency with loader.
     * Overwrites file completely each save.
     */
    public void saveEmployeeData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("employees.txt", false))) {
            for (Employee emp : employees) {
                writer.write(
                        emp.getPassportId() + "," +
                                emp.getUsername() + "," +
                                emp.getPassword() + "," +
                                emp.getName() + "," +
                                emp.getSurname() + "," +
                                emp.getRole().getCode() + "," +
                                emp.isActive()
                );
                writer.newLine();
            }
            employeesSaved = true;
        } catch (IOException e) {
            System.out.println("Error saving employees: " + e.getMessage());
            employeesSaved = false;
        }
    }

    /*
     * Employee data accessor.
     * Returns shallow copy to prevent external modification.
     */
    public LinkedHashSet<Employee> getEmployeeData() {
        return employees;
    }

    /*
     * Customer data loader.
     * File format: name:surname:phone:email:points
     * Uses phone number as natural primary key.
     */
    public void loadCustomerData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] data = line.split(":");
                    if (data.length == 5) {
                        String name = data[0].trim();
                        String surname = data[1].trim();
                        String phone = data[2].trim();
                        String email = data[3].trim();
                        int bonusCardBalance = Integer.parseInt(data[4].trim());

                        Customer customer = new Customer(name, surname, phone, email, bonusCardBalance);
                        customers.put(phone, customer);
                    }
                }
            }
            customersLoaded = true;
        } catch (FileNotFoundException e) {
            System.out.println("Customer file not found. A new file will be created.");
            customersLoaded = false;
        } catch (IOException e) {
            System.out.println("Error reading customer file: " + e.getMessage());
            customersLoaded = false;
        }
    }

    /*
     * Customer data saver.
     * Maintains colon-delimited format consistency.
     */
    public void saveCustomerData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("customers.txt", false))) {
            for (Customer customer : customers.values()) {
                writer.write(
                        customer.getName() + ":" +
                                customer.getSurname() + ":" +
                                customer.getPhone() + ":" +
                                customer.getEmail() + ":" +
                                customer.getBonusPoints()
                );
                writer.newLine();
            }
            customersSaved = true;
        } catch (IOException e) {
            System.out.println("Error saving customer data: " + e.getMessage());
            customersSaved = false;
        }
    }

    /*
     * Customer data accessor.
     * Returns shallow copy to prevent external modification.
     */
    public HashMap<String, Customer> getCustomers() {
        return customers;
    }

    /*
     * Transaction history loader.
     * File format: phone:date:amount
     * Groups transactions by customer phone number.
     */
    private void loadCustomerTransactionData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("customer_transactions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String phone = parts[0].trim();
                    if (phone.isEmpty()) continue;

                    try {
                        LocalDate date = LocalDate.parse(parts[1].trim());
                        double amount = Double.parseDouble(parts[2].trim());

                        ArrayList<TransactionLabel> transactions;
                        if (customerTransactions.containsKey(phone)) {
                            transactions = customerTransactions.get(phone);
                        } else {
                            transactions = new ArrayList<>();
                            customerTransactions.put(phone, transactions);
                        }
                        transactions.add(new TransactionLabel(date, amount));
                    } catch (Exception e) {
                        System.out.println("Skipping invalid transaction: " + line);
                    }
                }
            }
            transactionsLoaded = true;
        } catch (IOException e) {
            System.out.println("Error reading transaction file: " + e.getMessage());
            transactionsLoaded = false;
        }
    }

    /*
     * Transaction history saver.
     * Maintains colon-delimited format consistency.
     */
    public void saveCustomerTransactions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("customer_transactions.txt", false))) {
            for (String phone : customerTransactions.keySet()) {
                ArrayList<TransactionLabel> transactions = customerTransactions.get(phone);
                for (TransactionLabel transaction : transactions) {
                    writer.write(phone + ":" +
                            transaction.getDate() + ":" +
                            transaction.getAmount());
                    writer.newLine();
                }
            }
            transactionsSaved = true;
        } catch (IOException e) {
            System.out.println("Error saving transaction file: " + e.getMessage());
            transactionsSaved = false;
        }
    }

    /*
     * Transaction data accessor.
     * Returns shallow copy to prevent external modification.
     */
    public HashMap<String, ArrayList<TransactionLabel>> getCustomerTransactions() {
        return customerTransactions;
    }

    /*
     * Customer-specific transaction accessor.
     * Returns null for unknown customers.
     */
    public ArrayList<TransactionLabel> getCustomerTransactions(String phone) {
        if (!customers.containsKey(phone)) {
            return null;
        }
        return customerTransactions.get(phone);
    }

    /*
     * Product catalog loader.
     * File format: name:code:category:price:offer
     * Uses product code as primary key.
     */
    public void loadProductData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("products.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] data = line.split(":");
                    if (data.length == 5) {
                        String name = data[0].trim();
                        String code = data[1].trim();
                        String category = data[2].trim();
                        double pricePerUnit = Double.parseDouble(data[3].trim());
                        boolean weeklyOffer = Boolean.parseBoolean(data[4].trim());

                        Product product = new Product(name, code, category, pricePerUnit, weeklyOffer);
                        products.put(code, product);
                    }
                }
            }
            productsLoaded = true;
        } catch (FileNotFoundException e) {
            System.out.println("Product file not found. A new file will be created.");
            productsLoaded = false;
        } catch (IOException e) {
            System.out.println("Error reading product file: " + e.getMessage());
            productsLoaded = false;
        }
    }

    /*
     * Product catalog saver.
     * Maintains colon-delimited format consistency.
     */
    public void saveProductData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("products.txt", false))) {
            for (Product product : products.values()) {
                String line = product.getName() + ":" + product.getCode() + ":" + product.getCategory() +
                        ":" + product.getBasePrice() + ":" + product.getIsOnWeeklyOffer();
                writer.write(line);
                writer.newLine();
            }
            productsSaved = true;
        } catch (IOException e) {
            System.out.println("Error saving product data: " + e.getMessage());
            productsSaved = false;
        }
    }

    /*
     * Product data accessor.
     * Returns shallow copy to prevent external modification.
     */
    public HashMap<String, Product> getProducts() {
        return products;
    }

    /*
     * Inventory data loader.
     * File format: code:quantity
     * Tracks current stock levels by product code.
     */
    public void loadInventoryData() {
        try (BufferedReader reader = new BufferedReader(new FileReader("inventory.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String productCode = parts[0].trim();
                        int amount = Integer.parseInt(parts[1].trim());
                        inventory.put(productCode, amount);
                    }
                }
            }
            inventoryLoaded = true;
        } catch (FileNotFoundException e) {
            System.out.println("Inventory file not found. A new file will be created.");
            inventoryLoaded = false;
        } catch (IOException e) {
            System.out.println("Error reading inventory file: " + e.getMessage());
            inventoryLoaded = false;
        } catch (NumberFormatException e) {
            System.out.println("Error parsing inventory quantity: " + e.getMessage());
            inventoryLoaded = false;
        }
    }

    /*
     * Inventory data saver.
     * Maintains colon-delimited format consistency.
     */
    public void saveInventoryData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("inventory.txt", false))) {
            for (HashMap.Entry<String, Integer> entry : inventory.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
            inventorySaved = true;
        } catch (IOException e) {
            System.out.println("Error saving inventory data: " + e.getMessage());
            inventorySaved = false;
        }
    }

    /*
     * Inventory data accessor.
     * Returns defensive copy to prevent external modification.
     */
    public HashMap<String, Integer> getInventory() {
        return inventory;
    }

    /*
     * System status summary.
     * Provides counts of all loaded entities.
     */
    @Override
    public String toString() {
        int employeeCount = employees != null ? employees.size() : 0;
        int customerCount = customers != null ? customers.size() : 0;
        int productCount = products != null ? products.size() : 0;
        int inventoryCount = inventory != null ? inventory.size() : 0;

        int transactionCount = 0;
        if (customerTransactions != null) {
            for (ArrayList<TransactionLabel> transactions : customerTransactions.values()) {
                if (transactions != null) {
                    transactionCount += transactions.size();
                }
            }
        }

        return "FileHandler[" +
                "employees=" + employeeCount +
                ", customers=" + customerCount +
                ", transactions=" + transactionCount +
                ", products=" + productCount +
                ", inventory=" + inventoryCount + "]";
    }

    /*
     * Deep equality comparison.
     * Verifies all data collections match exactly.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileHandler that = (FileHandler) o;
        return employees.equals(that.employees) &&
                customers.equals(that.customers) &&
                customerTransactions.equals(that.customerTransactions) &&
                products.equals(that.products) &&
                inventory.equals(that.inventory);
    }
}