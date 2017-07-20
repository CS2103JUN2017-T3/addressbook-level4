package teamthree.twodo.logic;

import static java.util.Objects.requireNonNull;
import static teamthree.twodo.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static teamthree.twodo.logic.parser.CliSyntax.PREFIX_CATEGORY;

import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import teamthree.twodo.commons.core.EventsCenter;
import teamthree.twodo.commons.core.LogsCenter;
import teamthree.twodo.commons.events.logic.NewUserInputEvent;
import teamthree.twodo.commons.events.model.DeleteCategoryEvent;
import teamthree.twodo.logic.commands.DeleteCommand;
import teamthree.twodo.logic.commands.HelpCommand;
import teamthree.twodo.logic.commands.RedoCommand;
import teamthree.twodo.logic.commands.UndoCommand;
import teamthree.twodo.logic.parser.Parser;
import teamthree.twodo.logic.parser.exceptions.ParseException;
import teamthree.twodo.model.ReadOnlyTaskBook;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.storage.StorageManager;

//@@author A0162253M
// Stores the history of commands executed.
public class CommandHistory {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);

    private Stack<String> userInputHistory;
    private Stack<ReadOnlyTask> beforeEditHistory;
    private Stack<ReadOnlyTask> afterEditHistory;
    private Stack<ReadOnlyTask> deleteHistory;
    private Stack<ReadOnlyTask> addHistory;
    private Stack<ReadOnlyTask> markHistory;
    private Stack<ReadOnlyTask> unmarkHistory;
    private Stack<ReadOnlyTaskBook> clearHistory;
    private ArrayList<String> fullUserInputHistory;
    private Stack<ArrayList<Task>> taskWithTagsHistory;
    private Stack<String> tagNameHistory;

    public CommandHistory() {
        beforeEditHistory = new Stack<ReadOnlyTask>();
        afterEditHistory = new Stack<ReadOnlyTask>();
        addHistory = new Stack<ReadOnlyTask>();
        deleteHistory = new Stack<ReadOnlyTask>();
        markHistory = new Stack<ReadOnlyTask>();
        unmarkHistory = new Stack<ReadOnlyTask>();
        clearHistory = new Stack<ReadOnlyTaskBook>();
        userInputHistory = new Stack<String>();
        fullUserInputHistory = new ArrayList<>();
        taskWithTagsHistory = new Stack<ArrayList<Task>>();
        tagNameHistory = new Stack<String>();
    }

    /**
     * Appends {@code userInput} to the list of user input entered.
     */
    public void add(String userInput) {
        requireNonNull(userInput);
        fullUserInputHistory.add(userInput);
        EventsCenter.getInstance().post(new NewUserInputEvent(this.getHistory()));
    }

    /**
     * Returns a defensive copy of {@code userInputHistory}.
     */
    public ArrayList<String> getHistory() {
        return new ArrayList<String>(fullUserInputHistory);
    }

    /**
     * Appends {@code userInput} to the list of user input entered.
     *
     * @throws ParseException
     */
    public void addToUserInputHistory(String userInput) throws ParseException {
        requireNonNull(userInput);
        String[] arguments = seperateInput(userInput);
        boolean isUndoRedo = arguments[0].equals(RedoCommand.COMMAND_WORD)
                || arguments[0].equals(RedoCommand.COMMAND_WORD_UNIXSTYLE)
                || arguments[0].equals(UndoCommand.COMMAND_WORD)
                || arguments[0].equals(UndoCommand.COMMAND_WORD_UNIXSTYLE);

        if (!isUndoRedo) {
            boolean isDeleteCommandWord = arguments[0].equals(DeleteCommand.COMMAND_WORD)
                    || arguments[0].equals(DeleteCommand.COMMAND_WORD_QUICK)
                    || arguments[0].equals(DeleteCommand.COMMAND_WORD_SHORT)
                    || arguments[0].equals(DeleteCommand.COMMAND_WORD_FAST);

            if (isDeleteCommandWord) {
                String[] splitArgs = arguments[1].trim().split(" ");
                boolean isDeleteTag = splitArgs.length > 1
                    && splitArgs[0].trim().equals(PREFIX_CATEGORY.toString());

                if (isDeleteTag) {
                    getUserInputHistory().push(PREFIX_CATEGORY.toString());
                } else {
                    getUserInputHistory().push(arguments[0]);
                }
            } else {
                getUserInputHistory().push(arguments[0]);
            }
        }
    }

    /**
     * Appends {@code task} to the list of task before edits entered.
     */
    public void addToBeforeEditHistory(ReadOnlyTask task) {
        requireNonNull(task);
        beforeEditHistory.push(task);
    }

    /**
     * Appends {@code task} to the list of task after edits entered.
     */
    public void addToAfterEditHistory(ReadOnlyTask task) {
        requireNonNull(task);
        afterEditHistory.push(task);
    }

    /**
     * Appends {@code task} to the list of added tasks entered.
     */
    public void addToAddHistory(ReadOnlyTask task) {
        requireNonNull(task);
        addHistory.push(task);
    }

    /**
     * Appends {@code task} to the list of deleted tasks entered.
     */
    public void addToDeleteHistory(ReadOnlyTask task) {
        requireNonNull(task);
        deleteHistory.push(task);
    }

    /**
     * Appends {@code task} to the list of marked tasks entered.
     */
    public void addToMarkHistory(ReadOnlyTask task) {
        requireNonNull(task);
        markHistory.push(task);
    }

    /**
     * Appends {@code task} to the list of unmarked tasks entered.
     */
    public void addToUnmarkHistory(ReadOnlyTask task) {
        requireNonNull(task);
        unmarkHistory.push(task);
    }

    /**
     * Appends {@code filePath} to the list of cleared filePath entered.
     */
    public void addToClearHistory(ReadOnlyTaskBook taskBook) {
        requireNonNull(taskBook);
        clearHistory.push(taskBook);
    }

    public void addToTaskWithTagsHistory(ArrayList<Task> taskList) {
        requireNonNull(taskList);
        taskWithTagsHistory.push(taskList);
    }

    public void addToTagNameHistory(String tagName) {
        requireNonNull(tagName);
        tagNameHistory.push(tagName);
    }

    public Stack<String> getUserInputHistory() {
        requireNonNull(userInputHistory);
        return userInputHistory;
    }

    public Stack<ReadOnlyTask> getBeforeEditHistory() {
        requireNonNull(beforeEditHistory);
        return beforeEditHistory;
    }

    public Stack<ReadOnlyTask> getAfterEditHistory() {
        requireNonNull(afterEditHistory);
        return afterEditHistory;
    }

    public Stack<ReadOnlyTask> getAddHistory() {
        requireNonNull(addHistory);
        return addHistory;
    }

    public Stack<ReadOnlyTask> getDeleteHistory() {
        requireNonNull(deleteHistory);
        return deleteHistory;
    }

    public Stack<ReadOnlyTask> getMarkHistory() {
        requireNonNull(markHistory);
        return markHistory;
    };

    public Stack<ReadOnlyTask> getUnmarkHistory() {
        requireNonNull(unmarkHistory);
        return unmarkHistory;
    }

    public Stack<ReadOnlyTaskBook> getClearHistory() {
        requireNonNull(clearHistory);
        return clearHistory;
    }

    public Stack<ArrayList<Task>> getTaskWithTagsHistory() {
        requireNonNull(taskWithTagsHistory);
        return taskWithTagsHistory;
    }

    public Stack<String> getTagNameHistory() {
        requireNonNull(tagNameHistory);
        return tagNameHistory;
    }

    public void handleDeleteCategoryEvent(DeleteCategoryEvent e) {
        logger.info(LogsCenter.getEventHandlingLogMessage(e, "Stored Task In Deleted Category"));
        addToTaskWithTagsHistory(e.tasksUnderCategory);
        addToTagNameHistory(e.tagName);
    }

    private String[] seperateInput(String userInput) throws ParseException {
        final Matcher matcher = Parser.BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String args = matcher.group("arguments");
        String[] result = {commandWord, args};
        return result;
    }
}
