SELECT a.*
FROM ban.users a
         JOIN ban.ip_addresses b
              ON a.uuid = b.uuid
WHERE b.address = :address;