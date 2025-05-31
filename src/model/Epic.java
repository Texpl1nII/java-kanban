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

    public void addSubtask(Subtask subtask) {
        if (!subtaskIds.contains(subtask.getId())) {
            subtaskIds.add(subtask.getId());
        }
    }

    public void removeSubtask(Subtask subtask) {
        subtaskIds.remove((Integer) subtask.getId());
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public List<Subtask> getSubtasks() {
        List<Subtask> subtasks = new ArrayList<>();
        if (taskManager != null) {
            for (Integer id : subtaskIds) {
                Subtask subtask = taskManager.getSubtask(id);
                if (subtask != null) {
                    subtasks.add(subtask);
                }
            }
        }
        return subtasks;
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
        return "Epic{id=" + getId() +
                ", title='" + getTitle() +
                "', description='" + getDescription() +
                "', status=" + getStatus() +
                ", subtasks=" + subtaskIds.size() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : "null") +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                "}";
    }
}