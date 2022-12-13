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
    `uuid`    char(36)     NOT NULL UNIQUE,
    `created` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `name`    varchar(32)  NOT NULL,
    `bio`     varchar(512) NOT NULL,
    `level`   int(11)      NOT NULL,
    `balance` double       NOT NULL,
    `rules`   mediumblob   NOT NULL,
    `spawn`   mediumblob   NOT NULL,
    `logo`    longblob     NOT NULL,

    PRIMARY KEY (`uuid`)
);

-- Create the members table if it does not exist
CREATE TABLE IF NOT EXISTS `%member_data%`
(
    `user_uuid` char(36) NOT NULL UNIQUE,
    `town_uuid` char(36) NOT NULL,
    `role`      int(11)  NOT NULL,

    PRIMARY KEY (`user_uuid`, `town_uuid`),
    FOREIGN KEY (`user_uuid`) REFERENCES `%user_data%` (`uuid`),
    FOREIGN KEY (`town_uuid`) REFERENCES `%town_data%` (`uuid`)
);

-- Create the claim worlds table if it does not exist
CREATE TABLE IF NOT EXISTS `%claim_worlds%`
(
    `id`                int(11)      NOT NULL,
    `server_name`       varchar(255) NOT NULL,
    `world_uuid`        char(36)     NOT NULL,
    `world_name`        varchar(128) NOT NULL,
    `world_environment` varchar(16)  NOT NULL,
    `claims`            longblob     NOT NULL,

    PRIMARY KEY (`id`)
);