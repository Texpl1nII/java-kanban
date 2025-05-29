package manager;

import model.Task;
import model.Subtask;
import model.Epic;
import manager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class IntervalBasedTaskManager extends InMemoryTaskManager {
    private final Map<LocalDateTime, Boolean> timeSlots = new HashMap<>();
    private static final Duration SLOT_DURATION = Duration.ofMinutes(15);
    private static final LocalDateTime START_DATE = LocalDateTime.of(2025, 1, 1, 0, 0);
    private static final LocalDateTime END_DATE = START_DATE.plusYears(1);

    private boolean isTimeSlotFree(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return true;
        }
        LocalDateTime current = task.getStartTime();
        LocalDateTime end = task.getStartTime().plus(task.getDuration());
        while (current.isBefore(end)) {
            LocalDateTime slot = alignToSlot(current);
            if (timeSlots.getOrDefault(slot, true)) {
                return false;
            }
            current = current.plus(SLOT_DURATION);
        }
        return true;
    }

    private void reserveTimeSlots(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return;
        }
        LocalDateTime current = task.getStartTime();
        LocalDateTime end = task.getStartTime().plus(task.getDuration());
        while (current.isBefore(end)) {
            LocalDateTime slot = alignToSlot(current);
            timeSlots.put(slot, true);
            current = current.plus(SLOT_DURATION);
        }
    }

    private void freeTimeSlots(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return;
        }
        LocalDateTime current = task.getStartTime();
        LocalDateTime end = task.getStartTime().plus(task.getDuration());
        while (current.isBefore(end)) {
            LocalDateTime slot = alignToSlot(current);
            timeSlots.put(slot, false);
            current = current.plus(SLOT_DURATION);
        }
    }

    private LocalDateTime alignToSlot(LocalDateTime time) {
        long minutes = Duration.between(START_DATE, time).toMinutes();
        long slotIndex = minutes / 15;
        return START_DATE.plusMinutes(slotIndex * 15);
    }

    @Override
    public Task createTask(Task task) {
        if (!isTimeSlotFree(task)) {
            throw new IllegalStateException("Временные интервалы заняты");
        }
        Task createdTask = super.createTask(task);
        reserveTimeSlots(createdTask);
        return createdTask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!isTimeSlotFree(subtask)) {
            throw new IllegalStateException("Временные интервалы заняты");
        }
        Subtask createdSubtask = super.createSubtask(subtask);
        reserveTimeSlots(createdSubtask);
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        Task oldTask = getTask(task.getId());
        if (oldTask != null) {
            freeTimeSlots(oldTask);
        }
        if (!isTimeSlotFree(task)) {
            if (oldTask != null) {
                reserveTimeSlots(oldTask);
            }
            throw new IllegalStateException("Временные интервалы заняты");
        }
        super.updateTask(task);
        reserveTimeSlots(task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = getSubtask(subtask.getId());
        if (oldSubtask != null) {
            freeTimeSlots(oldSubtask);
        }
        if (!isTimeSlotFree(subtask)) {
            if (oldSubtask != null) {
                reserveTimeSlots(oldSubtask);
            }
            throw new IllegalStateException("Временные интервалы заняты");
        }
        super.updateSubtask(subtask);
        reserveTimeSlots(subtask);
    }
}