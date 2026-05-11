package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM genre ORDER BY id",
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Optional<Genre> getById(int id) {
        List<Genre> genres = jdbcTemplate.query("SELECT id, name FROM genre WHERE id = ?",
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), id);
        return genres.stream().findFirst();
    }
}
