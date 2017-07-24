# A0139267W-reused
###### \java\teamthree\twodo\automark\AutoMarkManagerStud.java
``` java
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
```
###### \java\teamthree\twodo\commons\core\ConfigStud.java
``` java
//A copy of Config to bypass static declarations for testing purposes
public class ConfigStud {

    // Default notification period of 1 day. Can be changed by user.
    private static Long notificationPeriod = (long) (1000 * 60 * 60 * 24);
    private static String notificationPeriodToString = "1 day";
    private static String defaultConfigFile = "config.json";

    // To change default notification period
    private static final long DAY_TO_MILLIS = 1000 * 60 * 60 * 24;
    private static final long WEEK_TO_MILLIS = DAY_TO_MILLIS * 7;

    // Config values customizable through config file
    private String appTitle = "2Do";
    private Level logLevel = Level.INFO;
    private String userPrefsFilePath = "preferences.json";
    private String taskBookFilePath = "data/2Do.xml";

    public static Long getDefaultNotificationPeriod() {
        return notificationPeriod;
    }

    public static void setDefaultNotificationPeriod(long newNotificationPeriod) {
        notificationPeriod = newNotificationPeriod;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public String getUserPrefsFilePath() {
        return userPrefsFilePath;
    }

    public void setUserPrefsFilePath(String userPrefsFilePath) {
        this.userPrefsFilePath = userPrefsFilePath;
    }

    public void setTaskBookFilePath(String taskBookFilePath) {
        this.taskBookFilePath = taskBookFilePath;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Config)) { //this handles null as well.
            return false;
        }

        ConfigStud o = (ConfigStud) other;

        return Objects.equals(appTitle, o.appTitle) && Objects.equals(logLevel, o.logLevel)
                && Objects.equals(userPrefsFilePath, o.userPrefsFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appTitle, logLevel, userPrefsFilePath);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("App title : " + appTitle);
        sb.append("\nCurrent log level : " + logLevel);
        sb.append("\nPreference file Location : " + userPrefsFilePath);
        sb.append("\nTask Book file Location " + taskBookFilePath);
        return sb.toString();
    }

    public static String getDefaultConfigFile() {
        return defaultConfigFile;
    }

    public String getTaskBookFilePath() {
        return taskBookFilePath;
    }

    public static void changeDefaultNotificationPeriod(String newNotificationPeriod) {
        notificationPeriodToString = newNotificationPeriod;
        Matcher integerParser = Pattern.compile("\\d*").matcher(newNotificationPeriod);
        assert (integerParser.find());
        integerParser.find();
        if (integerParser.group().trim().equals("")) {
            integerParser = Pattern.compile("\\d*").matcher(newNotificationPeriod);
            assert (integerParser.find());
        }
        int period = Integer.parseInt(integerParser.group().trim());
        long newDefault = 0;
        if (newNotificationPeriod.toLowerCase().contains("day")) {
            newDefault = DAY_TO_MILLIS * period;
        } else if (newNotificationPeriod.toLowerCase().contains("week")) {
            newDefault = WEEK_TO_MILLIS * period;
        }
        notificationPeriod = newDefault;
    }

    public static String defaultNotificationPeriodToString() {
        return notificationPeriodToString;
    }
}
```
