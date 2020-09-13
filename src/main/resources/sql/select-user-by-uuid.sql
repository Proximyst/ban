SELECT *
FROM `users`
WHERE `uuid` = ?
ORDER BY `timestamp` DESC
LIMIT 1;