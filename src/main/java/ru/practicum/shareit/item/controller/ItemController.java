package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsOfUser(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items. Получен GET запрос по  от пользователя c id {} на получение всех своих вещей.",
                userId);
        return new ResponseEntity<>(itemService.getItemsOfUserById(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long id,
                                           @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items/{}. Получен GET запрос от пользователя c id {} на получение вещи с id {}.",
                id, userId);

        return new ResponseEntity<>(itemService.getItemById(id, userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody ItemDto itemDto,
                                              @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items. Получен POST запрос от пользователя c id {} на добавление новой вещи {}.",
                userId, itemDto);

        return new ResponseEntity<>(itemService.createNewItem(itemDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemDto itemDto,
                                        @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items/{}. Получен PATCH запрос  от пользователя c id {} на обновление" +
                " обновление вещи с id {}.", id, userId, itemDto);

        return new ResponseEntity<>(itemService.updateItemOfUserById(id, itemDto, userId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id,
                                        @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items/{}. Получен DELETE запрос  от пользователя c id {} на удаление вещи с id {}.",
                id, userId, id);
        itemService.deleteItemOfUserById(id, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam(name = "text") String text,
                                                     @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items/search. Получен GET запрос от пользователя c id {} на получение списка вещей" +
                " по запросу '{}'.", userId, text);
        return new ResponseEntity<>(itemService.findItemsOfUser(text, userId), HttpStatus.OK);
    }
}
