package manager;

import manager.InMemoryTaskManager;
import model.Subtask;
import manager.ManagerSaveException;
import model.Task;
import model.Epic;
import model.Status;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public enum TaskType {
        TASK,
        EPIC,
        SUBTASK
    }

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task createTask(Task task) {
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
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
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
            writer.write("id,type,name,status,description,epic\n");

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
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file: " + file.getPath(), e);
        }
    }

    private String toString(Task task) {
        String type = task instanceof Epic ? TaskType.EPIC.name() :
                task instanceof Subtask ? TaskType.SUBTASK.name() : TaskType.TASK.name();
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";

        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                epic = (Epic) getEpic(epicId);
                if (epic == null) {
                    epic = new Epic("Temp", "Temp");
                    epic.setId(epicId);
                }
                Subtask subtask = new Subtask(title, description, epic);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            if (lines.length <= 1) {
                return manager; // Empty file or only header
            }

            for (int i = 1; i < lines.length; i++) {
                if (!lines[i].trim().isEmpty()) {
                    Task task = manager.fromString(lines[i]);
                    if (task instanceof Task && !(task instanceof Epic || task instanceof Subtask)) {
                        manager.tasks.put(task.getId(), task);
                    } else if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
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