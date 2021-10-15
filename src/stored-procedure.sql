use moviedb;
DROP FUNCTION IF EXISTS increment_starId;

DELIMITER $$

CREATE FUNCTION increment_starId()
    RETURNS VARCHAR(10)
    DETERMINISTIC
BEGIN
	DECLARE maxId VARCHAR(10);
    DECLARE newInt INT;
SELECT max(id) INTO maxId FROM stars;
SET newInt = RIGHT(maxId, 7) + 1;
RETURN CONCAT("nm", newInt);
END
$$

DELIMITER ;


DROP FUNCTION IF EXISTS increment_movieId;

DELIMITER $$

CREATE FUNCTION increment_movieId()
    RETURNS VARCHAR(10)
    DETERMINISTIC
BEGIN
	DECLARE maxId VARCHAR(10);
    DECLARE newInt INT;
SELECT max(id) INTO maxId FROM movies;
SET newInt = RIGHT(maxId, 6) + 1;
RETURN CONCAT("tt", newInt);

END
$$

DELIMITER ;


DROP FUNCTION IF EXISTS increment_genreId;

DELIMITER $$

CREATE FUNCTION increment_genreId()
    RETURNS INT
    DETERMINISTIC
BEGIN
	DECLARE maxId INT;
    DECLARE newInt INT;
SELECT max(id) INTO maxId FROM genres;
SET newInt = maxId + 1;
RETURN newInt;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS add_star;

DELIMITER $$

CREATE PROCEDURE add_star(IN s_name VARCHAR(100), IN s_year INT)
BEGIN
    DECLARE star_id VARCHAR(10);
SELECT increment_starId() INTO star_id;
INSERT 	stars(id, name, birthYear) VALUES (star_id, s_name, s_year);
SELECT CONCAT("Star added: Star ID (", star_id, ").") as message;
END

$$
DELIMITER ;





DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie(IN m_title VARCHAR(100), IN m_year INT, IN m_director VARCHAR(100), IN m_star VARCHAR(100), IN m_genre VARCHAR(32))
BEGIN
	DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;

SELECT M.id INTO movie_id FROM movies M WHERE M.title = m_title and M.director = m_director and M.year = m_year;
SELECT S.id INTO star_id FROM stars S WHERE m_star = S.name;
SELECT G.id INTO genre_id FROM genres G WHERE m_genre = G.name;

-- MOVIE DOES NOT EXIST
IF (movie_id IS NULL) THEN
SELECT increment_movieId() INTO movie_id;
INSERT INTO movies(id, title, year, director) VALUES (movie_id, m_title, m_year, m_director);

-- STAR DOES NOT EXIST IN DATABASE
IF (star_id IS NULL) THEN
SELECT increment_starId() INTO star_id;
INSERT INTO stars(id, name, birthYear) VALUES (star_id, m_star, NULL);
INSERT INTO stars_in_movies(starId, movieId) VALUES (star_id, movie_id);

-- STAR EXISTS IN DATABASE
ELSE
			-- STAR WAS NOT CREDITED IN THE MOVIE
			IF ((SELECT SIM.starId FROM stars_in_movies SIM WHERE SIM.starId = star_id and SIM.movieId = movie_id) IS NULL) THEN
				INSERT INTO stars_in_movies(starId, movieId) VALUES (star_id, movie_id);

			-- STAR WAS ALREADY IN THE MOVIE
ELSE
SELECT "ALREADY IN THE MOVIE" as addedstar;
END IF;
			-- SELECT "EXISTS" as star;
END IF;


		-- GENRE DOES NOT EXIST
		IF (genre_id IS NULL) THEN
SELECT increment_genreId() INTO genre_id;
INSERT INTO genres(id, name) VALUES (genre_id, m_genre);
INSERT INTO genres_in_movies(genreId, movieId) VALUES (genre_id, movie_id);
ELSE
			IF ((SELECT GIM.genreId FROM genres_in_movies GIM WHERE GIM.genreId = genre_id and GIM.movieId = movie_id) IS NULL) THEN
				INSERT INTO genres_in_movies(genreId, movieId) VALUES (genre_id, movie_id);

			-- STAR WAS ALREADY IN THE MOVIE
ELSE
SELECT "ALREADY CLASSIFIED WITH THIS GENRE" as addedgenre;
END IF;
END IF;
SELECT CONCAT("Movie added: Movie ID (", movie_id, "), Star ID (", star_id ,"), Genre ID (", genre_id, ").") as message;
-- MOVIE EXISTS
ELSE
SELECT "Movie already exists." as message;
END IF;

END

$$
DELIMITER ;




