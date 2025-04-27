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
}