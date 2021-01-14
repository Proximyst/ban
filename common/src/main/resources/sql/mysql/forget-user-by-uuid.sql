DELETE A
FROM punishments A,
     identities B
WHERE B.type = 0
  AND B.uuid = :uuid
  AND A.target = B.id;

UPDATE punishments A, identities B
SET A.punisher = '00000000-0000-0000-0000-000000000000'
WHERE B.type = 0
  AND B.uuid = :uuid
  AND A.punisher = B.id;

DELETE
FROM ip_addresses
WHERE uuid = :uuid;

DELETE
FROM users
WHERE uuid = :uuid;

DELETE
FROM identities
WHERE uuid = :uuid;
