import java.util.LinkedHashSet;
import java.util.Scanner;

/*
 * Specialized Employee subclass with exclusive HR administration privileges.
 * Handles all personnel management operations including:
 * - Employee lifecycle (creation/modification/deletion)
 * - Role assignment
 * - System access control
 */
public class HRManager extends Employee {

    // Centralized file handler for persistent employee data storage
    private static final FileHandler fileHandler = new FileHandler();

    // In-memory employee registry loaded from persistent storage
    // LinkedHashSet maintains insertion order while preventing duplicates
    private static final LinkedHashSet<Employee> allEmployees = fileHandler.getEmployeeData();

    /*
     * Constructs an HRManager with predefined HR role and active status.
     * Leverages superclass constructor with hardcoded role parameters.
     */
    public HRManager(String passportId, String username, String password,
                     String name, String surname) {
        super(passportId, username, password, name, surname, EmployeeRole.HR_MANAGER.getCode(), true);
    }

    /*
     * Main HR management interface displaying all authorized operations.
     * Implements infinite loop until explicit exit (option 7).
     * Each menu option delegates to specialized handler methods.
     */
    public void showHRMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== HR MANAGER MENU ===");
            System.out.println("1. Create New Employee");
            System.out.println("2. Delete Employee");
            System.out.println("3. Update Employee Details");
            System.out.println("4. List All Employees");
            System.out.println("5. Find Employee by ID");
            System.out.println("6. Find Employee by Username");
            System.out.println("7. Exit");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    handleCreateEmployee(scanner);  // Full employee onboarding
                    break;
                case 2:
                    handleDeleteEmployee(scanner);  // Permanent removal
                    break;
                case 3:
                    handleUpdateDetails(scanner);   // Selective field updates
                    break;
                case 4:
                    listAllEmployees();            // Complete registry dump
                    break;
                case 5:
                    handleFindById(scanner);       // ID-based lookup
                    break;
                case 6:
                    handleFindByUsername(scanner); // Username-based lookup
                    break;
                case 7:
                    return;  // Exit menu loop
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /*
     * Guides HR through new employee creation workflow.
     * Implements multistep validation for:
     * 1. Passport ID uniqueness
     * 2. Username availability
     * 3. Password complexity
     * 4. Role validity
     */
    private void handleCreateEmployee(Scanner scanner) {
        System.out.println("\n=== CREATE NEW EMPLOYEE ===");
        Employee newEmployee = new Employee();

        try {
            // Passport ID validation
            System.out.print("Enter passport ID: ");
            String passportId = scanner.nextLine();
            if (Employee.findById(passportId) != null) {
                throw new IllegalArgumentException("Passport ID already exists in the system.");
            }
            newEmployee.setPassportId(passportId);

            // Username validation
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            if (Employee.findByUsername(username) != null) {
                throw new IllegalArgumentException("Username already exists in the system.");
            }
            newEmployee.setUsername(username);

            // Password setting
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            newEmployee.setPassword(password);

            // Personal details
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            newEmployee.setName(name);

            System.out.print("Enter surname: ");
            String surname = scanner.nextLine();
            newEmployee.setSurname(surname);

            // Role assignment
            System.out.print("Enter role code (" +
                    EmployeeRole.MANAGER.getCode() + " = Manager, " +
                    EmployeeRole.HR_MANAGER.getCode() + " = HR Manager, " +
                    EmployeeRole.CASHIER.getCode() + " = Cashier): ");
            String roleCode = scanner.nextLine();
            newEmployee.setRole(roleCode);

            // Finalize creation
            allEmployees.add(newEmployee);
            System.out.println("Employee created successfully!");

        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /*
     * Handles employee termination workflow.
     * Requires exact passport ID match for deletion.
     * Provides success/failure feedback.
     */
    private void handleDeleteEmployee(Scanner scanner) {
        System.out.println("\n=== DELETE EMPLOYEE ===");
        System.out.print("Enter passport ID of employee to delete: ");
        String passportId = scanner.nextLine();

        if (Employee.deleteEmployee(passportId)) {
            System.out.println("Employee deleted successfully!");
        } else {
            System.out.println("Employee not found!");
        }
    }

    /*
     * Interactive employee modification interface.
     * Supports selective field updates with real-time validation.
     * Implements sub-menu for granular control.
     */
    private void handleUpdateDetails(Scanner scanner) {
        System.out.println("\n=== UPDATE EMPLOYEE ===");
        System.out.print("Enter passport ID of employee: ");
        String passportId = scanner.nextLine();

        Employee emp = Employee.findById(passportId);
        if (emp == null) {
            System.out.println("Employee with passport ID " + passportId + " not found!");
            return;
        }

        while (true) {
            System.out.println("\nCurrent employee details:");
            System.out.println(emp);
            System.out.println("\nWhat would you like to update?");
            System.out.println("1. Passport ID");
            System.out.println("2. Username");
            System.out.println("3. Password");
            System.out.println("4. Name");
            System.out.println("5. Surname");
            System.out.println("6. Role");
            System.out.println("7. Active Status");
            System.out.println("8. Back to Main Menu");
            System.out.print("Select option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number 1-8.");
                continue;
            }

            switch (choice) {
                case 1: // Passport ID update
                    System.out.print("Enter new passport ID: ");
                    String newPassport = scanner.nextLine();
                    try {
                        emp.setPassportId(newPassport);
                        System.out.println("Passport ID updated successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 2: // Username update
                    System.out.print("Enter new username: ");
                    String newUsername = scanner.nextLine();
                    try {
                        emp.setUsername(newUsername);
                        System.out.println("Username updated successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 3: // Password update
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    try {
                        emp.setUsername(newPassword); // [Note] Likely a bug: should be setPassword
                        System.out.println("Password updated successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 4: // Name update
                    System.out.print("Enter new name: ");
                    String newName = scanner.nextLine();
                    try {
                        emp.setName(newName);
                        System.out.println("Name updated successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 5: // Surname update
                    System.out.print("Enter new surname: ");
                    String newSurname = scanner.nextLine();
                    try {
                        emp.setSurname(newSurname);
                        System.out.println("Surname updated successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 6: // Role update
                    System.out.print("Enter new role code (101=Manager, 102=HR Manager, 103=Cashier): ");
                    String newRole = scanner.nextLine();
                    try {
                        emp.setRole(newRole);
                        System.out.println("Role updated successfully.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;

                case 7: // Status toggle
                    if (emp.isActive()) {
                        System.out.print("Do you want to deactivate this employee? (y/n): ");
                        String choice2 = scanner.nextLine().trim().toLowerCase();
                        if (choice2.equals("y")) {
                            emp.deactivateEmployee();
                            System.out.println("Employee is now " + (emp.isActive() ? "active" : "inactive"));
                        } else if (choice2.equals("n")) {
                            System.out.println("Status remains unchanged");
                        } else {
                            System.out.println("Invalid choice. Status remains unchanged");
                        }
                    } else {
                        System.out.print("Do you want to activate this employee? (y/n): ");
                        String choice2 = scanner.nextLine().trim().toLowerCase();
                        if (choice2.equals("y")) {
                            emp.activateEmployee();
                            System.out.println("Employee is now " + (emp.isActive() ? "active" : "inactive"));
                        } else if (choice2.equals("n")) {
                            System.out.println("Status remains unchanged");
                        } else {
                            System.out.println("Invalid choice. Status remains unchanged");
                        }
                    }
                    break;

                case 8: // Exit submenu
                    System.out.println("Returning to HR menu...");
                    return;

                default:
                    System.out.println("Invalid option. Please select 1-8.");
            }
        }
    }

    /*
     * Locates employee by government-issued ID.
     * Displays full employee record or "not found" message.
     */
    private void handleFindById(Scanner scanner) {
        System.out.println("\n=== FIND EMPLOYEE BY ID ===");
        System.out.print("Enter passport ID: ");
        String passportId = scanner.nextLine();

        Employee emp = findById(passportId);
        if (emp != null) {
            System.out.println(emp);
        } else {
            System.out.println("Employee not found!");
        }
    }

    /*
     * Locates employee by system username.
     * Case-sensitive exact match required.
     */
    private void handleFindByUsername(Scanner scanner) {
        System.out.println("\n=== FIND EMPLOYEE BY USERNAME ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        Employee emp = findByUsername(username);
        if (emp != null) {
            System.out.println(emp);
        } else {
            System.out.println("Employee not found!");
        }
    }

    /*
     * Inherits equality comparison from Employee superclass.
     * Maintains consistency with parent class behavior.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /*
     * Standardized string representation inherited from Employee.
     * Ensures uniform display format across all employee types.
     */
    @Override
    public String toString() {
        return super.toString();
    }
}