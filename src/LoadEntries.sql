LOAD DATA LOCAL INFILE '~/projects/cs122b-spring21-team-85/cs122b-spring21-project3-movie-parser/movie_insert_info.txt' INTO TABLE movies FIELDS TERMINATED BY '\t' ENCLOSED BY '"' LINES TERMINATED BY '\n';

LOAD DATA LOCAL INFILE '~/projects/cs122b-spring21-team-85/cs122b-spring21-project3-movie-parser/genre_insert_info.txt' INTO TABLE genres FIELDS TERMINATED BY '\t' ENCLOSED BY '"' LINES TERMINATED BY '\n';

LOAD DATA LOCAL INFILE '~/projects/cs122b-spring21-team-85/cs122b-spring21-project3-movie-parser/genre_in_movies_insert_info.txt' INTO TABLE genres_in_movies FIELDS TERMINATED BY '\t' ENCLOSED BY '"' LINES TERMINATED BY '\n';

LOAD DATA LOCAL INFILE '~/projects/cs122b-spring21-team-85/cs122b-spring21-project3-movie-parser/star_insert_info.txt' INTO TABLE stars FIELDS TERMINATED BY '\t' ENCLOSED BY '"' LINES TERMINATED BY '\n';

LOAD DATA LOCAL INFILE '~/projects/cs122b-spring21-team-85/cs122b-spring21-project3-movie-parser/cast_insert_info.txt' INTO TABLE stars FIELDS TERMINATED BY '\t' ENCLOSED BY '"' LINES TERMINATED BY '\n';

LOAD DATA LOCAL INFILE '~/projects/cs122b-spring21-team-85/cs122b-spring21-project3-movie-parser/star_in_movies_insert_info.txt' INTO TABLE stars_in_movies FIELDS TERMINATED BY '\t' ENCLOSED BY '"' LINES TERMINATED BY '\n';

UPDATE stars SET birthYear = NULL WHERE birthYear = 0;