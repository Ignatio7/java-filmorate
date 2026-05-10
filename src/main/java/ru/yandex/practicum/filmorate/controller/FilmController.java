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
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        film.setId(idGenerator.getAndIncrement());
        films.put(film.getId(), film);
        log.info("Добавлен фильм id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.warn("Не удалось обновить фильм: id={} не найден", film.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновлен фильм id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }
}
