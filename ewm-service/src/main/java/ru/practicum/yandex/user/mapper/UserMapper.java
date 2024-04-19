package ru.practicum.yandex.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.user.dto.NewUserRequest;
import ru.practicum.yandex.user.dto.UserDto;
import ru.practicum.yandex.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserDto userDto);

    User toModel(NewUserRequest userShortDto);

    NewUserRequest toShortDto(User user);

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> userList);
}
