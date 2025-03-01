package ru.practicum.shareit.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.service.CommentService;

@Slf4j
@RestController
@RequestMapping("/items")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable("itemId") long itemId,
                                                         @RequestHeader(value = "X-Sharer-User-Id") Long userId,
                                                         @RequestBody CommentDto commentDto) {
        log.info("Эдпонинт /items/{}/comment. Получен POST запрос  от пользователя c id {} на создание" +
                        " комментария с id {}.", itemId,
                userId, commentDto);

        return new ResponseEntity<>(commentService.addComment(commentDto, itemId, userId), HttpStatus.OK);
    }
}
