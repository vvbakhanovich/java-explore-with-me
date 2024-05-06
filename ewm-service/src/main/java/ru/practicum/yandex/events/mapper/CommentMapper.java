package ru.practicum.yandex.events.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.events.dto.CommentDto;
import ru.practicum.yandex.events.dto.CommentRequestDto;
import ru.practicum.yandex.events.model.Comment;
import ru.practicum.yandex.events.model.CommentRequest;
import ru.practicum.yandex.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {

    CommentDto toDto(Comment comment);

    CommentRequest toRequestModel(CommentRequestDto commentDto);

    Comment toModel(CommentRequestDto commentRequestDto);

    List<CommentRequestDto> toDtoList(List<Comment> comments);
}
