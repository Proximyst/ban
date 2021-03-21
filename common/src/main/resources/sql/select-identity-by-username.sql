SELECT a.id, a.type, a.uuid, b.username
FROM ban.identities a
         JOIN ban.users b
              ON a.id = b.identity
WHERE b.username = :username;