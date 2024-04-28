package ru.practicum.yandex.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.user.dto.EventFullDto;
import ru.practicum.yandex.user.dto.EventShortDto;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.dto.UpdateEventUserRequest;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.EventState;
import ru.practicum.yandex.user.model.Location;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.*;

@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface EventMapper {

    NewEvent toModel(NewEventDto newEventDto);

    EventFullDto toDto(Event addedEvent);

    EventShortDto toShortDto(Event event);

    List<EventShortDto> toShortDtoList(List<Event> events);

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
