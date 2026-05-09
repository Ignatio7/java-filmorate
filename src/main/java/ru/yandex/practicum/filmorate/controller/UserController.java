package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        setDefaultName(user);
        user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        log.info("Создан пользователь id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.warn("Не удалось обновить пользователя: id={} не найден", user.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }
        setDefaultName(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
