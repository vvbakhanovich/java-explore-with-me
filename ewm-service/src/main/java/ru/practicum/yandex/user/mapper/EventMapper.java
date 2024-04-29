package ru.practicum.yandex.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.yandex.category.mapper.CategoryMapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.user.dto.EventFullDto;
import ru.practicum.yandex.user.dto.EventShortDto;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.dto.UpdateEventUserRequest;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.EventShort;
import ru.practicum.yandex.user.model.EventState;
import ru.practicum.yandex.user.model.Location;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.*;

@Mapper(componentModel = "spring", uses = {LocationMapper.class, UserMapper.class, CategoryMapper.class})
public interface EventMapper {

    NewEvent toModel(NewEventDto newEventDto);

    EventFullDto toDto(Event addedEvent);

    EventShortDto toShortDto(Event event);

    @Mapping(source = "confirmedRequests", target = "confirmedRequests")
    EventShort toShortEvent(Event event, long confirmedRequests);

    List<EventShortDto> toShortDtos(List<Event> events);

    List<EventShortDto> toShortDtoList(List<EventShort> events);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEvent(UpdateEventUserRequest updateEvent, @MappingTarget Event event);

    default Event toFullEvent(NewEvent newEventDto, Category category, User initiator, EventState state, Location location) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .title(newEventDto.getTitle())
                .eventDate(newEventDto.getEventDate())
                .category(category)
                .state(state)
                .initiator(initiator)
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .location(location)
                .build();
    }
}
