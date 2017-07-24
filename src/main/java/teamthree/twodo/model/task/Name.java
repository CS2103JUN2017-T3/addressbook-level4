package teamthree.twodo.model.task;

import static java.util.Objects.requireNonNull;

import teamthree.twodo.commons.exceptions.IllegalValueException;
//@@author A0124399W-reused
/**
 * Represents a Task's name in the TaskList.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_NAME_CONSTRAINTS =
            "Task name should not be blank and it should not contain forward slashes.";

    /*
     * Name can be any character other than newline and forward slash
     */
    public static final String NAME_VALIDATION_REGEX = "[^/]+";

    public final String fullName;

    /**
     * Validates given Task Name.
     *
     * @throws IllegalValueException if given name string is invalid.
     */
    public Name(String name) throws IllegalValueException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!isValidName(trimmedName)) {
            throw new IllegalValueException(MESSAGE_NAME_CONSTRAINTS);
        }
        this.fullName = trimmedName;
    }

    /**
     * Returns true if a given string is a valid Task name.
     */
    public static boolean isValidName(String test) {
        return test.matches(NAME_VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Name // instanceof handles nulls
                && this.fullName.equals(((Name) other).fullName)); // state check
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
