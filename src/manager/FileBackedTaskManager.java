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
import java.util.List;
import java.util.ArrayList;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
        hasOverlaps(task);
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        hasOverlaps(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            epic = new Epic("Temp Epic", "Temporary epic for subtask", null, null);
            epic = createEpic(epic);
            subtask = new Subtask(subtask.getTitle(), subtask.getDescription(), subtask.getDuration(), subtask.getStartTime(), epic);
        }
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        hasOverlaps(task);
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
        hasOverlaps(subtask);
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
        String title = task.getTitle().replace(",", ";");
        String description = task.getDescription().replace(",", ";");
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(), type, title, task.getStatus(), description,
                durationStr, startTimeStr, epicId);
    }

    private Task fromString(String value) {
        if (value.trim().isEmpty()) {
            return null;
        }
        String[] parts = value.split(",", -1);
        if (parts.length < 8) {
            System.err.println("Malformed CSV line: " + value);
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String title = parts[2].replace(";", ",");
            Status status = Status.valueOf(parts[3]);
            String description = parts[4].replace(";", ",");
            Duration duration = parts[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[5]));
            LocalDateTime startTime = parts[6].isEmpty() ? null : LocalDateTime.parse(parts[6], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String epicIdStr = parts[7];

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
                    return epic;
                case SUBTASK:
                    int epicId = epicIdStr.isEmpty() ? 0 : Integer.parseInt(epicIdStr);
                    Epic epicObj = epics.get(epicId);
                    if (epicObj == null) {
                        epicObj = new Epic("Temp", "Temp", null, null);
                        epicObj.setId(epicId);
                        epics.put(epicId, epicObj);
                    }
                    Subtask subtask = new Subtask(title, description, duration, startTime, epicObj);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    return subtask;
                default:
                    System.err.println("Unknown task type: " + type);
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + value + ", error: " + e.getMessage());
            return null;
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!file.exists()) {
            return manager;
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) {
                return manager;
            }
            List<String> subtaskLines = new ArrayList<>();
            boolean historySection = false;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    historySection = true;
                    continue;
                }
                if (historySection) {
                    break;
                }
                String[] parts = line.split(",", -1);
                if (parts.length >= 2 && parts[1].equals(TaskType.SUBTASK.name())) {
                    subtaskLines.add(line);
                } else {
                    Task task = manager.fromString(line);
                    if (task != null) {
                        if (task.getType() == TaskType.TASK) {
                            manager.tasks.put(task.getId(), task);
                            if (task.getStartTime() != null) {
                                manager.prioritizedTasks.add(task);
                            }
                        } else if (task.getType() == TaskType.EPIC) {
                            manager.epics.put(task.getId(), (Epic) task);
                        }
                        manager.idCounter = Math.max(manager.idCounter, task.getId());
                    } else {
                        System.err.println("Skipped invalid task line: " + line);
                    }
                }
            }
            for (String line : subtaskLines) {
                Task task = manager.fromString(line);
                if (task != null) {
                    Subtask subtask = (Subtask) task;
                    manager.subtasks.put(subtask.getId(), subtask);
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtask(subtask);
                    }
                    if (subtask.getStartTime() != null) {
                        manager.prioritizedTasks.add(subtask);
                    }
                    manager.idCounter = Math.max(manager.idCounter, task.getId());
                } else {
                    System.err.println("Skipped invalid subtask line: " + line);
                }
            }
            if (historySection && !lines.isEmpty()) {
                String historyLine = null;
                for (int i = lines.size() - 1; i >= 0; i--) {
                    String line = lines.get(i).trim();
                    if (!line.isEmpty()) {
                        historyLine = line;
                        break;
                    }
                }
                if (historyLine != null && !historyLine.equals("id,type,name,status,description,duration,startTime,epicId")) {
                    String[] historyIds = historyLine.split(",");
                    for (String idStr : historyIds) {
                        if (!idStr.trim().isEmpty()) {
                            try {
                                int id = Integer.parseInt(idStr.trim());
                                Task task = manager.tasks.get(id);
                                if (task == null) task = manager.epics.get(id);
                                if (task == null) task = manager.subtasks.get(id);
                                if (task != null) {
                                    manager.historyManager.add(task);
                                } else {
                                    System.err.println("Skipping invalid history ID: " + idStr);
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("Skipping invalid history ID: " + idStr);
                            }
                        }
                    }
                }
            }
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading from file: " + file.getPath(), e);
        }
    }
}