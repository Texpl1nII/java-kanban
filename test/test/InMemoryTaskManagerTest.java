package test;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testClearTasksRemovesAllTasks() {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.clearTasks();
        assertTrue(taskManager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Приоритетный список должен быть пуст");
    }

    @Test
    void testClearEpicsRemovesEpicsAndSubtasks() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        taskManager.createSubtask(subtask);
        taskManager.clearEpics();
        assertTrue(taskManager.getAllEpics().isEmpty(), "Все эпики должны быть удалены");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Приоритетный список должен быть пуст");
    }

    @Test
    void testDeleteTaskRemovesFromHistory() {
        Task task = new Task("Task", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);
        taskManager.getTask(task.getId());
        taskManager.deleteTask(task.getId());
        assertFalse(taskManager.getHistory().contains(task), "Задача должна быть удалена из истории");
    }

    @Test
    void testDeleteEpicRemovesSubtasksAndHistory() {
        Epic epic = new Epic("Epic", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        taskManager.createSubtask(subtask);
        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subtask.getId());
        taskManager.deleteEpic(epic.getId());
        assertFalse(taskManager.getHistory().contains(epic), "Эпик должен быть удален из истории");
        assertFalse(taskManager.getHistory().contains(subtask), "Подзадача должна быть удалена из истории");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадачи должны быть удалены");
    }

    @Test
    void testTaskWithoutStartTimeNotInPrioritizedList() {
        Task task = new Task("Task", "Desc", Duration.ofMinutes(30), null);
        taskManager.createTask(task);
        assertFalse(taskManager.getPrioritizedTasks().contains(task), "Задача без startTime не должна быть в приоритетном списке");
    }

    @Test
    void testUpdateTaskStatus() {
        Task task = new Task("Task", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        Task updatedTask = taskManager.getTask(task.getId());
        assertNotNull(updatedTask, "Задача должна существовать");
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus(), "Статус задачи должен обновиться");
    }
}