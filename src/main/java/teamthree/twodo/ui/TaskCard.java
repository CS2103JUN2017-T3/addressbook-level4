package teamthree.twodo.ui;

import java.util.Date;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.TaskWithDeadline;

public class TaskCard extends UiPart<Region> {

    private static final String FXML = "TaskCard.fxml";

    /**
     * Description: Certain keywords such as "location" and "resources" are
     * reserved keywords in JavaFX. As a consequence, UI elements' variable
     * names cannot be set to such keywords or an exception will be thrown by
     * JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">
     *      The issue on TaskList level 4</a>
     */

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label deadline;
    @FXML
    private Label description;
    @FXML
    private FlowPane tags;

    public TaskCard(ReadOnlyTask task, int displayedIndex) {
        super(FXML);
        name.setText(task.getName().fullName);
        id.setText(displayedIndex + ". ");
        // Also add a complete/incomplete tag to indicate its completion status
        String completionStatus = "Incomplete";
        if (task.isCompleted()) {
            completionStatus = "Complete";
        }
        //@@author A0124399W-reused
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
