package manager;

import manager.TaskManager;
import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;

public class Managers {
    public static TaskManager getDefault() {
        try {
            File file = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(file);
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp file for FileBackedTaskManager", e);
        }
    }
    //тут нет пробела

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}