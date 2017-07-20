package teamthree.twodo.logic.commands;

import teamthree.twodo.commons.core.Messages;
import teamthree.twodo.commons.core.UnmodifiableObservableList;
import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.logic.commands.exceptions.CommandException;
import teamthree.twodo.model.category.CategoryManager;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.exceptions.TaskNotFoundException;

// @@author A0162253M - reused
// Deletes a task identified using its last displayed index from the TaskBook.
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String COMMAND_WORD_QUICK = "-";
    public static final String COMMAND_WORD_FAST = "d";
    public static final String COMMAND_WORD_SHORT = "del";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the task identified by the index number used in the last task listing.\n"
            + "Parameters: INDEX (must be a positive integer)\n" + "Example: " + COMMAND_WORD + " 1 ";

    public static final String MESSAGE_DELETE_TASK_SUCCESS = "Deleted Task: %1$s";
    public static final String MESSAGE_DELETE_TAG_SUCCESS = "Deleted Tag: %1$s";

    public final Index targetIndex;
    public final boolean deleteCategoryFlag;

    public DeleteCommand(Index targetIndex, boolean categoryOp) {
        this.targetIndex = targetIndex;
        this.deleteCategoryFlag = categoryOp;
    }

    @Override
    public CommandResult execute() throws CommandException {
        if (deleteCategoryFlag) {
            if (targetIndex.getZeroBased() >= catMan.getCategoryList().size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_TAG_DISPLAYED_INDEX);
            } else if (targetIndex.getZeroBased() <= CategoryManager.INDEX_LAST_DEFAULT.getZeroBased()) {
                throw new CommandException(Messages.MESSAGE_DEFAULT_TAG_INDEX);
            }
            try {
                Tag toDel = catMan.deleteCategory(targetIndex);
                return new CommandResult(String.format(MESSAGE_DELETE_TAG_SUCCESS, toDel.tagName));
            } catch (IllegalValueException e) {
                //impossible to get this exception
                e.printStackTrace();
            }
            return new CommandResult("Delete category failed.");
        }

        UnmodifiableObservableList<ReadOnlyTask> lastShownList = model.getFilteredAndSortedTaskList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        ReadOnlyTask taskToDelete = lastShownList.get(targetIndex.getZeroBased());

        try {
            model.deleteTask(taskToDelete);
            history.addToDeleteHistory(taskToDelete);
        } catch (TaskNotFoundException pnfe) {
            assert false : "The target task cannot be missing";
        }
        return new CommandResult(String.format(MESSAGE_DELETE_TASK_SUCCESS, taskToDelete));
    }

}
