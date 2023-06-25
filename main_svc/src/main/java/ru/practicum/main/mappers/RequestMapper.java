package ru.practicum.main.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.request.RequestDto;
import ru.practicum.main.models.Request;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    RequestDto toRequestDto(Request request);

    List<RequestDto> toRequestDtoList(List<Request> requests);
}
