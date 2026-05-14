package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    Integer id;

    @NotBlank(message = "Название не может быть пустым")
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;

    @NotNull(message = "Дата релиза обязательна")
    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    int duration;

    Mpa mpa;

    List<Genre> genres = new ArrayList<>();

    @AssertTrue(message = "Дата релиза — не раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return releaseDate != null && !releaseDate.isBefore(CINEMA_BIRTHDAY);
    }
}
