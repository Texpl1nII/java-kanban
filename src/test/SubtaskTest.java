import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic);
        subtask2.setId(2);

        assertEquals(subtask1, subtask2, "Subtasks with the same ID should be equal");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Hash codes should match for equal subtasks");
    }
}