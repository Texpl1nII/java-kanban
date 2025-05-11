package manager;

import manager.TaskManager;
import manager.InMemoryTaskManager;
import manager.HistoryManager;
import manager.InMemoryHistoryManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}