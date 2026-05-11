package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public User create(User user) {
        user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        log.debug("User stored: id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.debug("User updated in storage: id={}", user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findUserById(int id) {
        return getById(id);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(int id) {
        users.remove(id);
        friends.remove(id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (friends.containsKey(userId)) {
            friends.get(userId).remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        return friends.getOrDefault(userId, new HashSet<>()).stream()
                .map(users::get)
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriends = friends.getOrDefault(userId, new HashSet<>());
        Set<Integer> otherFriends = friends.getOrDefault(otherId, new HashSet<>());
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }
}
