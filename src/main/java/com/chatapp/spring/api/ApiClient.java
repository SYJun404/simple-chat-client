package com.chatapp.spring.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper(); // Jackson

    public static <T> T post(String path, Object body, TypeReference<T> typeRef)
        throws Exception {
        String json = MAPPER.writeValueAsString(body);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json));

        HttpResponse<String> response = CLIENT.send(
            builder.build(),
            HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 400) {
            throw new ApiException(response.statusCode(), response.body());
        }

        return MAPPER.readValue(response.body(), typeRef);
    }

    public static <T> T get(String path, Class<T> responseType)
        throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .GET()
            .build();

        HttpResponse<String> response = CLIENT.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        return MAPPER.readValue(response.body(), responseType);
    }
}
