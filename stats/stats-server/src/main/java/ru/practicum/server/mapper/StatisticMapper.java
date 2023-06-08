package ru.practicum.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.CreateEndpointHitDto;
import ru.practicum.dto.ResponseEndpointHitDto;
import ru.practicum.server.model.EndpointHit;


@Mapper(componentModel = "spring")
public interface StatisticMapper {

    @Mapping(source = "timestamp", target = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EndpointHit mapToEndpointHit(CreateEndpointHitDto createEndpointHitDto);

    @Mapping(source = "timestamp", target = "timestamp", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ResponseEndpointHitDto mapToResponseEndpointHitDto(EndpointHit endpointHit);
}
