package manager;

import manager.InMemoryTaskManager;
import model.Subtask;
import manager.ManagerSaveException;
import model.Task;
import model.Epic;
import model.Status;
import model.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
        if (hasOverlaps(task)) {
            throw new IllegalStateException("Задача пересекается с другими задачами");
        }
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        createdEpic.setTaskManager(this);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (hasOverlaps(subtask)) {
            throw new IllegalStateException("Задача пересекается с другими задачами");
        }
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        if (hasOverlaps(task)) {
            throw new IllegalStateException("Задача пересекается с другими задачами");
        }
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (hasOverlaps(subtask)) {
            throw new IllegalStateException("Задача пересекается с другими задачами");
        }
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,duration,startTime,epicId\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
            writer.newLine();
            for (Task task : historyManager.getHistory()) {
                writer.write(task.getId() + ",");
            }
            writer.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file: " + file.getAbsolutePath(), e);
        }
    }

    private String toString(Task task) {
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
        String type = task.getType().name();
        String epicId = task.getType() == TaskType.SUBTASK ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(), type, task.getTitle(), task.getStatus(), task.getDescription(),
                durationStr, startTimeStr, epicId);
    }

    private Task fromString(String value) {
        if (value.trim().isEmpty()) {
            return null;
        }
        String[] parts = value.split(",");
        if (parts.length < 8) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String title = parts[2];
            Status status = Status.valueOf(parts[3]);
            String description = parts[4];
            Duration duration = parts[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[5]));
            LocalDateTime startTime = parts[6].isEmpty() ? null : LocalDateTime.parse(parts[6], DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            switch (type) {
                case TASK:
                    Task task = new Task(title, description, duration, startTime);
                    task.setId(id);
                    task.setStatus(status);
                    return task;
                case EPIC:
                    Epic epic = new Epic(title, description, duration, startTime);
                    epic.setId(id);
                    epic.setStatus(status);
                    epic.setTaskManager(this);
                    return epic;
                case SUBTASK:
                    int epicId = Integer.parseInt(parts[7]);
                    Epic epicObj = epics.get(epicId);
                    if (epicObj == null) {
                        epicObj = new Epic("Temp", "Temp", null, null);
                        epicObj.setId(epicId);
                        epicObj.setTaskManager(this);
                        epics.put(epicId, epicObj);
                    }
                    Subtask subtask = new Subtask(title, description, duration, startTime, epicObj);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    return subtask;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!file.exists()) {
            return manager;
        }
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            if (lines.length <= 1) {
                return manager;
            }
            for (int i = 1; i < lines.length; i++) {
                if (!lines[i].trim().isEmpty()) {
                    Task task = manager.fromString(lines[i]);
                    if (task.getType() == TaskType.TASK) {
                        manager.tasks.put(task.getId(), task);
                    } else if (task.getType() == TaskType.EPIC) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task.getType() == TaskType.SUBTASK) {
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtask(subtask);
                        }
                    }
                    manager.idCounter = Math.max(manager.idCounter, task.getId());
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading from file: " + file.getPath(), e);
        }
        return manager;
    }
}