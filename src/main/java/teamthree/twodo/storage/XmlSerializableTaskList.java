package teamthree.twodo.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import teamthree.twodo.commons.core.UnmodifiableObservableList;
import teamthree.twodo.commons.exceptions.IllegalValueException;
import teamthree.twodo.model.ReadOnlyTaskList;
import teamthree.twodo.model.tag.Tag;
import teamthree.twodo.model.task.ReadOnlyTask;
import teamthree.twodo.model.task.Task;

/**
 * An Immutable TaskList that is serializable to XML format
 */
@XmlRootElement(name = "twodo")
public class XmlSerializableTaskList implements ReadOnlyTaskList {

    @XmlElement
    private List<XmlAdaptedTask> task;
    @XmlElement
    private List<XmlAdaptedTag> tags;

    /**
     * Creates an empty XmlSerializableTaskList. This empty constructor is
     * required for marshalling
     */
    public XmlSerializableTaskList() {
        task = new ArrayList<>();
        tags = new ArrayList<>();
    }

    /**
     * Converts the current read only task list in the application to a XmlSerializableTaskList for storage
     */
    public XmlSerializableTaskList(ReadOnlyTaskList src) {
        this();
        task.addAll(src.getTaskList().stream().map(XmlAdaptedTask::new).collect(Collectors.toList()));
        tags.addAll(src.getTagList().stream().map(XmlAdaptedTag::new).collect(Collectors.toList()));
    }
    /**
     * Intended to be used for storing notified tasks data
     * @param taskSet Notified tasks set
     * @return Xml list for storage
     */
    public static List<XmlAdaptedTask> getXmlSerializableTaskList(HashSet<ReadOnlyTask> taskSet) {
        return new ArrayList<XmlAdaptedTask>(taskSet.stream().map(XmlAdaptedTask::new).collect(Collectors.toList()));
    }

    @Override
    public ObservableList<ReadOnlyTask> getTaskList() {
        final ObservableList<Task> tasks = this.task.stream().map(p -> {
            try {
                return p.toModelType();
            } catch (IllegalValueException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
        return new UnmodifiableObservableList<>(tasks);
    }

    @Override
    public ObservableList<Tag> getTagList() {
        final ObservableList<Tag> tags = this.tags.stream().map(t -> {
            try {
                return t.toModelType();
            } catch (IllegalValueException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
        return new UnmodifiableObservableList<>(tags);
    }

}
