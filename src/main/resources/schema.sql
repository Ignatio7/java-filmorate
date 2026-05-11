CREATE TABLE IF NOT EXISTS mpa_rating (
    id   INT PRIMARY KEY,
    name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
    id   INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL,
    login    VARCHAR(255) NOT NULL,
    name     VARCHAR(255),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS film (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(200),
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL,
    mpa_id       INT REFERENCES mpa_rating (id)
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id  INT REFERENCES film (id) ON DELETE CASCADE,
    genre_id INT REFERENCES genre (id),
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS film_like (
    film_id INT REFERENCES film (id) ON DELETE CASCADE,
    user_id INT REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS friendship (
    user_id   INT REFERENCES users (id) ON DELETE CASCADE,
    friend_id INT REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);
