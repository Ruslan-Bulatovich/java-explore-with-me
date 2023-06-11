package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ListViewStats;
import ru.practicum.dto.ResponseEndpointHitDto;
import ru.practicum.server.service.StatisticService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/stats")
    public ResponseEntity<ListViewStats> getStats(@RequestParam String start,
                                                  @RequestParam String end,
                                                  @RequestParam(required = false) List<String> uris,
                                                  @RequestParam(defaultValue = "false") Boolean unique) {
        return ResponseEntity.status(HttpStatus.OK).body(statisticService.getStats(start, end, uris, unique));
    }

    @PostMapping("/hit")
    public ResponseEntity<ResponseEndpointHitDto> addEndpointHit(@RequestBody @Valid CreateEndpointHitDto endpointHitDto) {
        log.info("addEndpointHit: {}", endpointHitDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(statisticService.addEndpointHit(endpointHitDto));
    }


}
