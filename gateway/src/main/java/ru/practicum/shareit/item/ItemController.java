package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @GetMapping
    public ResponseEntity<Object> getItemsOfUser(
            @RequestParam(defaultValue = "0", required = false) @PositiveOrZero int from,
            @RequestParam(defaultValue = "10", required = false) @Positive int size,
            @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items. Получен GET запрос по  от пользователя c id {} на получение всех своих вещей.",
                userId);
        return itemClient.getItemsOfUserById(from, size, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable Long id,
                                                   @RequestHeader(value = USER_ID_HEADER) Long userId) {
        log.info("Эндпоинт /items/{}. Получен GET запрос от пользователя c id {} на получение вещи с id {}.",
                id, userId);

        return itemClient.getItemById(id, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto,
                                              @RequestHeader(value = USER_ID_HEADER) Long userId) {
        log.info("Эндпоинт /items. Получен POST запрос от пользователя c id {} на добавление новой вещи {}.",
                userId, itemDto);

        return itemClient.createNewItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemDto itemDto,
                                        @RequestHeader(value = USER_ID_HEADER) Long userId) {
        log.info("Эндпоинт /items/{}. Получен PATCH запрос  от пользователя c id {} на обновление" +
                " обновление вещи  {}.", id, userId, itemDto);

        return itemClient.updateItemOfUserById(id, itemDto, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id,
                                        @RequestHeader(value = USER_ID_HEADER) Long userId) {
        log.info("Эндпоинт /items/{}. Получен DELETE запрос  от пользователя c id {} на удаление вещи с id {}.",
                id, userId, id);
        itemClient.deleteItemOfUserById(id, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
        public ResponseEntity<Object> searchItem(
        @RequestParam(defaultValue = "0", required = false) @PositiveOrZero int from,
        @RequestParam(defaultValue = "10", required = false) @Positive int size,
        @RequestParam(name = "text") String text,
        @RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        log.info("Эндпоинт /items/search. Получен GET запрос от пользователя c id {} на получение списка вещей" +
                " по запросу '{}'.", userId, text);
            return itemClient.findItemsOfUser(from, size, text, userId);
    }
}