package model;

import model.Task;
import model.Subtask;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Epic extends Task {
    private final List<Subtask> subtasks;

    public Epic(String title, String description, Duration duration, LocalDateTime startTime) {
        super(title, description, duration, startTime);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        if (subtask != null && !subtasks.contains(subtask)) {
            subtasks.add(subtask);
        }
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public List<Integer> getSubtaskIds() {
        List<Integer> ids = new ArrayList<>();
        for (Subtask subtask : subtasks) {
            ids.add(subtask.getId());
        }
        return ids;
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    @Override
    public Duration getDuration() {
        return subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus, Duration::plus);
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return subtasks.stream()
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
                ", subtasks=" + subtasks.size() +
                ", duration=" + (getDuration() != null ? getDuration().toMinutes() : "null") +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                "}";
    }
}