package com.example.fooddeliverymobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class RestOperations {
    private static final int TIMEOUT_MILLIS = 15_000;

    private RestOperations() {
    }

    public static HttpResult sendGet(String url, String token) throws IOException {
        return send("GET", url, null, token);
    }

    public static HttpResult sendPost(String url, String json) throws IOException {
        return send("POST", url, json, null);
    }

    public static HttpResult sendPost(String url, String json, String token) throws IOException {
        return send("POST", url, json, token);
    }

    public static HttpResult sendPut(String url, String json, String token) throws IOException {
        return send("PUT", url, json, token);
    }

    public static HttpResult sendDelete(String url, String token) throws IOException {
        return send("DELETE", url, null, token);
    }

    private static HttpResult send(String method, String urlValue, String json, String token)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlValue).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setReadTimeout(TIMEOUT_MILLIS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (token != null && !token.isBlank()) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }

            if (json != null) {
                connection.setDoOutput(true);
                try (OutputStreamWriter writer = new OutputStreamWriter(
                        connection.getOutputStream(), StandardCharsets.UTF_8)) {
                    writer.write(json);
                }
            }

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            return new HttpResult(statusCode, readBody(stream));
        } finally {
            connection.disconnect();
        }
    }

    private static String readBody(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    public record HttpResult(int statusCode, String body) {
        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
