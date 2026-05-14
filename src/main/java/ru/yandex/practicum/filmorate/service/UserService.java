package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        setDefaultName(user);
        User created = userStorage.create(user);
        log.info("Создан пользователь id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    public User update(User user) {
        findUserOrThrow(user.getId());
        setDefaultName(user);
        User updated = userStorage.update(user);
        log.info("Обновлен пользователь id={}, login={}", updated.getId(), updated.getLogin());
        return updated;
    }

    public User getById(int id) {
        return findUserOrThrow(id);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int id, int friendId) {
        findUserOrThrow(id);
        findUserOrThrow(friendId);
        userStorage.addFriend(id, friendId);
        log.info("Пользователь id={} добавил в друзья id={}", id, friendId);
    }

    public void removeFriend(int id, int friendId) {
        findUserOrThrow(id);
        findUserOrThrow(friendId);
        userStorage.removeFriend(id, friendId);
        log.info("Пользователь id={} удалил из друзей id={}", id, friendId);
    }

    public List<User> getFriends(int id) {
        findUserOrThrow(id);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        findUserOrThrow(id);
        findUserOrThrow(otherId);
        return userStorage.getCommonFriends(id, otherId);
    }

    private User findUserOrThrow(Integer id) {
        if (id == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
