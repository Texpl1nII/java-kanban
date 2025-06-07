package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;
import handler.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class TasksHttpHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHttpHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET")) {
                if (path.equals("/tasks")) {
                    List<Task> tasks = taskManager.getAllTasks();
                    sendText(exchange, gson.toJson(tasks), 200);
                } else {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    Task task = taskManager.getTask(id);
                    sendText(exchange, gson.toJson(task), 200);
                }
            } else if (method.equals("POST")) {
                String body = readRequestBody(exchange);
                Task task = gson.fromJson(body, Task.class);
                if (path.equals("/tasks")) {
                    Task createdTask = taskManager.createTask(task);
                    sendText(exchange, gson.toJson(new Response("Task created", createdTask.getId())), 201);
                } else {
                    int id = getIdFromPath(exchange);
                    if (id == -1) {
                        sendNotFound(exchange);
                        return;
                    }
                    task.setId(id);
                    taskManager.updateTask(task);
                    sendText(exchange, "{\"status\":\"Task updated\"}", 201);
                }
            } else if (method.equals("DELETE")) {
                int id = getIdFromPath(exchange);
                if (id == -1) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.deleteTask(id);
                sendText(exchange, "{\"status\":\"Task deleted\"}", 201);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }

    private record Response(String status, int id) {
    }
}