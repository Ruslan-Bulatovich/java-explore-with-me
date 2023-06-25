package ru.practicum.server.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.constants.Pattern;
import ru.practicum.server.models.EndpointHit;


@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    @Mapping(target = "timestamp", source = "timestamp", dateFormat = Pattern.DATE)
    EndpointHit toEntity(EndpointHitDto endpointHitDto);
}
