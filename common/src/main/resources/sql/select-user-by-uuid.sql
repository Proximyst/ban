SELECT *
FROM ban.users
WHERE uuid = :uuid
ORDER BY timestamp DESC
LIMIT 1;