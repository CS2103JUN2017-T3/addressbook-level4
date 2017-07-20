package teamthree.twodo.model.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import teamthree.twodo.commons.core.ComponentManager;
import teamthree.twodo.commons.core.EventsCenter;
import teamthree.twodo.commons.core.UnmodifiableObservableList;
import teamthree.twodo.commons.core.index.Index;
import teamthree.twodo.commons.events.model.DeleteCategoryEvent;
import teamthree.twodo.commons.events.model.TaskBookChangedEvent;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.model.Model;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.model.task.TaskWithDeadline;

// @@author A0124399W
/**
 * Manager class for maintaining the different categories in the task manager.
 */
public class CategoryManager extends ComponentManager {
    public static final Index INDEX_LAST_DEFAULT = Index.fromOneBased(5);
    private final Model model;

    /**
     * ==============DEFAULT CATEGORIES=======================
     */
    private final Category allTasks = new Category("All", 0);
    private final Category completeTasks = new Category("Completed", 0);
    private final Category incompleteTasks = new Category("Incomplete", 0);
    private final Category floatingTasks = new Category("Floating Tasks", 0);
    private final Category tasksWithDeadline = new Category("Tasks with Deadline", 0);
    /**
     * =======================================================
     */
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private List<Category> defaultCategories = new ArrayList<Category>();
    private OtherCategoryManager otherCategories;

    public CategoryManager(Model model) {
        this.model = model;
        initDefaultCategories();
        otherCategories = new OtherCategoryManager();
        resetCategoryList();
    }

    //Initializes just the default categories
    private void initDefaultCategories() {
        updateDefaultCategories();
        addToDefaultCategoryList(allTasks, completeTasks, incompleteTasks, floatingTasks, tasksWithDeadline);
    }

    public ObservableList<Category> getCategoryList() {
        return new UnmodifiableObservableList<Category>(categoryList);
    }

    public synchronized Tag deleteCategory(Index targetIndex) throws IllegalValueException {
        Tag toDel = otherCategories.deleteCategory(targetIndex);
        resetCategoryList();
        return toDel;
    }

    public synchronized Tag addCategory(String newTagName, List<Task> tasks) throws IllegalValueException {
        Tag toAdd = otherCategories.addCategory(newTagName, tasks);
        resetCategoryList();
        return toAdd;
    }

    //Updates all categories
    private void refreshAllMainList() {
        updateDefaultCategories();
        otherCategories.syncWithMasterTagList();
        resetCategoryList();
    }

    /**
     * Resets the main category list with the default and other categories.
     */
    public synchronized void resetCategoryList() {
        categoryList.clear();
        categoryList.addAll(defaultCategories);
        categoryList.addAll(otherCategories.getCategories());
    }

    // Updates all the default categories
    private void updateDefaultCategories() {
        setAllTasks();
        setCompleteTasks();
        setFloatingTasks();
    }

    //Adds multiple categories to the default list
    private void addToDefaultCategoryList(Category... categories) {
        for (Category category : categories) {
            defaultCategories.add(category);
        }
    }

    //Sets the number of all tasks category
    private void setAllTasks() {
        allTasks.setNumberOfConstituents(model.getTaskBook().getTaskList().size());
    }

    // Sets the number of complete and incomplete tasks categories
    private void setCompleteTasks() {
        int numComplete = 0;
        int numIncomplete = 0;
        for (ReadOnlyTask t : model.getTaskBook().getTaskList()) {
            if (t.isCompleted()) {
                numComplete++;
            } else {
                numIncomplete++;
            }
        }
        completeTasks.setNumberOfConstituents(numComplete);
        incompleteTasks.setNumberOfConstituents(numIncomplete);
    }

    /**
     * Sets both floating and tasks with deadlines
     */
    private void setFloatingTasks() {
        int numFloating = 0;
        int numWithDeadline = 0;
        for (ReadOnlyTask t : model.getTaskBook().getTaskList()) {
            if (!(t instanceof TaskWithDeadline)) {
                numFloating++;
            } else {
                numWithDeadline++;
            }
        }
        floatingTasks.setNumberOfConstituents(numFloating);
        tasksWithDeadline.setNumberOfConstituents(numWithDeadline);
    }

    /** ========================EVENT HANDLERS========================= */
    /**
     * Updates the category list when there is a change in the taskbook.
     */
    @Subscribe
    public void handleTaskBookChangedEvent(TaskBookChangedEvent event) {
        refreshAllMainList();
    }

    /** =======================INNER CLASSES=========================== */
    /**
     * Keeps track of the user-defined tags and the tasks which contain them.
     * Provides functionality to edit tags at a general level.
     */
    private class OtherCategoryManager {
        //Main mapping between tags and tasks which contain them
        private final HashMap<Tag, ArrayList<Task>> categoryMap = new HashMap<Tag, ArrayList<Task>>();

        OtherCategoryManager() {
            syncWithMasterTagList();
        }

        /**
         * Synchronizes inner categoryMap with the master tag list in the Model.
         */
        private synchronized void syncWithMasterTagList() {
            categoryMap.clear();
            ObservableList<Tag> masterList = model.getTaskBook().getTagList();
            masterList.forEach((tag) -> {
                ArrayList<Task> tasksWithTag = new ArrayList<Task>();
                model.getTaskBook().getTaskList().forEach((task) -> {
                    if (task.getTags().contains(tag)) {
                        tasksWithTag
                                .add(task instanceof TaskWithDeadline ? new TaskWithDeadline(task) : new Task(task));
                    }
                });
                if (!tasksWithTag.isEmpty()) {
                    categoryMap.put(tag, tasksWithTag);
                }
            });
        }

        /**
         * Returns a sorted list of all categories
         */
        private ArrayList<Category> getCategories() {
            ArrayList<Category> otherCategoryList = new ArrayList<Category>();
            categoryMap.forEach((key, value) -> {
                otherCategoryList.add(new Category(key.tagName, value.size()));
            });
            otherCategoryList.sort((cat, next) -> cat.getName().compareTo(next.getName()));
            return otherCategoryList;
        }

        /**
         * Deletes a user-defined category. Returns the Tag that was deleted.
         *
         * @param targetIndex
         * @throws IllegalValueException
         */
        private Tag deleteCategory(Index targetIndex) throws IllegalValueException {
            //Get category to delete from last shown list
            Tag toDel = new Tag(categoryList.get(targetIndex.getZeroBased()).getName());
            String tagName = categoryList.get(targetIndex.getZeroBased()).getName();
            ArrayList<Task> tasksUnderCategory = categoryMap.get(toDel);
            EventsCenter.getInstance().post(new DeleteCategoryEvent(tasksUnderCategory, tagName));
            tasksUnderCategory.forEach((task) -> {
                Task editedTask = task;
                HashSet<Tag> tags = new HashSet<Tag>(task.getTags());
                tags.remove(toDel);
                editedTask.setTags(tags);
                try {
                    model.updateTask(task, editedTask);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            syncWithMasterTagList();
            return toDel;
        }

        private Tag addCategory(String newTagName, List<Task> tasks) throws IllegalValueException {
            Tag toAdd = new Tag(newTagName);
            ArrayList<Task> tasksUnderCategory = new ArrayList<>();
            tasksUnderCategory.addAll(tasks);
            tasksUnderCategory.forEach((task) -> {
                Task editedTask = task;
                HashSet<Tag> tags = new HashSet<Tag>(task.getTags());
                tags.add(toAdd);
                editedTask.setTags(tags);
                try {
                    model.updateTask(task, editedTask);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            syncWithMasterTagList();
            return toAdd;
        }
    }
}
