package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.HitDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
public class HitClient extends Client {

    @Autowired
    public HitClient(@Value("${service.url}") String url, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(url))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public ResponseEntity<Object> getStatistic(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {
        String path = "/stats";
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "unique", unique,
                "uris", uris
        );
        return get(path, parameters);
    }

    public ResponseEntity<Object> createHit(HitDto hitDto) {
        String path = "/hit";
        return post(path, hitDto);
    }

    public ResponseEntity<Object> createHit(String app, String uri, String ip, LocalDateTime timestamp) {
        String path = "/hit";
        HitDto hitDto = new HitDto(app, uri, ip, timestamp);
        return post(path, hitDto);
    }
}