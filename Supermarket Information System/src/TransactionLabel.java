import java.time.LocalDate;
import java.util.Objects;
import java.text.DecimalFormat;

/**
 * Represents a financial transaction record in the supermarket system.
 * Tracks transaction date and amount with formatting capabilities.
 * Used for customer purchase history and reporting.
 */
public class TransactionLabel {
    private LocalDate date;  // Date of the transaction
    private double amount;   // Amount of the transaction
    private static final DecimalFormat amountFormat = new DecimalFormat("0.00");

    /* ------------------------------
       CONSTRUCTORS
       ------------------------------ */

    /**
     * Creates a transaction record with current date and specified amount.
     * Used for new transactions being recorded in real-time.
     */
    public TransactionLabel(double amount) {
        this.date = LocalDate.now();  // Current date for the transaction
        this.amount = amount;
    }

    /**
     * Creates a transaction record with specific date and amount.
     * Used for historical data or testing purposes.
     */
    public TransactionLabel(LocalDate date, double amount) {
        this.date = date;
        this.amount = amount;
    }

    /* ------------------------------
       GETTER METHODS
       ------------------------------ */

    /**
     * Returns the transaction date.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the raw transaction amount.
     */
    public double getAmount() {
        return amount;
    }

    /* ------------------------------
       FORMATTING METHODS
       ------------------------------ */

    /**
     * Returns the amount formatted as a currency string (2 decimal places).
     */
    public String getFormattedAmount() {
        return amountFormat.format(amount);
    }

    /* ------------------------------
       OBJECT OVERRIDES
       ------------------------------ */

    /**
     * Returns formatted string representation of the transaction.
     * Format: "Transaction [Date: YYYY-MM-DD, Amount: XX.XX€]"
     */
    @Override
    public String toString() {
        return "Transaction [Date: " + date + ", Amount: " + getFormattedAmount() + "€]";
    }

    /**
     * Compares transactions for equality based on:
     * - Exact date match
     * - Exact amount match (to the cent)
     */
    @Override
    public boolean equals(Object o) {
        // Check if comparing with same instance
        if (this == o) return true;

        // Check for null or different class
        if (o == null || getClass() != o.getClass()) return false;

        // Cast and compare significant fields
        TransactionLabel that = (TransactionLabel) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(date, that.date);
    }
}