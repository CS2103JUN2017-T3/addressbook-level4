package teamthree.twodo.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.Deadline;
import teamthree.twodo.model.task.Description;
import teamthree.twodo.model.task.Name;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;
import teamthree.twodo.model.task.TaskWithDeadline;

//@@author A0124399W-reused
/**
 * JAXB-friendly version of the Task.
 */
public class XmlAdaptedTask {

    @XmlElement(required = true)
    private String name;
    @XmlElement(required = false)
    private Deadline deadline;

    @XmlElement(required = false)
    private String isComplete;

    @XmlElement(required = false)
    private String description;

    @XmlElement
    private List<XmlAdaptedTag> tagged = new ArrayList<>();

    /**
     * Constructs an XmlAdaptedTask. This is the no-arg constructor that is
     * required by JAXB.
     */
    public XmlAdaptedTask() {
    }

    /**
     * Converts a given Task into this class for JAXB use.
     *
     * @param source Future changes to this will not affect the created XmlAdaptedTask
     */
    public XmlAdaptedTask(ReadOnlyTask source) {
        name = source.getName().fullName;
        if (source.getDeadline().isPresent()) {
            deadline = source.getDeadline().get();
        }
        isComplete = source.isCompleted().toString();
        description = source.getDescription().value;
        tagged = new ArrayList<>();
        for (Tag tag : source.getTags()) {
            tagged.add(new XmlAdaptedTag(tag));
        }
    }

    /**
     * Converts this jaxb-friendly adapted task object into the model's Task
     * object.
     *
     * @throws IllegalValueException
     *             if there were any data constraints violated in the adapted
     *             person
     */
    public Task toModelType() throws IllegalValueException {
        final List<Tag> taskTags = new ArrayList<>();
        for (XmlAdaptedTag tag : tagged) {
            taskTags.add(tag.toModelType());
        }
        final Name name = new Name(this.name);
        //final Email email = new Email(this.email);
        Description desc = new Description("No description");
        if (description != null) {
            desc = new Description(this.description);
        }

        final Set<Tag> tags = new HashSet<>(taskTags);
        Task task;
        if (deadline != null) {
            final Deadline deadline = new Deadline(this.deadline);
            return new TaskWithDeadline(name, deadline, desc, tags, false);
        }
        task = new Task(name, desc, tags, false);
        markIfComplete(task);
        return task;
    }

    private void markIfComplete(Task task) {
        if (isComplete != null && isComplete.equals(Boolean.TRUE.toString())) {
            task.markCompleted();
        }
    }
}
