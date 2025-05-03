package test;

import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(1);

        assertEquals(task1, task2, "Tasks with the same ID should be equal");
        assertEquals(task1.hashCode(), task2.hashCode(), "Hash codes should match for equal tasks");
    }

    @Test
    void taskFieldsShouldRemainUnchanged() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        task.setStatus(Status.IN_PROGRESS);

        Task sameTask = new Task("Task", "Description");
        sameTask.setId(1);
        sameTask.setStatus(Status.IN_PROGRESS);

        assertEquals(task.getTitle(), sameTask.getTitle(), "Title should remain unchanged");
        assertEquals(task.getDescription(), sameTask.getDescription(), "Description should remain unchanged");
        assertEquals(task.getStatus(), sameTask.getStatus(), "Status should remain unchanged");
    }
}