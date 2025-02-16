package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAllUsers();

    User findUser(Long id);

    User saveUser(User user);

    User updateUser(Long id, User newUser);

    void deleteUser(Long id);

    public void сheckingExistenceOfUser(Long id);
}
