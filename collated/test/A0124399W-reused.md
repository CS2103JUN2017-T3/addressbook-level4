# A0124399W-reused
###### \java\guitests\CommandBoxTest.java
``` java
public class CommandBoxTest extends TaskListGuiTest {
    private static final String COMMAND_THAT_SUCCEEDS = ListCommand.COMMAND_WORD + " /f";
    private static final String COMMAND_THAT_FAILS = "invalid command";

    private ArrayList<String> defaultStyleOfCommandBox;
    private ArrayList<String> errorStyleOfCommandBox;

    @Before
    public void setUp() {
        //Initial list floating tasks
        commandBox.runCommand(listFloating);
        //add if empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }
        defaultStyleOfCommandBox = new ArrayList<>(commandBox.getStyleClass());
        assertFalse("CommandBox default style classes should not contain error style class.",
                    defaultStyleOfCommandBox.contains(CommandBox.ERROR_STYLE_CLASS));

        // build style class for error
        errorStyleOfCommandBox = new ArrayList<>(defaultStyleOfCommandBox);
        errorStyleOfCommandBox.add(CommandBox.ERROR_STYLE_CLASS);
    }

    @Test
    public void commandBox_startingWithSuccessfulCommand() {
        assertBehaviorForSuccessfulCommand();
        assertBehaviorForFailedCommand();
    }

    @Test
    public void commandBox_startingWithFailedCommand() {
        assertBehaviorForFailedCommand();
        assertBehaviorForSuccessfulCommand();

        // verify that style is changed correctly even after multiple consecutive successful/failed commands
        assertBehaviorForSuccessfulCommand();
        assertBehaviorForFailedCommand();
        assertBehaviorForFailedCommand();
        assertBehaviorForSuccessfulCommand();
    }

    /**
     * Runs a command that fails, then verifies that
     * - the return value of runCommand(...) is false,
     * - the text remains,
     * - the command box has only one ERROR_STYLE_CLASS, with other style classes untouched.
     */
    private void assertBehaviorForFailedCommand() {
        assertFalse(commandBox.runCommand(COMMAND_THAT_FAILS));
        assertEquals(COMMAND_THAT_FAILS, commandBox.getCommandInput());
        assertEquals(errorStyleOfCommandBox, commandBox.getStyleClass());
    }

    /**
     * Runs a command that succeeds, then verifies that
     * - the return value of runCommand(...) is true,
     * - the text is cleared,
     * - the command box does not have any ERROR_STYLE_CLASS, with style classes the same as default.
     */
    private void assertBehaviorForSuccessfulCommand() {
        assertTrue(commandBox.runCommand(COMMAND_THAT_SUCCEEDS));
        assertEquals("", commandBox.getCommandInput());
        assertEquals(defaultStyleOfCommandBox, commandBox.getStyleClass());
    }

}

```
###### \java\guitests\EditCommandTest.java
``` java

public class EditCommandTest extends TaskListGuiTest {

    @Test
    public void edit_allFieldsSpecified_success() throws Exception {
        clearList();
        String detailsToEdit = PREFIX_NAME + "Bobby " + PREFIX_DEADLINE_END + "fri 10am " + PREFIX_DESCRIPTION
                + "Block 123, Bobby Street 3 " + PREFIX_TAG + "husband";
        Index taskListIndex = INDEX_FIRST_TASK;
        Task editedPerson = new TaskWithDeadlineBuilder().withName("Bobby").withDeadline("fri 10am")
                .withDescription("Block 123, Bobby Street 3").withTags("husband").build();
        assertEditSuccess(taskListIndex, detailsToEdit, editedPerson);
    }

    @Test
    public void editNotAllFieldsSpecifiedSuccess() throws Exception {
        clearList();
        commandBox.runCommand(listFloating);
        //add task if list is empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }

        String detailsToEdit = PREFIX_TAG + "sweetie " + PREFIX_TAG + "bestie";
        Index filteredListIndex = INDEX_FIRST_TASK;

        ReadOnlyTask taskToEdit = taskListPanel.getListView().getItems().get(filteredListIndex.getZeroBased());
        Task editedTask = new FloatingTaskBuilder(taskToEdit).withTags("sweetie", "bestie").build();

        assertEditSuccess(filteredListIndex, detailsToEdit, editedTask);
    }

    @Test
    public void editClearTagsSuccess() throws Exception {
        clearList();
        commandBox.runCommand(listFloating);
        //add task if list is empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }

        String detailsToEdit = PREFIX_TAG.getPrefix();
        Index filteredListIndex = INDEX_FIRST_TASK;

        ReadOnlyTask taskToEdit = taskListPanel.getListView().getItems().get(filteredListIndex.getZeroBased());
        Task editedTask = new FloatingTaskBuilder(taskToEdit).withTags().build();

        assertEditSuccess(filteredListIndex, detailsToEdit, editedTask);
    }

    @Test
    public void editMissingPersonIndexFailure() {
        commandBox.runCommand(listFloating);
        //add task if list is empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }

        commandBox.runCommand(EditCommand.COMMAND_WORD + " " + PREFIX_NAME + "Bobby");
        assertResultMessage(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
    }

    @Test
    public void editInvalidPersonIndexFailure() {
        commandBox.runCommand(listFloating);
        //add task if list is empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }
        commandBox.runCommand(EditCommand.COMMAND_WORD + " 34 " + PREFIX_NAME + "Bobby");
        assertResultMessage(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
    }

    @Test
    public void editNoFieldsSpecifiedFailure() {
        commandBox.runCommand(listFloating);
        //add task if list is empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }

        commandBox.runCommand(EditCommand.COMMAND_WORD + " 1");
        assertResultMessage(EditCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void editInvalidValuesFailure() {
        commandBox.runCommand(listFloating);
        //add task if list is empty
        if (taskListPanel.getNumberOfTasks() == 0) {
            commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        }
        commandBox.runCommand(EditCommand.COMMAND_WORD + " 1 " + PREFIX_DEADLINE_START + "abcd");
        assertResultMessage(Deadline.MESSAGE_DEADLINE_CONSTRAINTS_STRICT);

        commandBox.runCommand(EditCommand.COMMAND_WORD + " 1 " + PREFIX_TAG + "*&");
        assertResultMessage(Tag.MESSAGE_TAG_CONSTRAINTS);
    }

    @Test
    public void editDuplicateTaskFailure() {
        commandBox.runCommand(listFloating);

        commandBox.runCommand(AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "Enquire about Phone Bill "
                + PREFIX_DESCRIPTION + "Singtel, M1 " + PREFIX_TAG + "Phone");
        commandBox.runCommand(AddCommand.COMMAND_WORD + " 1 " + PREFIX_NAME + "CS1020 " + PREFIX_DESCRIPTION + "lab "
                + PREFIX_TAG + "school");
        commandBox.runCommand(AddCommand.COMMAND_WORD + " 1 " + PREFIX_NAME + "CS1020 " + PREFIX_DESCRIPTION + "lab "
                + PREFIX_TAG + "school");
        assertResultMessage(EditCommand.MESSAGE_DUPLICATE_TASK);
    }

    /**
     * Checks whether the edited task has the correct updated details.
     *
     * @param filteredTaskListIndex Index of task to edit in filtered list
     * @param detailsToEdit Details to edit the person with as input to the edit command
     * @param editedTask The expected task after editing the task's details
     */

    private void assertEditSuccess(Index filteredTaskListIndex, String detailsToEdit, Task editedTask) {
        commandBox
                .runCommand(EditCommand.COMMAND_WORD + " " + filteredTaskListIndex.getOneBased() + " " + detailsToEdit);

        // confirm the new card contains the right data
        /*
         * ReadOnlyTask taskAffectedByEditCommand =
         * taskListPanel.getListView().getItems() .get(editedTask);
         */
        ReadOnlyTask task;
        if (editedTask instanceof TaskWithDeadline) {
            task = new TaskWithDeadline(editedTask);
        } else {
            task = new Task(editedTask);
        }
        assertTrue(taskListPanel.getListView().getItems().contains(task));

        assertResultMessage(String.format(EditCommand.MESSAGE_EDIT_TASK_SUCCESS, task));
    }
    /*
     * Clears the list and adds two new tasks for testing.
     */
    private void clearList() {
        commandBox.runCommand(clear);
        commandBox.runCommand(TaskUtil.getAddCommand(td.supermarket));
        commandBox.runCommand(TaskUtil.getAddCommand(td.ida));
    }

}
```
###### \java\teamthree\twodo\logic\commands\AddCommandTest.java
``` java
public class AddCommandTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructorNullTaskFailure() throws Exception {
        thrown.expect(NullPointerException.class);
        new AddCommand(null);
    }

    @Test
    public void equalsFailsSuccessfully() {
        try {
            AddCommand command = new AddCommand(new FloatingTaskBuilder().build());
            Index targetIndex = Index.fromOneBased(1);
            DeleteCommand other = new DeleteCommand(targetIndex, false);
            assertFalse(command.equals(other));
        } catch (IllegalValueException e) {
            // should not reach here
            e.printStackTrace();
        }
    }

    @Test
    public void equalsReturnsTrueSuccessfully() {
        try {
            AddCommand command = new AddCommand(new FloatingTaskBuilder().build());
            assertTrue(command.equals(command));
            AddCommand other = new AddCommand(new FloatingTaskBuilder().build());
            assertTrue(command.equals(other));
        } catch (IllegalValueException e) {
            // should not reach here
            e.printStackTrace();
        }
    }

    @Test
    public void executeTaskAcceptedByModelAddSuccessful() throws Exception {
        ModelStubAcceptingTaskAdded modelStub = new ModelStubAcceptingTaskAdded();
        Task validTask = new TaskWithDeadlineBuilder().build();

        CommandResult commandResult = getAddCommandForTask(validTask, modelStub).execute();

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, validTask), commandResult.feedbackToUser);
        assertEquals(Arrays.asList(validTask), modelStub.tasksAdded);

        Task validFloatingTask = new FloatingTaskBuilder().build();

        commandResult = getAddCommandForTask(validFloatingTask, modelStub).execute();

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, validFloatingTask), commandResult.feedbackToUser);
        assertEquals(Arrays.asList(validTask, validFloatingTask), modelStub.tasksAdded);

    }

    @Test
    public void executeDuplicateTaskFailure() throws Exception {
        ModelStub modelStub = new ModelStubThrowingDuplicateTaskException();
        Task validTask = new TaskWithDeadlineBuilder().build();

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddCommand.MESSAGE_DUPLICATE_TASK);

        getAddCommandForTask(validTask, modelStub).execute();
    }

    @Test
    public void executeAddInvalidDeadlineFailure() throws Exception {
        ModelStub modelStub = new ModelStub();
        Task invalidTask = new TaskWithDeadlineBuilder().withEventDeadline("tomorrow 10am", "yesterday 10am").build();
        thrown.expect(CommandException.class);
        thrown.expectMessage(Messages.MESSAGE_INVALID_DEADLINE);
        getAddCommandForTask(invalidTask, modelStub).execute();
    }

    @Test
    public void executeAddTagInvalidIndexFailure() throws Exception {
        ModelStub modelStub = new ModelStubEmptyTaskList();
        String tagName = "NEWTAG";
        ArrayList<Index> indices = new ArrayList<Index>();
        indices.add(Index.fromOneBased(340));
        thrown.expect(CommandException.class);
        thrown.expectMessage(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        getAddCommandForTag(tagName, indices, modelStub).execute();
    }

    /**
     * Generates a new AddCommand with the details of the given person.
     */
    private AddCommand getAddCommandForTask(Task task, Model model) throws IllegalValueException {
        AddCommand command = new AddCommand(task);
        command.setData(model, new CommandHistory(), null);
        return command;
    }

    private AddCommand getAddCommandForTag(String tagName, ArrayList<Index> indices, Model model)
            throws IllegalValueException {
        AddCommand command = new AddCommand(tagName, indices);
        command.setData(model, new CommandHistory(), null);
        return command;
    }

    /**
     * A default model stub that have all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public void addTask(ReadOnlyTask person) throws DuplicateTaskException {
            fail("This method should not be called.");
        }

        @Override
        public void resetData(ReadOnlyTaskList newData) {
            fail("This method should not be called.");
        }

        @Override
        public ReadOnlyTaskList getTaskList() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void updateTask(ReadOnlyTask target, ReadOnlyTask editedPerson) throws DuplicateTaskException {
            fail("This method should not be called.");
        }

        @Override
        public UnmodifiableObservableList<ReadOnlyTask> getFilteredAndSortedTaskList() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void saveTaskList() {
            fail("This method should not be called.");
        }

        @Override
        public void markTask(ReadOnlyTask person) throws TaskNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void unmarkTask(ReadOnlyTask person) throws TaskNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void updateFilteredTaskListToShowPeriod(Deadline deadline, AttributeInputted attInput,
                boolean listIncomplete, Set<Tag> tagList) {
            fail("This method should not be called.");
        }

        @Override
        public void sort() {
            fail("This method should not be called.");
        }

        @Override
        public void setTaskList(ReadOnlyTaskList taskBook) {
            fail("This method should not be called.");
        }

        @Override
        public void updateFilteredTaskListToEmpty() {
            fail("This method should not be called.");
        }

        @Override
        public void updateFilteredTaskListToShowAll(Set<Tag> tagList, boolean listFloating, boolean listIncomplete) {
            fail("This method should not be called.");
        }

        @Override
        public void updateFilteredTaskListByKeywords(Set<String> keywords, boolean listIncomplete) {
            fail("This method should not be called.");
        }

        @Override
        public void changeOptions() {
            fail("This method should not be called.");
        }
    }

    /**
     * A Model stub that always throw a DuplicateTaskException when trying to
     * add a person.
     */
    private class ModelStubThrowingDuplicateTaskException extends ModelStub {
        @Override
        public void addTask(ReadOnlyTask person) throws DuplicateTaskException {
            throw new DuplicateTaskException();
        }
    }

    /**
     * A Model stub that always accept the person being added.
     */
    private class ModelStubAcceptingTaskAdded extends ModelStub {
        private final ArrayList<Task> tasksAdded = new ArrayList<>();

        @Override
        public void addTask(ReadOnlyTask task) throws DuplicateTaskException {
            if (task instanceof TaskWithDeadline) {
                tasksAdded.add(new TaskWithDeadline(task));
            } else {
                tasksAdded.add(new Task(task));
            }
        }
    }
    /**
     * A Model Stub that returns an empty taskList.
     */
    private class ModelStubEmptyTaskList extends ModelStub {
        @Override
        public UnmodifiableObservableList<ReadOnlyTask> getFilteredAndSortedTaskList() {
            return new UnmodifiableObservableList<ReadOnlyTask>(FXCollections.observableArrayList());
        }
    }

}
```
###### \java\teamthree\twodo\logic\parser\AddCommandParserTest.java
``` java
public class AddCommandParserTest {
    private static final String NAME_DESC_MOD = PREFIX_NAME + VALID_NAME_CSMOD;
    private static final String NAME_DESC_EVENT = PREFIX_NAME + VALID_NAME_EVENT;
    private static final String DEADLINE_DESC_MOD = " " + PREFIX_DEADLINE_END + VALID_END_DATE;
    private static final String DEADLINE_DESC_EVENT = " " + PREFIX_DEADLINE_START + VALID_START_DATE + " "
            + PREFIX_DEADLINE_END + VALID_END_DATE;
    private static final String DESC_MOD = " " + PREFIX_DESCRIPTION + VALID_DESCRIPTION_MOD;
    private static final String DESC_EVENT = " " + PREFIX_DESCRIPTION + VALID_DESCRIPTION_EVENT;
    private static final String TAG_DESC_WORK = " " + PREFIX_TAG + VALID_TAG_WORK;
    private static final String MESSAGE_INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            AddCommand.MESSAGE_USAGE);

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parseMissingPartsFailure() {
        // no prefix
        assertParseFailure(VALID_NAME_CSMOD, MESSAGE_INVALID_FORMAT);

        // no end date
        assertParseFailure(NAME_DESC_MOD + " " + PREFIX_DEADLINE_START + VALID_START_DATE, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parseEventSuccess() throws Exception {
        String userInput = " " + NAME_DESC_EVENT + DEADLINE_DESC_EVENT + TAG_DESC_WORK + DESC_EVENT;
        Task expected = new TaskWithDeadlineBuilder().withName(VALID_NAME_EVENT)
                .withEventDeadline(VALID_START_DATE, VALID_END_DATE).withDescription(VALID_DESCRIPTION_EVENT)
                .withTags(VALID_TAG_WORK).build();
        AddCommand expectedCommand = new AddCommand(expected);
        assertParseSuccess(userInput, expectedCommand);
    }

    @Test
    public void parseFloatSuccess() throws Exception {
        String userInput = " " + NAME_DESC_MOD + TAG_DESC_WORK + DESC_MOD;
        Task expected = new FloatingTaskBuilder().withName(VALID_NAME_CSMOD).withDescription(VALID_DESCRIPTION_MOD)
                .withTags(VALID_TAG_WORK).build();
        AddCommand expectedCommand = new AddCommand(expected);
        assertParseSuccess(userInput, expectedCommand);
    }

    @Test
    public void parseDeadlineSuccess() throws Exception {
        String userInput = " " + NAME_DESC_MOD + DEADLINE_DESC_MOD + TAG_DESC_WORK + DESC_MOD;
        Task expected = new TaskWithDeadlineBuilder().withName(VALID_NAME_CSMOD).withDeadline(VALID_END_DATE)
                .withDescription(VALID_DESCRIPTION_MOD).withTags(VALID_TAG_WORK).build();
        AddCommand expectedCommand = new AddCommand(expected);
        assertParseSuccess(userInput, expectedCommand);
    }

    /**
     * Asserts the parsing of {@code userInput} is unsuccessful and the error
     * message equals to {@code expectedMessage}
     */
    private void assertParseFailure(String userInput, String expectedMessage) {
        try {
            parser.parse(userInput);
            fail("An exception should have been thrown.");
        } catch (ParseException pe) {
            assertEquals(expectedMessage, pe.getMessage());
        }

    }

    /**
     * Asserts the parsing of {@code userInput} is successful and the result
     * matches {@code expectedCommand}
     */
    private void assertParseSuccess(String userInput, AddCommand expectedCommand) throws Exception {
        Command command = parser.parse(userInput);
        assertTrue(expectedCommand.equals(command));
    }
}
```
