package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldFailWhenEmailBlank() {
        User user = validUser();
        user.setEmail(" ");

        assertThat(validator.validate(user)).isNotEmpty();
    }

    @Test
    void shouldFailWhenEmailInvalid() {
        User user = validUser();
        user.setEmail("invalid");

        assertThat(validator.validate(user)).isNotEmpty();
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = validUser();
        user.setLogin("ivan petrov");

        assertThat(validator.validate(user)).isNotEmpty();
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThat(validator.validate(user)).isNotEmpty();
    }

    @Test
    void shouldPassWhenBirthdayToday() {
        User user = validUser();
        user.setBirthday(LocalDate.now());

        assertThat(validator.validate(user)).isEmpty();
    }

    private User validUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test_login");
        user.setName("Test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
