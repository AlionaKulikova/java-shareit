package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(@RequestBody ItemRequestDto itemRequestDto,
                                                 @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /requests.POST запрос на добавление нового ItemRequest {} от пользователя с id {}.",
                itemRequestDto, userId);
        return new ResponseEntity<>(itemRequestService.createRequest(itemRequestDto, userId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestResponseDto>> getAllForRequestor(
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /requests. GET запрос  на получение всех ItemRequest с" +
                " данными об ответах на них для пользователя с id {}.", userId);
        return new ResponseEntity<>(itemRequestService.getAllForRequestor(userId), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestResponseDto>> getAll(
            @RequestParam(defaultValue = "0", required = false) int from,
            @RequestParam(defaultValue = "20", required = false) int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("эндпоинту /requests/all. GET запрос по  на получение всех ItemRequest для пользователя с id {}.",
                userId);
        return new ResponseEntity<>(itemRequestService.getAllRequests(from, size, userId),
                HttpStatus.OK);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseDto> getById(@PathVariable long requestId,
                                                          @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /requests/{}. GET запрос на получение ItemRequest c id {} для пользователя с id {}.",
                requestId, requestId, userId);
        return new ResponseEntity<>(itemRequestService.getById(requestId, userId), HttpStatus.OK);
    }
}