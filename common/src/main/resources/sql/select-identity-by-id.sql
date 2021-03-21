SELECT a.id, a.type, a.uuid, a.address, b.username
FROM ban.identities a
         LEFT JOIN ban.users b
                   ON a.id = b.identity
WHERE a.id = :id;