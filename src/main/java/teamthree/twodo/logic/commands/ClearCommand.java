package teamthree.twodo.logic.commands;

import static java.util.Objects.requireNonNull;

import teamthree.twodo.model.TaskList;

/**
 * Clears the TaskList.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Task List has been cleared!";


    @Override
    public CommandResult execute() {
        requireNonNull(model);
        history.addToClearHistory(new TaskList(model.getTaskList()));
        model.resetData(new TaskList());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
