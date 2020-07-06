CREATE TABLE IF NOT EXISTS `meta_version`
(
    `key` TINYINT(1) NOT NULL DEFAULT 0,
    `version` INT NOT NULL,

    PRIMARY KEY (`key`)
);
INSERT IGNORE INTO `meta_version` (`version`) VALUES (0);
