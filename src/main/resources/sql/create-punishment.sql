INSERT INTO `punishments`
(`id`,
 `type`,
 `target`,
 `punisher`,
 `reason`,
 `lifted`,
 `lifted_by`,
 `time`,
 `duration`,
 `silent`)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);