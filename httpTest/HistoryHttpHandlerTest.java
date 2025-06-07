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

public class HistoryHttpHandlerTest {
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
    public void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.createTask(task);
        taskManager.getTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());
        assertEquals(1, history.size());
        assertEquals("Task 1", history.get(0).getTitle());
    }
}