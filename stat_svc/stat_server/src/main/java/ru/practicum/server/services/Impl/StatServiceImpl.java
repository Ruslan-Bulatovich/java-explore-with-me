package ru.practicum.server.services.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.mappers.EndpointHitMapper;
import ru.practicum.server.mappers.ViewStatsMapper;
import ru.practicum.server.models.ViewStats;
import ru.practicum.server.repositories.StatServerRepository;
import ru.practicum.server.services.StatService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private final StatServerRepository statServerRepository;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.debug("Save hit by app: " + endpointHitDto.getApp());
        statServerRepository.save(endpointHitMapper.toEntity(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.debug("Received stats.");
        List<ViewStats> viewStats;
        List<ViewStatsDto> response;
        if (unique) {
            viewStats = uris == null
                    ? statServerRepository.getUniqueViewStatsByStartAndEndTime(start, end)
                    : statServerRepository.getUniqueUrisViewStatsByStartAndEndTime(start, end, uris);
        } else {
            viewStats = uris == null ? statServerRepository.getViewStatsByStartAndEndTime(start, end)
                    : statServerRepository.getUrisViewStatsByStartAndEndTime(start, end, uris);
        }

        response = viewStatsMapper.toEntityList(viewStats);
        return response;
    }
}
