SELECT uuid, username
FROM ban.users
WHERE username = :username
ORDER BY timestamp DESC
LIMIT 1;