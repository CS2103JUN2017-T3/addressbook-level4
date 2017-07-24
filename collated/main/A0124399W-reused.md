# A0124399W-reused
###### \java\teamthree\twodo\model\task\Name.java
``` java
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
```
###### \java\teamthree\twodo\model\task\ReadOnlyTask.java
``` java
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
```
###### \java\teamthree\twodo\storage\XmlAdaptedTask.java
``` java
/**
 * JAXB-friendly version of the Task.
 */
public class XmlAdaptedTask {

    @XmlElement(required = true)
    private String name;
    @XmlElement(required = false)
    private Deadline deadline;

    @XmlElement(required = false)
    private String isComplete;

    @XmlElement(required = false)
    private String description;

    @XmlElement
    private List<XmlAdaptedTag> tagged = new ArrayList<>();

    /**
     * Constructs an XmlAdaptedTask. This is the no-arg constructor that is
     * required by JAXB.
     */
    public XmlAdaptedTask() {
    }

    /**
     * Converts a given Task into this class for JAXB use.
     *
     * @param source Future changes to this will not affect the created XmlAdaptedTask
     */
    public XmlAdaptedTask(ReadOnlyTask source) {
        name = source.getName().fullName;
        if (source.getDeadline().isPresent()) {
            deadline = source.getDeadline().get();
        }
        isComplete = source.isCompleted().toString();
        description = source.getDescription().value;
        tagged = new ArrayList<>();
        for (Tag tag : source.getTags()) {
            tagged.add(new XmlAdaptedTag(tag));
        }
    }

    /**
     * Converts this jaxb-friendly adapted task object into the model's Task
     * object.
     *
     * @throws IllegalValueException
     *             if there were any data constraints violated in the adapted
     *             person
     */
    public Task toModelType() throws IllegalValueException {
        final List<Tag> taskTags = new ArrayList<>();
        for (XmlAdaptedTag tag : tagged) {
            taskTags.add(tag.toModelType());
        }
        final Name name = new Name(this.name);
        //final Email email = new Email(this.email);
        Description desc = new Description("No description");
        if (description != null) {
            desc = new Description(this.description);
        }

        final Set<Tag> tags = new HashSet<>(taskTags);
        Task task;
        if (deadline != null) {
            final Deadline deadline = new Deadline(this.deadline);
            return new TaskWithDeadline(name, deadline, desc, tags, false);
        }
        task = new Task(name, desc, tags, false);
        markIfComplete(task);
        return task;
    }

    private void markIfComplete(Task task) {
        if (isComplete != null && isComplete.equals(Boolean.TRUE.toString())) {
            task.markCompleted();
        }
    }
}
```
###### \java\teamthree\twodo\ui\TaskCard.java
``` java
        if (task instanceof TaskWithDeadline) {
            deadline.setText(task.getDeadline().get().toString());
            if (isOverdue(task) && !task.isCompleted()) {
                markAsOverdue(displayedIndex);
                completionStatus = "Overdue";
            }
        } else {
            deadline.setText("No deadline");
        }
        if (task.getDescription().toString().isEmpty()) {
            description.setText("No description");
        } else {
            description.setText(task.getDescription().toString());
        }
        initTags(task);
        tags.getChildren().add(new Label(completionStatus));
    }

    private boolean isOverdue(ReadOnlyTask task) {
        return task.getDeadline().get().getEndDate().before(new Date());
    }

    public void markAsOverdue(int displayedIndex) {
        if (isEven(displayedIndex)) {
            this.cardPane.setStyle("-fx-background-color: #8B0000;");
        } else {
            this.cardPane.setStyle("-fx-background-color: #700000;");
        }
    }

    private boolean isEven(int displayedIndex) {
        return displayedIndex % 2 == 0;
    }

    private void initTags(ReadOnlyTask task) {
        task.getTags().forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }
}
```
