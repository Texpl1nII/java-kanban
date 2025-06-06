package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.ManagerSaveException;
import exception.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Resource not found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Task overlaps with existing tasks\"}", 406);
    }

    protected void sendInternalServerError(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Internal server error\"}", 500);
    }

    protected int getIdFromPath(HttpExchange h) {
        String[] pathParts = h.getRequestURI().getPath().split("/");
        try {
            return Integer.parseInt(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected String readRequestBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void handleException(HttpExchange h, Exception e) throws IOException {
        if (e instanceof NotFoundException) {
            sendNotFound(h);
        } else if (e instanceof ManagerSaveException) {
            sendInternalServerError(h);
        } else {
            sendHasInteractions(h);
        }
    }
}