import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.InputMismatchException;

/**
 * Displays the main menu, handles login logic, and routes users to their respective roles.
 * Acts as the gateway for all user interactions post-system-initialization.
 */
public class MainMenu {
    // Shared file handler used to retrieve employee data
    // Static to ensure single instance across all menu interactions
    private static final FileHandler fileHandler = new FileHandler();

    // Set of all employees loaded from file
    // LinkedHashSet preserves insertion order while ensuring uniqueness
    private static final LinkedHashSet<Employee> employees = fileHandler.getEmployeeData();

    // Scanner instance for user input
    // Shared to avoid resource leaks and maintain input consistency
    private static final Scanner scanner = new Scanner(System.in);

    // Reference to the control system for shutdown coordination
    // Final to prevent reassignment after constructor
    private final ControlSystem controlSystem;

    /**
     * Constructor links this menu to the main control system.
     * Establishes bidirectional communication for system-wide operations.
     */
    public MainMenu(ControlSystem controlSystem) {
        this.controlSystem = controlSystem;
    }

    /**
     * Displays the initial login menu and responds to user input.
     * Implements infinite loop to maintain session until explicit exit.
     */
    public void showLoginMenu() {
        while (true) {
            try {
                System.out.println("\n=== MAIN MENU ===");
                System.out.println("1. Login");
                System.out.println("2. Exit");
                System.out.print("Select option: ");

                // Validate input range (1-2) to prevent illegal menu choices
                int choice = getValidIntegerInput(1, 2);

                switch (choice) {
                    case 1:
                        handleLogin();  // Proceed to credential validation
                        break;
                    case 2:
                        System.out.println("Exiting system...");
                        handleExitOption();  // Trigger controlled shutdown
                        System.exit(0);     // Fallback termination
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());  // User-friendly error feedback
            }
        }
    }

    /**
     * Handles login interaction and redirects authenticated users to the appropriate role menu.
     * Implements:
     * 1. Username/password input with whitespace trimming
     * 2. Empty input validation
     * 3. Authentication attempt
     */
    private static void handleLogin() {
        try {
            System.out.print("\nEnter username: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");  // Guard clause
            }

            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");  // Guard clause
            }

            Employee authenticatedUser = authenticate(username, password);

            if (authenticatedUser != null) {
                System.out.println("\nLogin successful! Welcome " + authenticatedUser.getName());
                redirectToRoleMenu(authenticatedUser);  // Role-based routing
            } else {
                System.out.println("Invalid username or password");  // Authentication failed
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());  // General error handling
        }
    }

    /**
     * Verifies user credentials against the loaded employee data.
     * Iterates through employee set using linear search (O(n)).
     * Returns null if no match found.
     */
    private static Employee authenticate(String username, String password) {
        for (Employee emp : employees) {
            if (emp.authenticate(username, password)) {
                return emp;  // Early exit on successful match
            }
        }
        return null;  // Explicit failure indicator
    }

    /**
     * Routes authenticated employees to role-specific menus based on role code.
     * Throws IllegalArgumentException for unknown role codes.
     * Instantiates role-specific objects to maintain separation of concerns.
     */
    private static void redirectToRoleMenu(Employee employee) {
        try {
            switch (employee.getRole().getCode()) {
                case "101": // Manager
                    Manager manager = new Manager(
                            employee.getPassportId(),
                            employee.getUsername(),
                            employee.getPassword(),
                            employee.getName(),
                            employee.getSurname()
                    );
                    manager.showManagerMenu();  // Delegates to Manager's UI flow
                    break;

                case "102": // HR Manager
                    HRManager hrManager = new HRManager(
                            employee.getPassportId(),
                            employee.getUsername(),
                            employee.getPassword(),
                            employee.getName(),
                            employee.getSurname()
                    );
                    hrManager.showHRMenu();  // Delegates to HRManager's UI flow
                    break;

                case "103": // Cashier
                    Cashier cashier = new Cashier(
                            employee.getPassportId(),
                            employee.getUsername(),
                            employee.getPassword(),
                            employee.getName(),
                            employee.getSurname()
                    );
                    cashier.showCashierMenu();  // Delegates to Cashier's UI flow
                    break;

                default:
                    throw new IllegalArgumentException("Unknown role type: " + employee.getRole().getCode());
            }
        } catch (Exception e) {
            System.out.println("Error redirecting to role menu: " + e.getMessage());
        }
    }

    /**
     * Initiates system shutdown through the control system.
     * Wraps shutdown request in try-catch for fault tolerance.
     */
    private void handleExitOption() {
        try {
            controlSystem.requestShutdown();  // Delegates shutdown to ControlSystem
        } catch (Exception e) {
            System.out.println("Error during shutdown: " + e.getMessage());
        }
    }

    /**
     * Validates integer input within specified bounds.
     * Handles:
     * 1. Non-integer inputs (InputMismatchException)
     * 2. Out-of-range values (IllegalArgumentException)
     * Consumes newline to prevent scanner state corruption.
     */
    private int getValidIntegerInput(int min, int max) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // consume newline to prevent input bleeding
                if (input < min || input > max) {
                    throw new IllegalArgumentException("Please enter a number between " + min + " and " + max);
                }
                return input;
            } catch (InputMismatchException e) {
                scanner.nextLine(); // clear invalid input from buffer
                throw new IllegalArgumentException("Invalid input. Please enter a number.");
            }
        }
    }
}