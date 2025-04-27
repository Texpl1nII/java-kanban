import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager manager;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.createEpic(new Epic("Epic", "Description"));
        subtask = new Subtask("Subtask", "Description", epic);
    }

    @Test
    void shouldAddAndFindTasksById() {
        Task task = manager.createTask(new Task("Task", "Description"));
        Subtask createdSubtask = manager.createSubtask(new Subtask("Subtask", "Description", epic));

        assertEquals(task, manager.getTask(task.getId()), "Task should be found by ID");
        assertEquals(epic, manager.getEpic(epic.getId()), "Epic should be found by ID");
        assertEquals(createdSubtask, manager.getSubtask(createdSubtask.getId()), "Subtask should be found by ID");
    }

    @Test
    void epicCannotBeItsOwnSubtask() {
        subtask.setId(epic.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.createSubtask(subtask);
        });
        assertEquals("Subtask cannot be its own epic", exception.getMessage());
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        Subtask createdSubtask = manager.createSubtask(subtask);
        createdSubtask.setId(epic.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            manager.updateSubtask(createdSubtask);
        });
        assertEquals("Subtask cannot be its own epic", exception.getMessage());
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = manager.createTask(new Task("Task", "Description"));
        manager.getTask(task.getId()); // Add to history
        assertFalse(manager.getHistory().isEmpty(), "History should contain the task");

        manager.deleteTask(task.getId());
        assertTrue(manager.getHistory().isEmpty(), "Task should be removed from history");
    }

    @Test
    void shouldRemoveEpicAndSubtasksFromHistoryWhenDeleted() {
        Subtask createdSubtask = manager.createSubtask(subtask);
        manager.getEpic(epic.getId()); // Add epic to history
        manager.getSubtask(createdSubtask.getId()); // Add subtask to history
        assertEquals(2, manager.getHistory().size(), "History should contain epic and subtask");

        manager.deleteEpic(epic.getId());
        assertTrue(manager.getHistory().isEmpty(), "Epic and subtask should be removed from history");
    }

    @Test
    void shouldMaintainEpicSubtaskIntegrity() {
        Subtask createdSubtask = manager.createSubtask(subtask);
        assertEquals(1, manager.getEpicSubtasks(epic.getId()).size(), "Epic should have one subtask");

        manager.deleteSubtask(createdSubtask.getId());
        assertTrue(manager.getEpicSubtasks(epic.getId()).isEmpty(), "Epic should have no subtasks after deletion");
    }

    @Test
    void shouldPreventInconsistentSetterUpdates() {
        Task task = manager.createTask(new Task("Task", "Description"));
        task.setId(999); // Attempt to change ID manually

        manager.updateTask(task);
        assertNotNull(manager.getTask(task.getId()), "Task should still be accessible with original ID");
    }
}