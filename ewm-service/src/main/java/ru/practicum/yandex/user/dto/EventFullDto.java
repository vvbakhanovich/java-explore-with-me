package ru.practicum.yandex.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.yandex.category.dto.CategoryDto;
import ru.practicum.yandex.user.model.EventState;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFullDto {

    private Long id;

    private String annotation;

    private CategoryDto category;

    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Boolean paid;

    private String description;

    private String title;

    private Long confirmedRequests;

    private Long views;

    private int participantLimit;

    private boolean requestModeration;

    private EventState state;

    private LocalDateTime createdOn;

    private LocalDateTime publishedOn;
}
