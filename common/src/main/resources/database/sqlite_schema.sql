-- Create the users table if it does not exist
CREATE TABLE IF NOT EXISTS `%user_data%`
(
    `uuid`        char(36)    NOT NULL UNIQUE,
    `username`    varchar(16) NOT NULL,
    `preferences` longblob    NOT NULL,

    PRIMARY KEY (`uuid`)
);

-- Create the towns table if it does not exist
CREATE TABLE IF NOT EXISTS `%town_data%`
(
    `id`   integer     NOT NULL PRIMARY KEY AUTOINCREMENT,
    `name` varchar(16) NOT NULL,
    `data` longblob    NOT NULL
);

-- Create the claim worlds table if it does not exist
CREATE TABLE IF NOT EXISTS `%claim_data%`
(
    `id`                integer      NOT NULL PRIMARY KEY AUTOINCREMENT,
    `server_name`       varchar(255) NOT NULL,
    `world_uuid`        char(36)     NOT NULL,
    `world_name`        varchar(128) NOT NULL,
    `world_environment` varchar(32)  NOT NULL,
    `claims`            longblob     NOT NULL
);