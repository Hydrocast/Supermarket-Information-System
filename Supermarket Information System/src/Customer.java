import java.util.ArrayList;
import java.util.HashMap;

/*
 * Represents a supermarket customer with extended attributes beyond basic Person data.
 * Manages:
 * - Contact information (phone/email)
 * - Loyalty program participation (bonus points)
 * - Purchase history tracking
 * Integrates with persistent storage via FileHandler singleton.
 */
public class Customer extends Person {
    // Unique customer identifier (primary key)
    private String phone;

    // Contact information for receipts/notifications
    private String email;

    // Loyalty points accumulated (1 point per €100 spent)
    private int bonusPoints;

    // Shared file handler instance for data persistence
    private static final FileHandler fileHandler = new FileHandler();

    // Complete transaction registry (phone -> transaction list)
    private static final HashMap<String, ArrayList<TransactionLabel>> allTransactionHistory = fileHandler.getCustomerTransactions();

    // Instance-specific transaction records
    private ArrayList<TransactionLabel> transactionHistory;

    // Central customer registry (phone -> Customer)
    private static final HashMap<String, Customer> allCustomers = fileHandler.getCustomers();

    /* ----------------------------------------
       CONSTRUCTORS
       ---------------------------------------- */

    /*
     * Primary constructor for new customers.
     * Initializes with:
     * - Zero bonus points
     * - Empty transaction history
     * - Validated contact details
     */
    public Customer(String name, String surname, String phone, String email) {
        super(name, surname);
        setPhone(phone);  // Validated assignment
        setEmail(email);  // Validated assignment
        this.bonusPoints = 0;
        transactionHistory = new ArrayList<TransactionLabel>();
        allTransactionHistory.put(phone, this.transactionHistory);
    }

    /*
     * Restoration constructor for existing customers.
     * Loads:
     * - Pre-existing bonus points
     * - Historical transactions (if any)
     */
    public Customer(String name, String surname, String phone, String email, int bonusPoints) {
        super(name, surname);
        setPhone(phone);
        setEmail(email);
        this.bonusPoints = bonusPoints;
        // Attempt to load existing transaction history
        if ((transactionHistory = fileHandler.getCustomerTransactions(phone)) == null) {
            transactionHistory = new ArrayList<TransactionLabel>();
            allTransactionHistory.put(phone, transactionHistory);
        }
    }

    /*
     * Default constructor for serialization/placeholder use.
     * Requires manual property assignment post-instantiation.
     */
    public Customer() {
        this.bonusPoints = 0;
    }

    /* ----------------------------------------
       SETTERS WITH VALIDATION
       ---------------------------------------- */

    /*
     * Phone number mutator with strict validation:
     * - Non-empty
     * - 4-20 characters
     * - Digits only
     * Serves as natural primary key for customer records.
     */
    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidPersonDataException("Phone number cannot be empty");
        }
        String cleanPhone = phone.trim();

        if (cleanPhone.length() < 4 || cleanPhone.length() > 20) {
            throw new InvalidPersonDataException("Phone must be 4-20 digits");
        }

        // Verify numeric-only composition
        for (char c : cleanPhone.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new InvalidPersonDataException("Phone must contain only digits");
            }
        }
        this.phone = cleanPhone;
    }

    /*
     * Email mutator with basic format validation:
     * - Requires '@' and '.' characters
     * - Trims whitespace
     * Note: Does not validate full RFC standards compliance
     */
    public void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new InvalidPersonDataException("Invalid email format");
        }
        this.email = email.trim();
    }

    /* ----------------------------------------
       TRANSACTION AND BONUS POINT MANAGEMENT
       ---------------------------------------- */

    /*
     * Records a new transaction in customer history.
     * Does not automatically update bonus points -
     * must call addBonusPoints() separately.
     */
    public void addTransaction(TransactionLabel transaction) {
        transactionHistory.add(transaction);
    }

    /*
     * Awards bonus points based on purchase amount.
     * Conversion rate: 1 point per €100 spent.
     * Truncates fractional points (floor division).
     */
    public void addBonusPoints(double amount) {
        bonusPoints += (int) amount;
    }

    /*
     * Redeems bonus points for purchase discount.
     * Exchange rate: 100 points = €1 discount.
     * Applies maximum possible discount without exceeding:
     * - Available points
     * - Transaction amount
     * Returns actual discount applied.
     */
    public double useBonusPoints(double amount) {
        int maxPossibleDiscount = this.bonusPoints / 100;
        double discountToApply = Math.min(amount, maxPossibleDiscount);
        int pointsToUse = (int) (discountToApply * 100);
        this.bonusPoints -= pointsToUse;
        return discountToApply;
    }

    /* ----------------------------------------
       GETTERS
       ---------------------------------------- */

    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getBonusPoints() { return bonusPoints; }
    public ArrayList<TransactionLabel> getTransactionHistory() { return transactionHistory; }
    public static HashMap<String, ArrayList<TransactionLabel>> getAllTransactionHistory() { return allTransactionHistory; }

    /* ----------------------------------------
       STATIC METHODS FOR CUSTOMER MANAGEMENT
       ---------------------------------------- */

    /*
     * Displays all registered customers in system.
     * Output format:
     * Name Surname | Phone | Email | Bonus Points | Transaction Count
     */
    public static void displayAllCustomers() {
        System.out.println("\n=== REGISTERED CUSTOMERS ===");
        for (Customer customer : allCustomers.values()) {
            System.out.println(customer);
        }
    }

    /*
     * Customer lookup by primary key (phone).
     * Returns null if no matching customer found.
     */
    public static Customer findCustomer(String phone) {
        return allCustomers.get(phone);
    }

    /*
     * Retrieves complete customer registry.
     * Returns defensive copy to prevent modification.
     */
    public static ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(allCustomers.values());
    }

    /* ----------------------------------------
       OVERRIDES (toString, equals)
       ---------------------------------------- */

    /*
     * Standardized customer string representation.
     * Includes:
     * - Full name
     * - Contact details
     * - Loyalty points
     * - Transaction count
     */
    @Override
    public String toString() {
        return getName() + " " + getSurname() +
                " | Phone: " + phone +
                " | Email: " + email +
                " | Bonus: " + bonusPoints + " points" +
                " | Transactions: " + (transactionHistory == null ? "No transaction history" : transactionHistory.size());
    }

    /*
     * Equality comparison based solely on phone number.
     * Consistent with phone-as-primary-key design.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer other = (Customer) obj;
        return phone.equals(other.phone);
    }
}