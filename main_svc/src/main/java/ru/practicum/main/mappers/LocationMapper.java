package ru.practicum.main.mappers;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.models.Location;

@Mapper(componentModel = "spring")
@Component
public interface LocationMapper {
    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);

}
