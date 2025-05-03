package model;

public class Subtask extends model.Task {
    private final int epicId;

    public Subtask(String title, String description, model.Epic epic) {
        super(title, description);
        this.epicId = epic.getId();
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + id + ", title='" + title + "', description='" + description + "', status=" + status + ", epicId=" + epicId + "}";
    }
}