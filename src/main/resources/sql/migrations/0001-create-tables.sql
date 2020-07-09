CREATE TABLE `punishments`
(
    `id`        BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `type`      TINYINT UNSIGNED NOT NULL,
    `target`    CHAR(36)         NOT NULL,
    -- UUID of (0, 0) if console:
    `punisher`  CHAR(36)         NOT NULL,
    `reason`    TINYTEXT                  DEFAULT NULL,
    `lifted`    BOOLEAN          NOT NULL DEFAULT FALSE,
    -- UUID of (0, 0) for console:
    `lifted_by` CHAR(36),
    `time`      BIGINT UNSIGNED  NOT NULL,
    -- 0 for permanent:
    `duration`  BIGINT UNSIGNED  NOT NULL,
    `silent`    BOOLEAN          NOT NULL DEFAULT FALSE,

    PRIMARY KEY (`id`),
    INDEX (`target`),
    INDEX (`punisher`)
);