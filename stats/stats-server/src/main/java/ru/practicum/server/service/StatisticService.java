package ru.practicum.server.service;


import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ListViewStats;
import ru.practicum.dto.ResponseEndpointHitDto;

import java.util.List;

public interface StatisticService {
    ResponseEndpointHitDto addEndpointHit(CreateEndpointHitDto createEndpointHitDto);

    ListViewStats getStats(String start, String end, List<String> uris, Boolean unique);
}
