-- Create the users table if it does not exist
CREATE TABLE IF NOT EXISTS `%user_data%`
(
    `uuid`     char(36)    NOT NULL UNIQUE,
    `username` varchar(16) NOT NULL,

    PRIMARY KEY (`uuid`)
);

-- Create the towns table if it does not exist
CREATE TABLE IF NOT EXISTS `%town_data%`
(
    `id`   int         NOT NULL,
    `name` varchar(16) NOT NULL,
    `data` longblob    NOT NULL,

    PRIMARY KEY (`id`)
);

-- Create the claim worlds table if it does not exist
CREATE TABLE IF NOT EXISTS `%claim_data%`
(
    `id`                int          NOT NULL,
    `server_name`       varchar(255) NOT NULL,
    `world_uuid`        char(36)     NOT NULL,
    `world_name`        varchar(128) NOT NULL,
    `world_environment` varchar(16)  NOT NULL,
    `claims`            longblob     NOT NULL,

    PRIMARY KEY (`id`)
);