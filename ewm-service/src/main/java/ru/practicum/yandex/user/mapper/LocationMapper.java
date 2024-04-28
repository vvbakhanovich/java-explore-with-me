package ru.practicum.yandex.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.user.dto.LocationDto;
import ru.practicum.yandex.user.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toModel(LocationDto locationDto);

    LocationDto toDto(Location location);
}
