import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    }

    @Override
    public String toString() {
        return "Epic{id=" + id + ", title='" + title + "', description='" + description + "', status=" + status + ", subtasks=" + subtaskIds.size() + "}";
    }
}