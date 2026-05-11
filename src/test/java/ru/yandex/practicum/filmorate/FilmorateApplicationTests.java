package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    private User testUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser = userStorage.create(testUser);

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new Mpa(1, "G"));
        testFilm = filmStorage.create(testFilm);
    }

    // --- UserDbStorage tests ---

    @Test
    void testFindUserById() {
        Optional<User> userOptional = userStorage.findUserById(testUser.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", testUser.getId())
                );
    }

    @Test
    void testCreateUser() {
        assertThat(testUser.getId()).isNotNull();
        assertThat(testUser.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void testUpdateUser() {
        testUser.setName("Updated Name");
        userStorage.update(testUser);
        Optional<User> updated = userStorage.findUserById(testUser.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void testGetAllUsers() {
        Collection<User> users = userStorage.getAll();
        assertThat(users).isNotEmpty();
    }

    @Test
    void testAddAndGetFriends() {
        User friend = new User();
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(1995, 5, 5));
        friend = userStorage.create(friend);

        userStorage.addFriend(testUser.getId(), friend.getId());
        List<User> friends = userStorage.getFriends(testUser.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(friend.getId());
    }

    @Test
    void testRemoveFriend() {
        User friend = new User();
        friend.setEmail("friend2@test.com");
        friend.setLogin("friend2");
        friend.setName("Friend2");
        friend.setBirthday(LocalDate.of(1995, 5, 5));
        friend = userStorage.create(friend);

        userStorage.addFriend(testUser.getId(), friend.getId());
        userStorage.removeFriend(testUser.getId(), friend.getId());
        List<User> friends = userStorage.getFriends(testUser.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User2");
        user2.setBirthday(LocalDate.of(1992, 3, 3));
        user2 = userStorage.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@test.com");
        commonFriend.setLogin("common");
        commonFriend.setName("Common");
        commonFriend.setBirthday(LocalDate.of(1993, 4, 4));
        commonFriend = userStorage.create(commonFriend);

        userStorage.addFriend(testUser.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> common = userStorage.getCommonFriends(testUser.getId(), user2.getId());
        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(commonFriend.getId());
    }

    // --- FilmDbStorage tests ---

    @Test
    void testCreateFilm() {
        assertThat(testFilm.getId()).isNotNull();
        assertThat(testFilm.getName()).isEqualTo("Test Film");
        assertThat(testFilm.getMpa()).isNotNull();
        assertThat(testFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void testGetFilmById() {
        Optional<Film> filmOptional = filmStorage.getById(testFilm.getId());
        assertThat(filmOptional).isPresent();
        assertThat(filmOptional.get().getName()).isEqualTo("Test Film");
    }

    @Test
    void testUpdateFilm() {
        testFilm.setName("Updated Film");
        filmStorage.update(testFilm);
        Optional<Film> updated = filmStorage.getById(testFilm.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Film");
    }

    @Test
    void testGetAllFilms() {
        Collection<Film> films = filmStorage.getAll();
        assertThat(films).isNotEmpty();
    }

    @Test
    void testAddAndRemoveLike() {
        filmStorage.addLike(testFilm.getId(), testUser.getId());
        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();

        filmStorage.removeLike(testFilm.getId(), testUser.getId());
    }

    @Test
    void testGetPopular() {
        filmStorage.addLike(testFilm.getId(), testUser.getId());
        List<Film> popular = filmStorage.getPopular(1);
        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(testFilm.getId());
    }

    @Test
    void testFilmWithGenres() {
        Film filmWithGenre = new Film();
        filmWithGenre.setName("Genre Film");
        filmWithGenre.setDescription("Desc");
        filmWithGenre.setReleaseDate(LocalDate.of(2005, 6, 15));
        filmWithGenre.setDuration(90);
        filmWithGenre.setMpa(new Mpa(2, "PG"));
        filmWithGenre.setGenres(List.of(new Genre(1, "Комедия"), new Genre(2, "Драма")));

        Film created = filmStorage.create(filmWithGenre);
        Optional<Film> found = filmStorage.getById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(2);
    }
}
