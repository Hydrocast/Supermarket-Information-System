import java.util.LinkedHashSet;
import java.util.Objects;

/*
 * Enumeration representing predefined employee roles within the organization.
 * Encapsulates role codes and descriptions to ensure type safety and
 * centralized role management across the system.
 */
enum EmployeeRole {
    MANAGER("101", "Manager"),
    HR_MANAGER("102", "Human Resources Manager"),
    CASHIER("103", "Cashier");

    private final String code;        // Unique identifier for each role
    private final String description; // Human-readable role name

    /*
     * Private constructor ensures enum constants are the only possible instances.
     * Enforces immutable role definitions at compile time.
     */
    private EmployeeRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Returns the machine-readable role code (e.g., "101")
    public String getCode() {
        return this.code;
    }

    // Returns the human-friendly role description (e.g., "Manager")
    public String getDescription() {
        return this.description;
    }

    /*
     * Factory method to retrieve enum constant by code.
     * Throws IllegalArgumentException for invalid codes to fail fast.
     */
    public static EmployeeRole getRoleByCode(String code) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + code +
                ". Valid codes are: 101 (Manager), 102 (HR Manager), 103 (Cashier)");
    }

    /*
     * Validates whether a given string corresponds to a defined role code.
     * Used for input validation before role assignment.
     */
    public static boolean isValidRoleCode(String code) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Provides formatted string representation combining both
     * description and code for debugging and display purposes.
     */
    @Override
    public String toString() {
        return this.description + " (" + this.code + ")";
    }
}

/*
 * Core class representing system users with authentication capabilities.
 * Extends Person to inherit basic identity attributes while adding
 * role-based access control and employment-specific properties.
 */
class Employee extends Person {

    private String passportId;  // Government-issued unique identifier
    private String username;    // System login credential
    private String password;    // Authentication secret (plaintext)
    private EmployeeRole role;  // Determines system permissions
    private boolean active = true;  // Tracks employment status

    /*
     * Centralized employee data store loaded from persistent storage.
     * LinkedHashSet maintains insertion order while preventing duplicates.
     */
    private static final FileHandler fileHandler = new FileHandler();
    private static final LinkedHashSet<Employee> allEmployees = fileHandler.getEmployeeData();

    /*
     * Primary constructor for creating fully-configured employee instances.
     * Performs validation through setter methods to ensure object consistency.
     */
    public Employee(String passportId, String username, String password,
                    String name, String surname, String roleCode, Boolean active) {
        super(name, surname);
        setPassportId(passportId);
        setUsername(username);
        setPassword(password);
        setActive(active);
        setRole(roleCode);
    }

    /*
     * Protected no-arg constructor for use by:
     * 1. Subclasses (HRManager, Cashier, etc.)
     * 2. Serialization frameworks
     */
    protected Employee() {}

    // --- Setters with Validation ---

    /*
     * Sets passport ID with length validation.
     * Trims whitespace to ensure consistent storage.
     */
    protected void setPassportId(String passportId) {
        if (passportId == null || passportId.trim().isEmpty()) {
            throw new IllegalArgumentException("Passport ID cannot be empty");
        }
        if (passportId.trim().length() > 30) {
            throw new IllegalArgumentException("Passport ID must be between 1 and 30 characters.");
        }
        this.passportId = passportId.trim();
    }

    /*
     * Configures username with length constraints.
     * Prevents system-wide duplicates through external validation.
     */
    protected void setUsername(String username) {
        if (username == null || username.trim().length() < 2 || username.trim().length() > 15) {
            throw new IllegalArgumentException("Username must be between 2 and 15 characters.");
        }
        this.username = username.trim();
    }

    /*
     * Stores password with basic complexity requirements.
     * Note: In production, should hash passwords before storage.
     */
    protected void setPassword(String password) {
        if (password == null || password.trim().length() < 6 || password.trim().length() > 30) {
            throw new IllegalArgumentException("Password must be between 6 and 30 characters.");
        }
        this.password = password.trim();
    }

