import java.util.Objects;

/*
 * Custom runtime exception for invalid person data scenarios.
 * Used when name/surname validation fails during Person operations.
 */
class InvalidPersonDataException extends RuntimeException {
    public InvalidPersonDataException(String message) {
        super(message);
    }
}

/*
 * Foundation class representing human entities in the system.
 * Encapsulates core identity attributes with strict validation.
 * Serves as base class for all role-specific person types.
 */
public class Person {
    // Personal identity attributes
    private String name;     // Legal first name (1-50 chars)
    private String surname;  // Family name (1-50 chars)

    /*
     * Primary constructor with immediate validation.
     * Guarantees Person objects are always in valid state.
     */
    public Person(String name, String surname) {
        setName(name);      // Validated assignment
        setSurname(surname); // Validated assignment
    }

    /*
     * Protected constructor for inheritance and serialization.
     * Allows delayed initialization when loading persisted objects.
     */
    protected Person() {}

    /*
     * Central validation logic for name/surname attributes.
     * Enforces:
     * 1. Non-null requirement
     * 2. Non-empty content (after trimming)
     * 3. Maximum length constraint (50 chars)
     */
    private void validateNameOrSurname(String input, String fieldName) {
        if (input == null) {
            throw new InvalidPersonDataException(fieldName + " cannot be null.");
        }

        String trimmedInput = input.trim();
        if (trimmedInput.isEmpty()) {
            throw new InvalidPersonDataException(fieldName + " cannot be empty.");
        }

        if (trimmedInput.length() > 50) {
            throw new InvalidPersonDataException(fieldName + " exceeds 50 characters.");
        }
    }

    /*
     * Mutator for name with integrated validation.
     * Trims whitespace and enforces business rules.
     */
    public void setName(String name) {
        validateNameOrSurname(name, "Name");
        this.name = name.trim(); // Store normalized value
    }

    /*
     * Mutator for surname with identical validation as name.
     * Maintains consistency in attribute handling.
     */
    public void setSurname(String surname) {
        validateNameOrSurname(surname, "Surname");
        this.surname = surname.trim(); // Store normalized value
    }

    /*
     * Accessor for person's first name.
     * Returns exact stored value without transformation.
     */
    public String getName() {
        return this.name;
    }

    /*
     * Accessor for person's family name.
     * Returns exact stored value without transformation.
     */
    public String getSurname() {
        return this.surname;
    }

    /*
     * Standardized string representation for logging.
     * Follows common Java toString() conventions.
     */
    @Override
    public String toString() {
        return String.format("Person[name='%s', surname='%s']", name, surname);
    }

    /*
     * Value-based equality comparison.
     * Considers two Persons equal if both name and surname match.
     * Follows standard Java equals() contract.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Identity check
        if (o == null || getClass() != o.getClass()) return false; // Type check

        Person person = (Person) o;
        return Objects.equals(name, person.name) &&
                Objects.equals(surname, person.surname);
    }

    /*
     * Specialized exception for person data validation failures.
     * Nested within Person class for logical grouping.
     */
    public static class InvalidPersonDataException extends RuntimeException {
        public InvalidPersonDataException(String message) {
            super(message);
        }
    }
}