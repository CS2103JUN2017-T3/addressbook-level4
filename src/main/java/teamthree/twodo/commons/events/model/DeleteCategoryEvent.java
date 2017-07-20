package teamthree.twodo.commons.events.model;

import java.util.ArrayList;

import teamthree.twodo.commons.events.BaseEvent;
import teamthree.twodo.model.task.Task;

public class DeleteCategoryEvent extends BaseEvent {

    public final ArrayList<Task> tasksUnderCategory;
    public final String tagName;

    public DeleteCategoryEvent(ArrayList<Task> tasksUnderCategory, String tagName) {
        this.tasksUnderCategory = tasksUnderCategory;
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
