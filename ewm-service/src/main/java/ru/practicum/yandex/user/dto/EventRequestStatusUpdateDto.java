package ru.practicum.yandex.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequestStatusUpdateDto {

    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;

    public void addConfirmedRequest(ParticipationRequestDto requestDto) {
        confirmedRequests.add(requestDto);
    }

    public void addRejectedRequest(ParticipationRequestDto requestDto) {
        rejectedRequests.add(requestDto);
    }
}
