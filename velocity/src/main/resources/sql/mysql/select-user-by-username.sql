SELECT *
FROM `users`
WHERE `username` = ?
ORDER BY `timestamp` DESC
LIMIT 1;