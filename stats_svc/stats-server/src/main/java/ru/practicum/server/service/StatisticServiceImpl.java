package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ListViewStats;
import ru.practicum.dto.ResponseEndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.server.mapper.StatisticMapper;
import ru.practicum.server.repository.StatisticRepository;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatisticServiceImpl implements StatisticService {
    private final StatisticRepository statistics;
    private final StatisticMapper mapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ResponseEndpointHitDto addEndpointHit(CreateEndpointHitDto createEndpointHitDto) {
        return mapper.mapToResponseEndpointHitDto(statistics.save(mapper.mapToEndpointHit(createEndpointHitDto)));
    }

    @Override
    public ListViewStats getStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime parseStart = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime parseEnd = LocalDateTime.parse(end, FORMATTER);
        List<ViewStats> viewStats;
        ListViewStats response;
        if (unique) {
            viewStats = uris == null
                    ? statistics.getUniqueViewStatsByStartAndEndTime(parseStart, parseEnd)
                    : statistics.getUniqueUrisViewStatsByStartAndEndTime(parseStart, parseEnd, uris);
        } else {
            viewStats = uris == null ? statistics.getViewStatsByStartAndEndTime(parseStart, parseEnd)
                    : statistics.getUrisViewStatsByStartAndEndTime(parseStart, parseEnd, uris);
        }
        response = ListViewStats.builder().viewStats(viewStats).build();
        return response;
    }
}
