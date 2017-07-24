package teamthree.twodo.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javafx.collections.FXCollections;
import teamthree.twodo.commons.core.Messages;
import teamthree.twodo.commons.core.UnmodifiableObservableList;
import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.logic.CommandHistory;
import teamthree.twodo.logic.commands.ListCommand.AttributeInputted;
import teamthree.twodo.logic.commands.exceptions.CommandException;
import teamthree.twodo.model.Model;
import teamthree.twodo.model.ReadOnlyTaskList;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.Deadline;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.model.task.TaskWithDeadline;
import teamthree.twodo.model.task.exceptions.DuplicateTaskException;
import teamthree.twodo.model.task.exceptions.TaskNotFoundException;
import teamthree.twodo.testutil.FloatingTaskBuilder;
import teamthree.twodo.testutil.TaskWithDeadlineBuilder;

//@@author A0124399W-reused
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
