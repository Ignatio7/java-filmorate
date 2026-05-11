package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa_rating ORDER BY id",
                (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Optional<Mpa> getById(int id) {
        List<Mpa> mpaList = jdbcTemplate.query("SELECT id, name FROM mpa_rating WHERE id = ?",
                (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name")), id);
        return mpaList.stream().findFirst();
    }
}
