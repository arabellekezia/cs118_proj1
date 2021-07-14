-- make sure that we are using the right database
USE CS144;

-- create table if it does not exist already 
CREATE TABLE IF NOT EXISTS Actors(name VARCHAR(40), movie VARCHAR(80), year INTEGER, role VARCHAR(40));

-- load data from actors.csv
LOAD DATA LOCAL INFILE './actors.csv' INTO TABLE Actors FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"';

-- execute query
SELECT name FROM Actors WHERE movie='Die Another Day';

-- DROP TABLE Actors;

