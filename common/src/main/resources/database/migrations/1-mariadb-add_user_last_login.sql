# Add last_login column to user table
ALTER TABLE `%user_data%`
    ADD COLUMN `last_login` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `username`;