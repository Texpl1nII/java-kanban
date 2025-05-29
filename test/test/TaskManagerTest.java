package test;

import model.Task;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Status;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @Test
    void testCreateAndGetTaskWithDurationAndStartTime() {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 10, 0));
        taskManager.createTask(task);
        Task retrieved = taskManager.getTask(task.getId());
        assertEquals(task.getDuration(), retrieved.getDuration(), "Продолжительность должна совпадать");
        assertEquals(task.getStartTime(), retrieved.getStartTime(), "Время начала должно совпадать");
        assertEquals(task.getStartTime().plus(task.getDuration()), retrieved.getEndTime(), "Время окончания должно быть корректным");
    }

    @Test
    void testEpicDurationAndTimes() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 10, 0), epic);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 11, 0), epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Duration.ofMinutes(90), epic.getDuration(), "Продолжительность эпика должна быть суммой подзадач");
        assertEquals(LocalDateTime.of(2025, 5, 27, 10, 0), epic.getStartTime(), "Время начала эпика — самое раннее");
        assertEquals(LocalDateTime.of(2025, 5, 27, 12, 0), epic.getEndTime(), "Время окончания эпика — самое позднее");
    }

    @Test
    void testEpicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.NEW, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        subtask1.setStatus(Status.DONE);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.DONE, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void testEpicStatusMixed() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        subtask1.setStatus(Status.NEW);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), epic);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void testEpicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.createSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epic.getId()).getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void testTaskOverlap() {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 10, 0));
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 10, 30));
        taskManager.createTask(task1);
        assertThrows(IllegalStateException.class, () -> taskManager.createTask(task2), "Должно быть выброшено исключение при пересечении задач");
    }

    @Test
    void testNoOverlap() {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 10, 0));
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 11, 0));
        assertDoesNotThrow(() -> {
            taskManager.createTask(task1);
            taskManager.createTask(task2);
        }, "Задачи без пересечения должны быть добавлены");
    }

    @Test
    void testPrioritizedTasksOrder() {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 10, 0));
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 9, 0));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(task2, prioritized.get(0), "Задачи должны быть отсортированы по startTime");
        assertEquals(task1, prioritized.get(1), "Задачи должны быть отсортированы по startTime");
    }
}