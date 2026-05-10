package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FilmValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = validFilm();
        film.setName(" ");

        assertThat(validator.validate(film)).isNotEmpty();
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));

        assertThat(validator.validate(film)).isNotEmpty();
    }

    @Test
    void shouldFailWhenReleaseDateBeforeAllowed() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThat(validator.validate(film)).isNotEmpty();
    }

    @Test
    void shouldFailWhenDurationNotPositive() {
        Film film = validFilm();
        film.setDuration(0);

        assertThat(validator.validate(film)).isNotEmpty();
    }

    @Test
    void shouldPassOnBoundaryValues() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(1);

        assertThat(validator.validate(film)).isEmpty();
    }

    private Film validFilm() {
        Film film = new Film();
        film.setName("Interstellar");
        film.setDescription("Space movie");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        return film;
    }
}
