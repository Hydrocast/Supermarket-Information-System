import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main class for managing the supermarket system lifecycle.
 * Handles system initialization, data loading/saving, and shutdown.
 * Coordinates all high-level system operations and ensures data persistence.
 */
public class ControlSystem {
    // Tracks whether the system is actively running (false triggers shutdown)
    private boolean systemRunning;
    // Shared scanner for user input across all system components
    private final Scanner scanner;
    // List of all data files required for system operation
    private final ArrayList<String> DATA_FILES = new ArrayList<String>();
    // Central file handling component for all I/O operations
    private final FileHandler fileHandler;

    /**
     * Constructor initializes system components and loads necessary data files.
     * Performs critical setup tasks:
     * 1. Creates shared input scanner
     * 2. Registers all data file paths
     * 3. Initializes FileHandler with registered files
     * 4. Loads persisted data from disk
     */
    public ControlSystem() {
        this.scanner = new Scanner(System.in);
        this.systemRunning = true;

        // System-critical data files (order matters for dependency chain)
        DATA_FILES.add("employees.txt");          // Employee credentials/roles
        DATA_FILES.add("customers.txt");          // Customer profiles
        DATA_FILES.add("customer_transactions.txt"); // Transaction history
        DATA_FILES.add("products.txt");           // Product catalog
        DATA_FILES.add("inventory.txt");          // Live stock levels

        this.fileHandler = new FileHandler(DATA_FILES);

        // Load all data into memory at startup
        loadAllData();

        System.out.println("System initialized successfully!");
        System.out.println("=== SUPERMARKET MANAGEMENT SYSTEM ===");
    }

    /**
     * Main entry point of the program.
     * Creates the ControlSystem instance and starts the application.
     * Args are unused in this implementation but preserved for future extensibility.
     */
    public static void main(String[] args) {
        ControlSystem system = new ControlSystem();
        system.startSystem();
    }

    /**
     * Starts the main system loop by displaying the login menu.
     * Delegates further flow control to the MainMenu class.
     */
    public void startSystem() {
        MainMenu mainMenu = new MainMenu(this);
        mainMenu.showLoginMenu();
    }

    /**
     * Loads data from all registered files via FileHandler.
     * Throws IllegalStateException if FileHandler fails to initialize,
     * as system cannot function without data.
     */
    private void loadAllData() {
        // Guard clause for critical dependency
        if (fileHandler == null) {
            throw new IllegalStateException("FileHandler not initialized - cannot load data");
        }

        System.out.println("Loading system data...");
        fileHandler.loadAllFiles();  // Load all files
    }

    /**
     * Orchestrates graceful system shutdown:
     * 1. Generates final activity report
     * 2. Persists all data to disk
     * 3. Terminates JVM with exit code 0
     */
    public void shutdownSystem() {
        GeneralReport.generateFinalReport();  // Static report generation
        System.out.println("\nInitiating system shutdown...");
        saveAllData();  // Ensure no data loss
        System.out.println("System shutdown complete. Goodbye!");
        System.exit(0);  // Clean termination
    }

    /**
     * Saves all system data to disk via FileHandler.
     */
    private void saveAllData() {
        System.out.println("Saving system data...");
        fileHandler.saveAllFiles();  // Atomic save operation
    }

    /**
     * External shutdown request handler.
     * Sets shutdown flag and triggers shutdown sequence.
     */
    public void requestShutdown() {
        this.systemRunning = false;  // Signal to stop accepting new operations
        shutdownSystem();            // Begin shutdown protocol
    }
}