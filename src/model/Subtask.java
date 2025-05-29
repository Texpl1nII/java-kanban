package model;

import model.Task;
import model.Epic;
import model.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, Duration duration, LocalDateTime startTime, Epic epic) {
        super(title, description, duration, startTime);
        this.epicId = epic.getId();
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + id + ", title='" + title + "', description='" + description +
                "', status=" + status + ", duration=" + (duration != null ? duration.toMinutes() : "null") +
                ", startTime=" + startTime + ", epicId=" + epicId + "}";
    }
}