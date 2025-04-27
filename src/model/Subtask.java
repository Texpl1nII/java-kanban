public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, Epic epic) {
        super(title, description);
        this.epicId = epic.getId();
        epic.addSubtask(this);
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + id + ", title='" + title + "', description='" + description + "', status=" + status + ", epicId=" + epicId + "}";
    }
}