    /*
     * Assigns role after validating the provided code.
     * Uses EmployeeRole enum to ensure only valid roles can be assigned.
     */
    public void setRole(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Role code cannot be empty.");
        }
        String trimmedCode = roleCode.trim();
        if (!trimmedCode.equals(EmployeeRole.CASHIER.getCode()) &&
                !trimmedCode.equals(EmployeeRole.HR_MANAGER.getCode()) &&
                !trimmedCode.equals(EmployeeRole.MANAGER.getCode())) {
            throw new IllegalArgumentException("Invalid role code. Valid Role Codes: 101 = Manager, " +
                    "102 = HR Manager, 103 = Cashier.");
        }
        this.role = EmployeeRole.getRoleByCode(trimmedCode);
    }

    // Toggles employee active status (true = employed, false = terminated)
    public void setActive(boolean active) {
        this.active = active;
    }

    // --- Getters ---

    public String getPassportId() {
        return passportId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    // --- Static Accessors ---

    /*
     * Returns immutable view of all registered employees.
     * Includes both active and inactive records.
     */
    public static LinkedHashSet<Employee> getAllEmployees() {
        return allEmployees;
    }

    /*
     * Filters and returns only currently active employees.
     * Creates new collection to avoid modifying original data.
     */
    public static LinkedHashSet<Employee> getAllActiveEmployees() {
        LinkedHashSet<Employee> activeEmployees = new LinkedHashSet<>();
        for (Employee emp : allEmployees) {
            if (emp.active) {
                activeEmployees.add(emp);
            }
        }
        return activeEmployees;
    }

    // --- Authentication and Management ---

    /*
     * Verifies credentials against stored values.
     * Returns false if employee is inactive, regardless of credentials.
     */
    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password) && active;
    }

    /*
     * Locates employee by passport ID using linear search.
     * Returns null if no match found.
     */
    public static Employee findById(String passportId) {
        for (Employee emp : allEmployees) {
            if (emp.passportId.equals(passportId)) {
                return emp;
            }
        }
        return null;
    }

    /*
     * Finds employee by username (case-sensitive).
     * Useful for login validation and account management.
     */
    public static Employee findByUsername(String username) {
        for (Employee emp : allEmployees) {
            if (emp.getUsername().equals(username)) {
                return emp;
            }
        }
        return null;
    }

    /*
     * Removes employee from system entirely.
     * Returns true if removal succeeded, false if employee not found.
     */
    public static boolean deleteEmployee(String passportId) {
        Employee employeeToRemove = null;
        for (Employee emp : allEmployees) {
            if (emp.getPassportId().equals(passportId)) {
                employeeToRemove = emp;
                break;
            }
        }
        if (employeeToRemove != null) {
            allEmployees.remove(employeeToRemove);
            return true;
        }
        return false;
    }

    /*
     * Soft-delete operation - retains employee record
     * but marks as inactive to preserve historical data.
     */
    public void deactivateEmployee() {
        active = false;
    }

    // Reverses deactivation to restore system access
    public void activateEmployee() {
        active = true;
    }

    // --- Utility Methods ---

    /*
     * Diagnostic method to print all employees to console.
     * Formats output using toString() representation.
     */
    public void listAllEmployees() {
        System.out.println("\n=== ALL EMPLOYEES ===");
        for (Employee emp : allEmployees) {
            System.out.println(emp);
        }
    }

    /*
     * Defines equality based solely on passport ID.
     * Consistent with database primary key conventions.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(passportId, employee.passportId);
    }

    /*
     * Comprehensive string representation for logging and display.
     * Includes all key attributes except password for security.
     */
    @Override
    public String toString() {
        return "Passport: " + passportId +
                " | Username: " + username +
                " | Name: " + getName() + " " + getSurname() +
                " | Role: " + role.getDescription() +
                " | Status: " + (active ? "Active" : "Inactive");
    }
}