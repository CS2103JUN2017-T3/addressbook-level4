package teamthree.twodo.automark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import teamthree.twodo.commons.core.ComponentManager;
import teamthree.twodo.commons.events.model.DeadlineTimeReachedEvent;
import teamthree.twodo.commons.events.model.TaskListChangedEvent;
import teamthree.twodo.model.Model;
import teamthree.twodo.model.task.Deadline;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.TaskWithDeadline;
import teamthree.twodo.model.task.exceptions.TaskNotFoundException;

//@@author A0139267W-reused
// A copy of AutoMarkManager to bypass static declarations for testing purposes
public class AutoMarkManagerStud extends ComponentManager {
    /**
     * Only runs the auto completion functionality if the users sets it Is false
     * by default
     */
    private static boolean setToRun = false;

    // List of tasks yet to be completed
    private final List<ReadOnlyTask> uncompletedList = new ArrayList<ReadOnlyTask>();
    // Keeps track of tasks that have been completed
    private final HashSet<ReadOnlyTask> completed = new HashSet<ReadOnlyTask>();

    private final Model model;

    // In charge of scheduling and executing auto-completion markings
    private final Timer masterClock = new Timer();

    // Deadline of the most imminent task
    private Date nextDeadline;

    public AutoMarkManagerStud(Model model) {
        this.model = model;
        syncWithMasterTaskList(model.getTaskList().getTaskList());
    }

    public static boolean getSetToRun() {
        return setToRun;
    }

    // Enables or disables the auto-completion functionality
    public static void setToRun(boolean setting) {
        setToRun = setting;
    }

    /**
     * Synchronizes internal uncompleted list with the main task list
     *
     * @param masterList Full list of tasks from the task list
     */
    private synchronized void syncWithMasterTaskList(List<ReadOnlyTask> masterList) {
        if (masterList == null || masterList.isEmpty()) {
            return;
        }
        // Clears list first to avoid duplicates
        uncompletedList.clear();
        // Adds tasks which are not in the completed set
        masterList.forEach((t) -> {
            if (t instanceof TaskWithDeadline && !completed.contains(t)) {
                uncompletedList.add(t);
            }
        });
        sortTasksByDeadline();
        updateNextDeadline();
        // If this feature is disabled, do not execute the auto completion markings
        if (!setToRun) {
            return;
        }
        startTimerTask();
    }

    public void startTimerTask() {
        if (nextDeadline == null) {
            return;
        }
        masterClock.schedule(new NextAutomark(), nextDeadline);

    }

    private Date getCompletionTime(ReadOnlyTask task) {
        return task.getDeadline().get().getEndDate();
    }

    // =====================HELPER CLASS==========================

    private class NextAutomark extends TimerTask {

        /**
         * The following command will be run upon reaching the scheduled timing.
         * It will raise a DeadlineTimeReachedEvent with all the tasks that have
         * reached the deadline.
         *
         * After that it will update internal information.
         */
        @Override
        public void run() {
            List<ReadOnlyTask> tasksToAutoMark = new ArrayList<ReadOnlyTask>();
            Date currentDate = new Date();
            uncompletedList.forEach((t) -> {
                if (getCompletionTime(t).before(currentDate) || getCompletionTime(t).equals(nextDeadline)) {
                    tasksToAutoMark.add(t);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                model.markTask(t);
                            } catch (TaskNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            if (tasksToAutoMark.size() > 0) {
                raise(new DeadlineTimeReachedEvent(tasksToAutoMark));
            }

            updateInternalData(tasksToAutoMark);

            startTimerTask();
        }

    }

    // =========================HELPER METHODS=================================

    /**
     * Transfers the most recently auto marked tasks from the uncompleted list
     * to the completed set. Updates the nextDeadline with the deadline of the
     * next activity on the uncompleted list. Called only after a
     * DeadlineTimeReachedEvent.
     *
     * @param completedTasks The tasks which were sent with the DeadlineTimeReachedEvent
     */

    private synchronized void updateInternalData(List<ReadOnlyTask> completedTasks) {
        uncompletedList.removeAll(completedTasks);
        completed.addAll(completedTasks);
        updateNextDeadline();
    }

    // Sorts task list by their deadline
    private void sortTasksByDeadline() {
        uncompletedList.sort(new Comparator<ReadOnlyTask>() {

            @Override
            public int compare(ReadOnlyTask t, ReadOnlyTask u) {
                return getCompletionTime(t).compareTo(getCompletionTime(u));
            }

        });
    }

    // Updates nextDeadline to the next one on the uncompletedList.
    private void updateNextDeadline() {
        if (!uncompletedList.isEmpty()) {
            nextDeadline = removeInvalidDates() ? null : getCompletionTime(uncompletedList.get(0));
        } else {
            nextDeadline = null;
        }
    }

    /**
     * Transfers all invalid dates (i.e Default Dates) from uncompleted list to
     * completed set. This avoids an invalid date exception from being thrown at
     * startTimerTask. Returns whether uncompleted list is empty after
     * operation.
     *
     * @return true if completion list is empty after removing all invalid
     *         dates.
     */
    private boolean removeInvalidDates() {
        while (!getCompletionTime(uncompletedList.get(0)).after(Deadline.DEFAULT_DATE)) {
            completed.add(uncompletedList.remove(0));
        }
        return uncompletedList.isEmpty();
    }

    // =======================EVENT HANDLERS===========================

    // Synchronizes the uncompleted list with the master list when there is a change
    @Subscribe
    public void handleTaskListChangedEvent(TaskListChangedEvent event) {
        syncWithMasterTaskList(model.getTaskList().getTaskList());
    }

}
