package manager;

import manager.TaskManager;
import manager.HistoryManager;
import manager.Managers;
import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;
import exception.NotFoundException;

import java.util.HashMap;
import java.util.*;
import java.time.LocalDateTime;

public class InMemoryTaskManager implements TaskManager {
    protected int idCounter = 0;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) ->
            t1.getStartTime().compareTo(t2.getStartTime())
    );

    private int generateId() {
        return ++idCounter;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
        prioritizedTasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Task with ID " + id + " not found");
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Task createTask(Task task) {
        hasOverlaps(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public void updateTask(Task task) {
        Task existingTask = tasks.get(task.getId());
        if (existingTask == null) {
            throw new NotFoundException("Task with ID " + task.getId() + " not found");
        }
        if (!Objects.equals(task.getStartTime(), existingTask.getStartTime()) ||
                !Objects.equals(task.getDuration(), existingTask.getDuration())) {
            hasOverlaps(task);
        }
        prioritizedTasks.remove(existingTask);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task == null) {
            throw new NotFoundException("Task with ID " + id + " not found");
        }
        prioritizedTasks.remove(task);
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearEpics() {
        epics.clear();
        subtasks.clear();
        prioritizedTasks.removeIf(t -> t instanceof Subtask);
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic with ID " + id + " not found");
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NotFoundException("Epic with ID " + epic.getId() + " not found");
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            throw new NotFoundException("Epic with ID " + id + " not found");
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.remove(subtaskId);
            if (subtask != null) {
                prioritizedTasks.remove(subtask);
                historyManager.remove(subtaskId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear();
        prioritizedTasks.removeIf(t -> t instanceof Subtask);
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Subtask with ID " + id + " not found");
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        hasOverlaps(subtask);
        if (subtask.getId() != 0 && subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Subtask cannot be its own epic");
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new NotFoundException("Epic with ID " + subtask.getEpicId() + " not found");
        }
        if (epic.getId() == subtask.getId()) {
            throw new IllegalArgumentException("Epic cannot be its own subtask");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        hasOverlaps(subtask);
        if (subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Subtask cannot be its own epic");
        }
        Subtask existingSubtask = subtasks.get(subtask.getId());
        if (existingSubtask == null) {
            throw new NotFoundException("Subtask with ID " + subtask.getId() + " not found");
        }
        prioritizedTasks.remove(existingSubtask);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            throw new NotFoundException("Subtask with ID " + id + " not found");
        }
        prioritizedTasks.remove(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtaskIds().remove((Integer) subtask.getId());
            updateEpicStatus(epic);
        }
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Epic with ID " + epicId + " not found");
        }
        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public boolean isOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getDuration() == null ||
                task2.getStartTime() == null || task2.getDuration() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = start1.plus(task1.getDuration());
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = start2.plus(task2.getDuration());
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public void hasOverlaps(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return;
        }
        boolean overlaps = prioritizedTasks.stream()
                .filter(task -> task.getId() != newTask.getId())
                .filter(task -> task.getStartTime() != null && task.getDuration() != null)
                .anyMatch(task -> isOverlapping(task, newTask));
        if (overlaps) {
            throw new IllegalStateException("Task overlaps with other tasks");
        }
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                Status status = subtask.getStatus();
                if (status != Status.DONE) allDone = false;
                if (status != Status.NEW) allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}