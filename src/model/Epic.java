package model;

import model.Task;
import model.Subtask;
import model.TaskType;
import manager.InMemoryTaskManager;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private transient InMemoryTaskManager taskManager;

    public Epic(String title, String description, Duration duration, LocalDateTime startTime) {
        super(title, description, duration, startTime);
        this.subtaskIds = new ArrayList<>();
    }

    public void setTaskManager(InMemoryTaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    }

    @Override
    public Duration getDuration() {
        if (taskManager == null) return Duration.ZERO;
        return subtaskIds.stream()
                .map(taskManager::getSubtask)
                .filter(Objects::nonNull)
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus, Duration::plus);
    }

    @Override
    public LocalDateTime getStartTime() {
        if (taskManager == null) return null;
        return subtaskIds.stream()
                .map(taskManager::getSubtask)
                .filter(Objects::nonNull)
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        if (taskManager == null) return null;
        return subtaskIds.stream()
                .map(taskManager::getSubtask)
                .filter(Objects::nonNull)
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Epic{id=" + getId() + ", title='" + title + "', description='" + description +
                "', status=" + status + ", subtasks=" + subtaskIds.size() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : "null") +
                ", startTime=" + getStartTime() + "}";
    }
}