package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;
import handler.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class PrioritizedHttpHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHttpHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET") && path.equals("/prioritized")) {
                List<Task> prioritized = taskManager.getPrioritizedTasks();
                sendText(exchange, gson.toJson(prioritized), 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}