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
    void shouldNotAddNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty(), "History should be empty when adding null task");
    }

    @Test
    void shouldRemoveDuplicatesInHistory() {
        Task task = new Task("Task", "Description");
        task.setId(1);

        historyManager.add(task); // First add
        historyManager.add(task); // Second add (should replace first)

        assertEquals(1, historyManager.getHistory().size(), "History should contain only one instance of the task");
        assertEquals(task, historyManager.getHistory().get(0), "Latest task should be in history");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        assertEquals(1, historyManager.getHistory().size(), "History should contain one task after removal");
        assertEquals(task2, historyManager.getHistory().get(0), "Remaining task should be task2");
    }

    @Test
    void shouldMaintainOrderOfTasks() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        assertEquals(3, historyManager.getHistory().size(), "History should contain three tasks");
        assertEquals(task1, historyManager.getHistory().get(0), "First task should be task1");
        assertEquals(task2, historyManager.getHistory().get(1), "Second task should be task2");
        assertEquals(task3, historyManager.getHistory().get(2), "Third task should be task3");
    }

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "History should be empty initially");
    }
}