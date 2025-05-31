package test;

import model.Epic;
import model.Subtask;
import model.Task;
import model.Status;
import model.TaskType;
import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    void setUp(@TempDir File tempDir) throws IOException {
        tempFile = new File(tempDir, "tasks.csv");
        taskManager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void testSaveAndLoadWithDurationAndStartTime() throws IOException {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 10, 0));
        Epic epic = new Epic("Epic 1", "Epic Desc", null, null);
        taskManager.createTask(task);
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Subtask Desc", Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 27, 11, 0), epic);
        taskManager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTask(task.getId());
        assertNotNull(loadedTask, "Задача должна существовать");
        assertEquals(Duration.ofMinutes(30), loadedTask.getDuration(), "Продолжительность задачи должна быть сохранена");
        assertEquals(LocalDateTime.of(2025, 5, 27, 10, 0), loadedTask.getStartTime(), "Время начала задачи должно быть сохранено");

        Epic loadedEpic = loadedManager.getEpic(epic.getId());
        assertNotNull(loadedEpic, "Эпик должен существовать");
        assertEquals(Duration.ofMinutes(60), loadedEpic.getDuration(), "Продолжительность эпика должна быть корректной");
        assertEquals(LocalDateTime.of(2025, 5, 27, 11, 0), loadedEpic.getStartTime(), "Время начала эпика должно быть корректным");

        List<Subtask> epicSubtasks = loadedManager.getEpicSubtasks(epic.getId());
        assertEquals(subtask.getId(), epicSubtasks.get(0).getId(), "Подзадача должна быть связана с эпиком");
    }

    @Test
    void testSaveThrowsManagerSaveException() {
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(new File("/invalid/path/tasks.csv"));
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        assertThrows(ManagerSaveException.class, () -> invalidManager.createTask(task),
                "Должно быть выброшено исключение для неверного пути");
    }

    @Test
    void testLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны отсутствовать");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны отсутствовать");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны отсутствовать");
    }

    @Test
    void testSaveAndLoadEmptyManager() throws IOException {
        taskManager.clearTasks();
        taskManager.clearEpics();
        taskManager.clearSubtasks();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Задачи должны отсутствовать");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Эпики должны отсутствовать");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Подзадачи должны отсутствовать");
    }

    @Test
    void testHistorySavedAndLoaded() throws IOException {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);
        taskManager.getTask(task.getId());
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getHistory().size(), "История должна содержать одну задачу");
        assertEquals(task.getId(), loadedManager.getHistory().get(0).getId(), "Задача в истории должна совпадать");
    }

    @Test
    void testPrioritizedTasksSavedAndLoaded() throws IOException {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 10, 0));
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 27, 9, 0));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(task2.getId(), prioritized.get(0).getId(), "Задачи должны быть отсортированы по startTime");
        assertEquals(task1.getId(), prioritized.get(1).getId(), "Задачи должны быть отсортированы по startTime");
    }
}