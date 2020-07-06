CREATE TABLE IF NOT EXISTS `meta_version`
(
    `version` INT NOT NULL,

    PRIMARY KEY (`version`)
);
INSERT IGNORE INTO `meta_version` (`version`) VALUES (0);
