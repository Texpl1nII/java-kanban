package test;

import manager.InMemoryHistoryManager;
import manager.HistoryManager;
import manager.Managers;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void testAddDuplicate() {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        task.setId(1);
        historyManager.add(task);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "Дубликаты не должны добавляться");
        assertEquals(task, historyManager.getHistory().get(0), "Задача должна остаться в истории");
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(2));
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Задача должна быть удалена");
        assertFalse(history.contains(task1), "Задача 1 не должна быть в истории");

        historyManager.add(task1);
        historyManager.remove(2);
        history = historyManager.getHistory();
        assertFalse(history.contains(task2), "Задача 2 не должна быть в истории");

        historyManager.remove(3);
        history = historyManager.getHistory();
        assertFalse(history.contains(task3), "Задача 3 не должна быть в истории");
    }

    @Test
    void testAddNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty(), "Добавление null не должно изменять историю");
    }

    @Test
    void testRemoveNonExistentTask() {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        task.setId(1);
        historyManager.add(task);
        historyManager.remove(999);
        assertEquals(1, historyManager.getHistory().size(), "Удаление несуществующего ID не должно изменять историю");
    }

    @Test
    void testHistoryOrder() {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();
        assertEquals(task1, history.get(0), "Первая добавленная задача должна быть первой в истории");
        assertEquals(task2, history.get(1), "Вторая добавленная задача должна быть второй в истории");
    }
}