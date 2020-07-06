CREATE TABLE IF NOT EXISTS `usernames`
(
    `uuid`     CHAR(36)    NOT NULL,
    `username` VARCHAR(16) NOT NULL,

    PRIMARY KEY (`uuid`)
);