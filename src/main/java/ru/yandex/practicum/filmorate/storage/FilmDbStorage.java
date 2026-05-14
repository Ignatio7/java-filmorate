package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        validateMpaAndGenres(film);
        String sql = "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());
        saveGenres(film);
        log.debug("Film stored in DB: id={}", film.getId());
        return getById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        validateMpaAndGenres(film);
        String sql = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        saveGenres(film);
        log.debug("Film updated in DB: id={}", film.getId());
        return getById(film.getId()).orElse(film);
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM film f " +
                "LEFT JOIN mpa_rating m ON f.mpa_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
            }
            return film;
        }, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        film.setGenres(loadGenres(film.getId()));
        return Optional.of(film);
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM film f LEFT JOIN mpa_rating m ON f.mpa_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
            }
            return film;
        });
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    @Override
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM film WHERE id = ?", id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "MERGE INTO film_like (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Like added: filmId={}, userId={}", filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update("DELETE FROM film_like WHERE film_id = ? AND user_id = ?", filmId, userId);
        log.debug("Like removed: filmId={}, userId={}", filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM film f " +
                "LEFT JOIN mpa_rating m ON f.mpa_id = m.id " +
                "LEFT JOIN film_like fl ON f.id = fl.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
            }
            return film;
        }, count);
        films.forEach(f -> f.setGenres(loadGenres(f.getId())));
        return films;
    }

    private List<Genre> loadGenres(int filmId) {
        String sql = "SELECT g.id, g.name FROM genre g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "MERGE INTO film_genre (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM mpa_rating WHERE id = ?", Integer.class, film.getMpa().getId());
            if (count == null || count == 0) {
                throw new NotFoundException("Рейтинг MPA с id=" + film.getMpa().getId() + " не найден");
            }
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM genre WHERE id = ?", Integer.class, genre.getId());
                if (count == null || count == 0) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
    }
}
