import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Task", "Description");
        task.setId(1);

        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "History should contain one task");
        assertEquals(task, historyManager.getHistory().get(0), "Added task should be in history");
    }

    @Test
    void historyShouldNotExceedTenTasks() {
        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task " + i, "Description " + i);
            task.setId(i);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size(), "History should not exceed 10 tasks");
        assertEquals(2, historyManager.getHistory().get(0).getId(), "Oldest task should be removed");
    }
}