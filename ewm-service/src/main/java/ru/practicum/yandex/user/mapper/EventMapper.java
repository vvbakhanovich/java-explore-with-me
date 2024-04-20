package ru.practicum.yandex.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.category.model.Category;
import ru.practicum.yandex.user.dto.EventFullDto;
import ru.practicum.yandex.user.dto.EventShortDto;
import ru.practicum.yandex.user.dto.NewEventDto;
import ru.practicum.yandex.user.model.Event;
import ru.practicum.yandex.user.model.NewEvent;
import ru.practicum.yandex.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    NewEvent toModel(NewEventDto newEventDto);

    EventFullDto toDto(Event addedEvent);

    EventShortDto toShortDto(Event event);

    List<EventShortDto> toShortDtoList(List<Event> events);

    default Event toFullEvent(NewEvent newEventDto, Category category, User initiator) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .title(newEventDto.getTitle())
                .eventDate(newEventDto.getEventDate())
                .category(category)
                .initiator(initiator)
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .build();
    }
}
