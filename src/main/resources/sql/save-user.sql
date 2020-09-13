REPLACE INTO `users`
(`uuid`,
 `username`,
 `timestamp`)
VALUES (?, ?, NOW());