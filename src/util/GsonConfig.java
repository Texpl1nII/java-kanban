package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonConfig {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>) (src, type, context) ->
                                context.serialize(src != null ? src.format(FORMATTER) : null))
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonDeserializer<LocalDateTime>) (json, type, context) ->
                                json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString(), FORMATTER))
                .registerTypeAdapter(Duration.class,
                        (com.google.gson.JsonSerializer<Duration>) (src, type, context) ->
                                context.serialize(src != null ? src.toMinutes() : null))
                .registerTypeAdapter(Duration.class,
                        (com.google.gson.JsonDeserializer<Duration>) (json, type, context) ->
                                json.isJsonNull() ? null : Duration.ofMinutes(json.getAsLong()))
                .create();
    }
}