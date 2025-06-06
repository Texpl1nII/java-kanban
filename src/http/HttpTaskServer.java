package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import util.GsonConfig;
import handler.TasksHttpHandler;
import handler.SubtasksHttpHandler;
import handler.EpicsHttpHandler;
import handler.PrioritizedHttpHandler;
import handler.HistoryHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = GsonConfig.getGson();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", (HttpHandler) new TasksHttpHandler(taskManager, gson));
        server.createContext("/subtasks", (HttpHandler) new SubtasksHttpHandler(taskManager, gson));
        server.createContext("/epics", (HttpHandler) new EpicsHttpHandler(taskManager, gson));
        server.createContext("/history", (HttpHandler) new HistoryHttpHandler(taskManager, gson));
        server.createContext("/prioritized", (HttpHandler) new PrioritizedHttpHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    public void stop() {
        server.stop(0);
    }

    public static Gson getGson() {
        return GsonConfig.getGson();
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();
    }
}