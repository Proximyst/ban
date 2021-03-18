SELECT id, type, uuid
FROM ban.identities
WHERE type IN ('uuid', 'console')
  AND uuid = :uuid;