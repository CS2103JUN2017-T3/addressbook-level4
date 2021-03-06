package teamthree.twodo.logic.commands;

import static java.util.Objects.requireNonNull;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_CATEGORY;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DEADLINE_END;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DEADLINE_START;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_NAME;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_NOTIFICATION_PERIOD;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;

import teamthree.twodo.commons.core.EventsCenter;
import teamthree.twodo.commons.core.Messages;
import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.commons.events.model.AddOrEditCommandExecutedEvent;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.logic.commands.exceptions.CommandException;
import teamthree.twodo.model.TaskList;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.model.task.TaskWithDeadline;
import teamthree.twodo.model.task.exceptions.DuplicateTaskException;

//@@author A0124399W
// Adds a task to the TaskList.
public class AddCommand extends Command {

    //Command word can be any one of the three
    public static final String COMMAND_WORD = "add";
    public static final String COMMAND_WORD_QUICK = "+";
    public static final String COMMAND_WORD_FAST = "a";
    public static final String COMMAND_WORD_TAG = "tag";
    public static final String MESSAGE_USAGE_TAG = COMMAND_WORD + " " + PREFIX_CATEGORY
            + ": Adds a tag to multiple tasks.\n" + "Parameters: TAGNAME INDICES OF TASKS TO ADD TAG TO\n" + "Example: "
            + COMMAND_WORD + " " + PREFIX_CATEGORY + "NUS 1,2,4,7";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a Task that you need 2Do.\n"
            + "Please refer to the UserGuide for the correct input format of the parameters.\n"
            + "To find out how to add tags to multiple tasks type '"
            + HelpCommand.COMMAND_WORD + PREFIX_CATEGORY + "'.\n"
            + "Parameters: " + PREFIX_NAME + "{TASK} " + PREFIX_DEADLINE_START + "[START DATE] {TIME} "
            + PREFIX_DEADLINE_END + "[END DATE] {TIME} " + PREFIX_DESCRIPTION + "[DESCRIPTION] "
            + PREFIX_TAG + "[TAG1, TAG2,...] " + PREFIX_NOTIFICATION_PERIOD + "[ALARM]\n"
            + "Example for floating: " + COMMAND_WORD + " " + PREFIX_NAME + "Buy some lotion "
            + PREFIX_DESCRIPTION + "Must be water-based "
            + PREFIX_TAG + "personal\n" + "Example for deadline: "
            + PREFIX_NAME + "V0.5 " + PREFIX_DEADLINE_END + "next monday 8pm\n" + "Example for events: "
            + PREFIX_NAME + "Attend ComicCon " + PREFIX_DEADLINE_START + " friday 10am " + PREFIX_DEADLINE_END
            + "friday 10pm " + PREFIX_TAG + "Otaku";

    public static final String MESSAGE_SUCCESS = "New task added: %1$s\n";
    public static final String MESSAGE_SUCCESS_TAG = "New tag added: %1$s\n";
    public static final String MESSAGE_DUPLICATE_TASK = "This task already exists in your 2Do list";

    private final Task toAdd;
    private final String tagName;
    private final ArrayList<Index> taskIndices;

    // Creates an AddCommand to add the specified {@code ReadOnlyTask}
    public AddCommand(ReadOnlyTask task) {
        if (task instanceof TaskWithDeadline) {
            toAdd = new TaskWithDeadline(task);
        } else {
            toAdd = new Task(task);
        }
        tagName = null;
        taskIndices = null;
    }

    // Creates an AddCommand to add the specified category with the specified tasks
    public AddCommand(String tagName, ArrayList<Index> indices) {
        toAdd = null;
        this.tagName = tagName;
        this.taskIndices = indices;
    }

    @Override
    public CommandResult execute() throws CommandException {
        requireNonNull(model);
        if (toAdd == null && !taskIndices.isEmpty()) {
            ArrayList<Task> tasksForCategory = getTasksFromIndices();
            try {
                history.addToAddTagHistory(new TaskList(model.getTaskList()));
                Tag added = catMan.addCategory(tagName, tasksForCategory);
                history.addToTagAddedHistory(added);
                return new CommandResult(String.format(MESSAGE_SUCCESS_TAG, added.tagName));
            } catch (IllegalValueException e) {
                throw new CommandException(Tag.MESSAGE_TAG_CONSTRAINTS);
            }
        }
        try {
            if (isInvalidDeadline(toAdd)) {
                throw new CommandException(Messages.MESSAGE_INVALID_DEADLINE);
            }
            model.addTask(toAdd);
            history.addToAddHistory(toAdd);
            EventsCenter.getInstance().post(new AddOrEditCommandExecutedEvent(toAdd));
            return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
        } catch (DuplicateTaskException e) {
            throw new CommandException(MESSAGE_DUPLICATE_TASK);
        }

    }

    private boolean isInvalidDeadline(Task toAdd) {
        if (toAdd instanceof TaskWithDeadline) {
            return toAdd.getDeadline().get().getEndDate().before(toAdd.getDeadline().get().getStartDate());
        }
        return false;
    }

    //Returns list of tasks from given list of indices
    private ArrayList<Task> getTasksFromIndices() throws CommandException {
        List<ReadOnlyTask> lastShownList = model.getFilteredAndSortedTaskList();
        ArrayList<Task> tasksForCategory = new ArrayList<>();
        ReadOnlyTask task;
        for (Index idx : taskIndices) {
            if (idx.getOneBased() > lastShownList.size() || idx.getOneBased() < 1) {
                throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
            }
            if ((task = lastShownList.get(idx.getZeroBased())) instanceof TaskWithDeadline) {
                tasksForCategory.add(new TaskWithDeadline(task));
            } else {
                tasksForCategory.add(new Task(task));
            }
        }
        return tasksForCategory;
    }

    @Override
    public String toString() {
        return this.toAdd.getAsText();
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand)) {
            return false;
        }

        // state check
        AddCommand temp = (AddCommand) other;
        return this.toString().equals(temp.toString());
    }

}
