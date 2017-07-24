# A0162253M-reused
###### \java\teamthree\twodo\logic\commands\DeleteCommand.java
``` java
// Deletes a task identified using its last displayed index from the TaskList.
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";
    public static final String COMMAND_WORD_QUICK = "-";
    public static final String COMMAND_WORD_FAST = "d";
    public static final String COMMAND_WORD_SHORT = "del";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the task or tag identified by the index number used in the last task listing.\n"
            + "The index must be a positive integer.\n"
            + "Index of tags can be obtained from the tag list below."
            + "Deleting tags will remove the tag from relevant tasks.\n"
            + "Parameters: [tag] {INDEX}\n"
            + "Example for Deleting Task: " + COMMAND_WORD + " 1\n"
            + "Example for Deleting Tag: " + COMMAND_WORD + " tag 8\n";

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
                history.addToDelTagHistory(new TaskList(model.getTaskList()));
                Tag toDel = catMan.deleteCategory(targetIndex);
                history.addToTagDeletedHistory(toDel);
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
```
###### \java\teamthree\twodo\logic\LogicManager.java
``` java
public class LogicManager extends ComponentManager implements Logic {
    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private Model model;
    private CategoryManager catMan;
    private final CommandHistory history;
    private final UndoCommandHistory undoHistory;
    private final Parser parser;

    public LogicManager(Model model, CategoryManager catMan) {
        this.model = model;
        this.history = new CommandHistory();
        this.parser = new Parser();
        this.undoHistory = new UndoCommandHistory();
        this.catMan = catMan;
    }
    public LogicManager(Model model) {
        this.model = model;
        this.history = new CommandHistory();
        this.parser = new Parser();
        this.undoHistory = new UndoCommandHistory();
        this.catMan = null;
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        try {
            logger.info("----------------[USER COMMAND][" + commandText + "]");
            Command command = parser.parseCommand(commandText);
            command.setData(model, history, undoHistory, catMan);
            return command.execute();
        } finally {
            history.addToUserInputHistory(commandText);
            history.add(commandText);
        }
    }

    @Override
    public ObservableList<ReadOnlyTask> getFilteredTaskList() {
        return model.getFilteredAndSortedTaskList();
    }

    @Override
    public CommandHistory getCommandHistory() {
        return history;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }
}
```
