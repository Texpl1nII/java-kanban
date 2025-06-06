package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import handler.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class EpicsHttpHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHttpHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET")) {
                if (path.equals("/epics")) {
                    List<Epic> epics = taskManager.getAllEpics();
                    sendText(exchange, gson.toJson(epics), 200);
                } else if (path.matches("/epics/\\d+/subtasks")) {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    List<Subtask> subtasks = taskManager.getEpicSubtasks(id);
                    sendText(exchange, gson.toJson(subtasks), 200);
                } else {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    Epic epic = taskManager.getEpic(id);
                    sendText(exchange, gson.toJson(epic), 200);
                }
            } else if (method.equals("POST")) {
                String body = readRequestBody(exchange);
                Epic epic = gson.fromJson(body, Epic.class);
                if (path.equals("/epics")) {
                    taskManager.createEpic(epic);
                    sendText(exchange, "{\"status\":\"Epic created\"}", 201);
                } else {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    epic.setId(id);
                    taskManager.updateEpic(epic);
                    sendText(exchange, "{\"status\":\"Epic updated\"}", 201);
                }
            } else if (method.equals("DELETE")) {
                int id = getIdFromPath(exchange);
                if (id == -1) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteEpic(id);
                sendText(exchange, "{\"status\":\"Epic deleted\"}", 201);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}