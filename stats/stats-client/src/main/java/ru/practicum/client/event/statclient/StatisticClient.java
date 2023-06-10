package ru.practicum.client.event.statclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.client.BaseClient;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatisticClient extends BaseClient {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatisticClient(@Value("${STAT-SERVER-URL}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build());
    }

    public Long getViews(Long eventId) {
        String url = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        Map<String, Object> parameters = Map.of(
                "start", LocalDateTime.now().minusYears(100).format(formatter),
                "end", LocalDateTime.now().format(formatter),
                "uris", "/events/" + eventId,
                "unique", "false"
        );
        ResponseEntity<List<ViewStats>> response = get(url, parameters);
        List<ViewStats> viewStatsList = response.hasBody() ? response.getBody() : null;
        return viewStatsList != null && !viewStatsList.isEmpty() ? viewStatsList.get(0).getHits() : 0L;
    }

    public void postStats(HttpServletRequest servlet, String app) {
        CreateEndpointHitDto hit = CreateEndpointHitDto
                .builder()
                .app(app)
                .ip(servlet.getRemoteAddr())
                .uri(servlet.getRequestURI())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
        post("/hit", hit);
    }


}