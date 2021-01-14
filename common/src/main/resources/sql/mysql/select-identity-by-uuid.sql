SELECT id, type, uuid
FROM identities
WHERE type = 0
  AND uuid = :uuid;