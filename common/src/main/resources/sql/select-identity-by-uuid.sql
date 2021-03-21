SELECT a.id, a.type, a.uuid
FROM ban.identities a
         JOIN ban.users b
              ON a.id = b.identity
WHERE a.type IN ('UUID', 'CONSOLE')
  AND a.uuid = :uuid;