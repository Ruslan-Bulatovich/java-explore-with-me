package ru.practicum.main.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.models.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface CommentMapper {

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "event.id", target = "eventId")
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDtos(List<Comment> comment);

}
