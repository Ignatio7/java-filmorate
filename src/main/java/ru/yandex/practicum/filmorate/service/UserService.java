package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        setDefaultName(user);
        User created = userStorage.create(user);
        log.info("Создан пользователь id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    public User update(User user) {
        User existing = findUserOrThrow(user.getId());
        setDefaultName(user);
        user.setFriends(existing.getFriends());
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
        User user = findUserOrThrow(id);
        User friend = findUserOrThrow(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);
        log.info("Пользователи id={} и id={} стали друзьями", id, friendId);
    }

    public void removeFriend(int id, int friendId) {
        User user = findUserOrThrow(id);
        User friend = findUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
        log.info("Пользователи id={} и id={} удалены из друзей", id, friendId);
    }

    public List<User> getFriends(int id) {
        User user = findUserOrThrow(id);
        return user.getFriends().stream()
                .map(this::findUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        User user = findUserOrThrow(id);
        User otherUser = findUserOrThrow(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::findUserOrThrow)
                .collect(Collectors.toList());
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
