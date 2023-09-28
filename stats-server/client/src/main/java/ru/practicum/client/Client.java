package ru.practicum.client;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class Client {
    private final RestTemplate rest;

    public Client(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path, Map<String, Object> parameters) {
        try {
            return rest.exchange(path, HttpMethod.GET, null, Object.class, parameters);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    protected  <T> ResponseEntity<Object> post(String path, T body) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body);
        try {
            return rest.exchange(path, HttpMethod.POST, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    private static ResponseEntity<Object> prepareResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        } else {
            if (response.hasBody()) {
                return ResponseEntity.status(response.getStatusCode())
                        .body(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .build();
            }
        }
    }
}