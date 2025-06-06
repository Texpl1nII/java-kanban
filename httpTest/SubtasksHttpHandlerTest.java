package httpTest;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
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

public class SubtasksHttpHandlerTest {
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
        taskManager.clearSubtasks();
        taskManager.clearEpics();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        taskManager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Subtask>>(){}.getType());
        assertEquals(1, subtasks.size());
        assertEquals("Subtask 1", subtasks.get(0).getTitle());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc", null, null);
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now(), epic);
        taskManager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask returnedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals("Subtask 1", returnedSubtask.getTitle());
    }

    @Test
    public void testGetSubtaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Resource not found"));
    }
}