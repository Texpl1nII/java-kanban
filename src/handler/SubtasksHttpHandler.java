package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Subtask;
import handler.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class SubtasksHttpHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHttpHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET")) {
                if (path.equals("/subtasks")) {
                    List<Subtask> subtasks = taskManager.getAllSubtasks();
                    sendText(exchange, gson.toJson(subtasks), 200);
                } else {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    Subtask subtask = taskManager.getSubtask(id);
                    sendText(exchange, gson.toJson(subtask), 200);
                }
            } else if (method.equals("POST")) {
                String body = readRequestBody(exchange);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (path.equals("/subtasks")) {
                    taskManager.createSubtask(subtask);
                    sendText(exchange, "{\"status\":\"Subtask created\"}", 201);
                } else {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    subtask.setId(id);
                    taskManager.updateSubtask(subtask);
                    sendText(exchange, "{\"status\":\"Subtask updated\"}", 201);
                }
            } else if (method.equals("DELETE")) {
                int id = getIdFromPath(exchange);
                if (id == -1) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteSubtask(id);
                sendText(exchange, "{\"status\":\"Subtask deleted\"}", 201);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}