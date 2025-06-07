package httptest;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Task;
import model.Status;
import org.junit.jupiter.api.*;
import util.GsonConfig;
import http.HttpTaskServer;
import util.GsonConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHttpHandlerTest {
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
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 5, 10, 0));
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 5, 9, 0));
        Task task3 = new Task("Task 3", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 6, 5, 11, 0));
        taskManager.createTask(task2);
        taskManager.createTask(task1);
        taskManager.createTask(task3);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Response status should be 200 OK");
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertEquals(3, prioritizedTasks.size(), "Should return 3 tasks");
        assertEquals("Task 2", prioritizedTasks.get(0).getTitle(), "First task should be Task 2 (earliest start time)");
        assertEquals("Task 1", prioritizedTasks.get(1).getTitle(), "Second task should be Task 1");
        assertEquals("Task 3", prioritizedTasks.get(2).getTitle(), "Third task should be Task 3 (latest start time)");
    }

    @Test
    public void testGetPrioritizedTasksEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Response status should be 200 OK");
        List<Task> prioritizedTasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertTrue(prioritizedTasks.isEmpty(), "Should return an empty list when no tasks exist");
    }

    @Test
    public void testGetPrioritizedTasksInvalidMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Response status should be 404 for invalid method");
        assertTrue(response.body().contains("Resource not found"), "Response should indicate resource not found");
    }
}