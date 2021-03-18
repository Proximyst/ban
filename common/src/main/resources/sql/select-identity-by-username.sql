SELECT a.id, a.type, a.uuid
FROM ban.identities a
         JOIN ban.users b
              ON a.id = b.identity
WHERE b.username = :username;