package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        Film created = filmStorage.create(film);
        log.info("Добавлен фильм id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        findFilmOrThrow(film.getId());
        Film updated = filmStorage.update(film);
        log.info("Обновлен фильм id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    public Film getById(int id) {
        return findFilmOrThrow(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        findFilmOrThrow(filmId);
        ensureUserExists(userId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        findFilmOrThrow(filmId);
        ensureUserExists(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь id={} удалил лайк у фильма id={}", userId, filmId);
    }

    public List<Film> getPopular(Integer count) {
        int limit = count == null ? 10 : count;
        if (limit <= 0) {
            throw new ValidationException("Параметр count должен быть положительным");
        }
        return filmStorage.getPopular(limit);
    }

    private Film findFilmOrThrow(Integer id) {
        if (id == null) {
            throw new NotFoundException("Фильм не найден");
        }
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id=%d не найден", id)));
    }

    private void ensureUserExists(int userId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
    }
}
