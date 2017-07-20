package teamthree.twodo.logic.commands;

import static org.junit.Assert.assertTrue;
import static teamthree.twodo.testutil.TypicalTask.INDEX_FIRST_TASK;
import static teamthree.twodo.testutil.TypicalTask.INDEX_SECOND_TASK;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import teamthree.twodo.commons.core.Messages;
import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.logic.CommandHistory;
import teamthree.twodo.model.Model;
import teamthree.twodo.model.ModelManager;
import teamthree.twodo.model.TaskBook;
import teamthree.twodo.model.UserPrefs;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.testutil.TypicalTask;

//@@author A0139267W
public class MarkCommandTest {
    private Model model = new ModelManager(new TypicalTask().getTypicalTaskBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() throws Exception {

        ReadOnlyTask taskToMark = model.getFilteredAndSortedTaskList().get(INDEX_FIRST_TASK.getZeroBased());
        MarkCommand markCommand = prepareCommand(INDEX_FIRST_TASK);

        Model expectedModel = new ModelManager(new TaskBook(model.getTaskBook()), new UserPrefs());
        expectedModel.markTask(taskToMark);

        String expectedMessage = getExpectedMessage(expectedModel, taskToMark);

        CommandTestUtil.assertCommandSuccess(markCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() throws Exception {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredAndSortedTaskList().size() + 1);
        MarkCommand markCommand = prepareCommand(outOfBoundIndex);

        CommandTestUtil.assertCommandFailure(markCommand, model, Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() throws Exception {
        showFirstTaskOnly(model);
        ReadOnlyTask taskToMark = model.getFilteredAndSortedTaskList().get(INDEX_FIRST_TASK.getZeroBased());
        MarkCommand markCommand = prepareCommand(INDEX_FIRST_TASK);

        Model expectedModel = new ModelManager(model.getTaskBook(), new UserPrefs());
        showFirstTaskOnly(expectedModel);
        expectedModel.markTask(taskToMark);

        String expectedMessage = getExpectedMessage(expectedModel, taskToMark);
        // Properly resets the task list to its prior state
        showFirstTaskOnly(expectedModel);

        CommandTestUtil.assertCommandSuccess(markCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() throws Exception {
        showFirstTaskOnly(model);

        Index outOfBoundIndex = INDEX_SECOND_TASK;
        // Ensures that outOfBoundIndex is still in bounds of task list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getTaskBook().getTaskList().size());

        MarkCommand markCommand = prepareCommand(outOfBoundIndex);

        CommandTestUtil.assertCommandFailure(markCommand, model, Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
    }

    // Returns a {@code MarkCommand} with the parameter {@code index}
    private MarkCommand prepareCommand(Index index) {
        MarkCommand markCommand = new MarkCommand(index);
        markCommand.setData(model, new CommandHistory(), null);
        return markCommand;
    }

    // Obtains the appropriate expected message obtained after a successful MarkCommand
    private String getExpectedMessage(Model expectedModel, ReadOnlyTask taskToMark) {
        // Finds the updated task
        final String[] splitName = taskToMark.getName().fullName.split("\\s+");
        expectedModel.updateFilteredTaskList(new HashSet<>(Arrays.asList(splitName)), false);
        assertTrue(expectedModel.getFilteredAndSortedTaskList().size() == 1);

        ReadOnlyTask markedTask = expectedModel.getFilteredAndSortedTaskList().get(INDEX_FIRST_TASK.getZeroBased());

        /**
         *  Resets task list to its initial state
         *  Initial state is assumed to be the task list that lists all incomplete tasks
         */
        expectedModel.updateFilteredListToShowAllIncomplete(null, false);

        return String.format(MarkCommand.MESSAGE_MARK_TASK_SUCCESS, markedTask);
    }

    /**
     * Updates {@code model}'s filtered list to show only the indexed first task from the task book
     * Does not show any task if the indexed first task has been marked as completed
     */
    private void showFirstTaskOnly(Model model) {
        ReadOnlyTask task = model.getTaskBook().getTaskList().get(0);
        final String[] splitName = task.getName().fullName.split("\\s+");
        model.updateFilteredTaskList(new HashSet<>(Arrays.asList(splitName)), true);
    }

}