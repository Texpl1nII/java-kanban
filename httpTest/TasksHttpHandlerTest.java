package httpTest;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Task;
import model.Status;
import org.junit.jupiter.api.*;
import util.GsonConfig;
import http.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksHttpHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager taskManager;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        gson = GsonConfig.getGson();
        client = HttpClient.newHttpClient();
        taskManager.clearTasks();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> tasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());
        assertEquals(1, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task returnedTask = gson.fromJson(response.body(), Task.class);
        assertEquals("Task 1", returnedTask.getTitle());
    }

    @Test
    public void testGetTaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Resource not found"));
    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 5, 12, 0));
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
    }

    @Test
    public void testCreateTaskOverlap() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 5, 12, 0));
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 5, 12, 15));
        String taskJson = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Task overlaps"));
    }
}