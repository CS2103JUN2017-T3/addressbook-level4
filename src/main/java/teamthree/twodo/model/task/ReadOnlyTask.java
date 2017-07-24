package teamthree.twodo.model.task;

import java.util.Optional;
import java.util.Set;

import teamthree.twodo.model.tag.Tag;
//@@author A0124399W-reused
/**
 * A read-only immutable interface for a Task in the TaskList.
 * Implementations should guarantee: details are present and not null, field values are validated.
 */
public interface ReadOnlyTask {

    Name getName();
    Description getDescription();
    Set<Tag> getTags();
    Boolean isCompleted();
    Optional<Deadline> getDeadline();
    /**
     * Returns true if both have the same state. (interfaces cannot override .equals)
     */
    default boolean isSameStateAs(ReadOnlyTask other) {
        return other == this // short circuit if same object
                || (other != null // this is first to avoid NPE below
                && other.getName().equals(this.getName())// state checks here onwards
                && other.getDeadline().equals(this.getDeadline())
                && other.getDescription().equals(this.getDescription()))
                && other.isCompleted() == this.isCompleted();
    }

    /**
     * Formats the task as text, showing all contact details.
     */
    default String getAsText() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getName() + "\n")
                .append("Description: ")
                .append(getDescription())
                .append("Completed: ")
                .append(isCompleted() + "\n")
                .append("Tags: ");
        getTags().forEach(builder::append);
        builder.append("\n");
        return builder.toString();
    }

    public boolean getCompleted();

}
