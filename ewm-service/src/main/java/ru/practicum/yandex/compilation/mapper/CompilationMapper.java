package ru.practicum.yandex.compilation.mapper;

import org.mapstruct.Mapper;
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.events.mapper.EventMapper;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    CompilationDto toDto(Compilation compilation);
}
