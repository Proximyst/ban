SELECT *
FROM users
WHERE username = :username
ORDER BY timestamp DESC
LIMIT 1;