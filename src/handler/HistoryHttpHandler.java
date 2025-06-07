package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;
import handler.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class HistoryHttpHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public HistoryHttpHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET") && path.equals("/history")) {
                List<Task> history = taskManager.getHistory();
                sendText(exchange, gson.toJson(history), 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}