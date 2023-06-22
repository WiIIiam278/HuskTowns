-- Disable FK checks and start transaction
PRAGMA foreign_keys= off;
BEGIN TRANSACTION;

-- Rename old table and make a new one with correct columns
ALTER TABLE `%user_data%`
    RENAME TO `%user_data%_temp`;
CREATE TABLE IF NOT EXISTS `%user_data%`
(
    `uuid`        char(36)    NOT NULL UNIQUE,
    `username`    varchar(16) NOT NULL,
    `last_login`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `preferences` longblob    NOT NULL,

    PRIMARY KEY (`uuid`)
);

-- Copy data from old table to new one
INSERT INTO `%user_data%`(uuid, username, preferences)
SELECT `uuid`, `username`, `preferences`
FROM `%user_data%_temp`;

-- Drop old table
DROP TABLE `%user_data%_temp`;

-- Re-enable FK checks and commit
COMMIT TRANSACTION;
PRAGMA foreign_keys= on;