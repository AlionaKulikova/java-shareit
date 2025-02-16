package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final UserStorage userStorage;
    private Long id = 1L;

    @Autowired
    public InMemoryItemStorage(UserStorage userRepository) {
        this.userStorage = userRepository;
    }

    @Override
    public List<Item> getAllItemsOfUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId не может быть null");
        }
        List<Item> list = items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
        log.info("Получили список вещей пользователя c id {} .", userId);

        return list;
    }

    @Override
    public Item getItem(Long id, Long userId) {
        if (items.containsKey(id)) {
            userStorage.сheckingExistenceOfUser(userId);
            Item item = items.get(id);
            log.info("Получена вещь c id {} пользователя c id {} .", id, userId);

            return item;
        } else {
            log.error("Вещь c id " + id + " не найдена.");
            throw new NotFoundException("Вещь c id " + id + " не найдена");
        }
    }

    @Override
    public Item saveNewItem(Item item, Long userId) {
        if (item.getAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь не доступна для заказа.");
        }

        if (item.getName() == null || item.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя не заполненно.");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет описания.");
        }

        User user = userStorage.findUser(userId);
        if (user == null) {
            throw new NotFoundException("Владелец вещи не найден.");
        }
        item.setId(id);
        item.setOwner(user);
        items.put(id++, item);
        log.info("Пользователь c id {} создал вещь c id {}.", userId, item.getId());

        return item;
    }

    @Override
    public Item updateItemOfUser(Long itemId, Item item, Long userId) {
        if (items.containsKey(itemId)) {
            Item oldItem = items.get(itemId);
            if (items.containsKey(itemId) && items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
                if (item.getName() != null && !item.getName().isBlank()) {
                    oldItem.setName(item.getName());
                }
                if (item.getDescription() != null && !item.getDescription().isBlank()) {
                    oldItem.setDescription(item.getDescription());
                }
                if (item.getAvailable() != null) {
                    oldItem.setAvailable(item.getAvailable());
                }
                log.info("Обновлена вещь c id {} у пользователя c id {}.", item.getId(), userId);
            } else if (!items.containsKey(itemId) &&
                    items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
                log.info("Вещь с id {} не найдена.", itemId);
                throw new NotFoundException("Вещь не найдена.");
            } else {
                log.info("Пользователь с id {} не является владельцем обновляемой вещи.", userId);
                throw new ResponseStatusException(HttpStatus.valueOf(404), "Пользователь" +
                        " не является владельцем обновляемой вещи.");
            }

            return oldItem;
        } else {
            log.info("Вещь с id {} не найдена.", itemId);
            throw new NotFoundException("Вещь с id " + itemId + " не найдена.");
        }
    }

    @Override
    public void deleteItemOfUser(Long itemId, Long userId) {
        if (items.containsKey(itemId) && items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
            items.remove(itemId);
            log.info("Вещь с id {} удалена у пользователя с id {}.", itemId, userId);
        } else if (!items.containsKey(itemId) && items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
            log.info("Не найдена вещь с id {}.", itemId);
            throw new NotFoundException("Вещь не найдена.");
        } else {
            log.info("Пользователь с id {} не является владельцем удаляемой вещи.", userId);
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Пользователь не является" +
                    " владельцем удаляемой вещи.");
        }
    }

    @Override
    public List<Item> findItems(String text, Long userId) {
        userStorage.сheckingExistenceOfUser(userId);
        if (text == null || text.isBlank()) {
            log.info("Ничего не нашли по запросу пользователя с id {}.", userId);
            return List.of();
        }
        List<Item> itemsList = items.values().stream()
                .filter(a -> (a.getDescription().toLowerCase().contains(text.toLowerCase()) || a.getName().toLowerCase()
                        .contains(text.toLowerCase())) && a.getAvailable())
                .collect(Collectors.toList());
        log.info("Получены результаты поиска по запросу {} пользователя с id {}.", text, userId);

        return itemsList;
    }
}