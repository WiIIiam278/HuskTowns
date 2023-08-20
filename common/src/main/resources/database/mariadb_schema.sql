-- Set the storage engine
SET DEFAULT_STORAGE_ENGINE = InnoDB;

-- Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

-- Create the metadata table if it does not exist
CREATE TABLE IF NOT EXISTS `%meta_data%`
(
    `schema_version` int NOT NULL PRIMARY KEY
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Create the users table if it does not exist
CREATE TABLE IF NOT EXISTS `%user_data%`
(
    `uuid`        char(36)    NOT NULL UNIQUE PRIMARY KEY,
    `username`    varchar(16) NOT NULL,
    `last_login`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `preferences` longblob    NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
CREATE INDEX IF NOT EXISTS `%user_data%_username` ON `%user_data%` (`username`);

-- Create the towns table if it does not exist
CREATE TABLE IF NOT EXISTS `%town_data%`
(
    `id`   int         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` varchar(16) NOT NULL,
    `data` longblob    NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
CREATE INDEX IF NOT EXISTS `%town_data%_name` ON `%town_data%` (`name`);

-- Create the claim worlds table if it does not exist
CREATE TABLE IF NOT EXISTS `%claim_data%`
(
    `id`                int          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `server_name`       varchar(255) NOT NULL,
    `world_uuid`        char(36)     NOT NULL,
    `world_name`        varchar(128) NOT NULL,
    `world_environment` varchar(32)  NOT NULL,
    `claims`            longblob     NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;