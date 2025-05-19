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
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    void setUp(@TempDir File tempDir) throws IOException {
        tempFile = new File(tempDir, "tasks.csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void testCreateAndSaveTask() {
        Task task = new Task("Task 1", "Description 1") {
            @Override
            public TaskType getType() {
                return TaskType.TASK;
            }
        };
        Task createdTask = manager.createTask(task);

        assertNotNull(createdTask, "Task should be created");
        assertTrue(tempFile.exists(), "File should exist after saving");
        assertEquals(1, manager.getAllTasks().size(), "Task list should contain one task");

        try {
            String content = Files.readString(tempFile.toPath());
            assertTrue(content.contains("1,TASK,Task 1,NEW,Description 1,"), "File should contain task data");
        } catch (IOException e) {
            fail("Failed to read file: " + e.getMessage());
        }
    }

    @Test
    void testCreateAndSaveEpicAndSubtask() {
        Epic epic = new Epic("Epic 1", "Epic Description") {
            @Override
            public TaskType getType() {
                return TaskType.EPIC;
            }
        };
        Epic createdEpic = manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", createdEpic) {
            @Override
            public TaskType getType() {
                return TaskType.SUBTASK;
            }
        };
        Subtask createdSubtask = manager.createSubtask(subtask);

        assertNotNull(createdEpic, "Epic should be created");
        assertNotNull(createdSubtask, "Subtask should be created");
        assertEquals(1, manager.getAllEpics().size(), "Epic list should contain one epic");
        assertEquals(1, manager.getAllSubtasks().size(), "Subtask list should contain one subtask");

        try {
            String content = Files.readString(tempFile.toPath());
            assertTrue(content.contains("1,EPIC,Epic 1,NEW,Epic Description,"), "File should contain epic data");
            assertTrue(content.contains("2,SUBTASK,Subtask 1,NEW,Subtask Description,1"), "File should contain subtask data");
        } catch (IOException e) {
            fail("Failed to read file: " + e.getMessage());
        }
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Task 1", "Description 1") {
            @Override
            public TaskType getType() {
                return TaskType.TASK;
            }
        };
        Task createdTask = manager.createTask(task);
        createdTask.setTitle("Updated Task");
        createdTask.setStatus(Status.IN_PROGRESS);
        manager.updateTask(createdTask);

        assertEquals("Updated Task", manager.getTask(createdTask.getId()).getTitle(), "Task title should be updated");
        assertEquals(Status.IN_PROGRESS, manager.getTask(createdTask.getId()).getStatus(), "Task status should be updated");

        try {
            String content = Files.readString(tempFile.toPath());
            assertTrue(content.contains("1,TASK,Updated Task,IN_PROGRESS,Description 1,"), "File should contain updated task data");
        } catch (IOException e) {
            fail("Failed to read file: " + e.getMessage());
        }
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Task 1", "Description 1") {
            @Override
            public TaskType getType() {
                return TaskType.TASK;
            }
        };
        Task createdTask = manager.createTask(task);
        manager.deleteTask(createdTask.getId());

        assertNull(manager.getTask(createdTask.getId()), "Task should be deleted");
        assertEquals(0, manager.getAllTasks().size(), "Task list should be empty");

        try {
            String content = Files.readString(tempFile.toPath());
            assertEquals("id,type,name,status,description,epic\n", content, "File should only contain header");
        } catch (IOException e) {
            fail("Failed to read file: " + e.getMessage());
        }
    }

    @Test
    void testLoadFromFile() {
        Epic epic = new Epic("Epic 1", "Epic Description") {
            @Override
            public TaskType getType() {
                return TaskType.EPIC;
            }
        };
        Task task = new Task("Task 1", "Description 1") {
            @Override
            public TaskType getType() {
                return TaskType.TASK;
            }
        };
        manager.createEpic(epic);
        manager.createTask(task);
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", epic) {
            @Override
            public TaskType getType() {
                return TaskType.SUBTASK;
            }
        };
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getAllTasks().size(), "Loaded manager should have one task");
        assertEquals(1, loadedManager.getAllEpics().size(), "Loaded manager should have one epic");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Loaded manager should have one subtask");
        assertEquals("Task 1", loadedManager.getTask(2).getTitle(), "Loaded task should have correct title");
        assertEquals("Epic 1", loadedManager.getEpic(1).getTitle(), "Loaded epic should have correct title");
        assertEquals("Subtask 1", loadedManager.getSubtask(3).getTitle(), "Loaded subtask should have correct title");
        assertEquals(1, loadedManager.getSubtask(3).getEpicId(), "Subtask should be linked to correct epic");
    }

    @Test
    void testLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Tasks should be empty for empty file");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Epics should be empty for empty file");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Subtasks should be empty for empty file");
    }

    @Test
    void testSaveThrowsManagerSaveException() {
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(new File("/invalid/path/tasks.csv"));
        Task task = new Task("Task 1", "Description 1") {
            @Override
            public TaskType getType() {
                return TaskType.TASK;
            }
        };

        assertThrows(ManagerSaveException.class, () -> invalidManager.createTask(task),
                "Should throw ManagerSaveException for invalid file path");
    }
}