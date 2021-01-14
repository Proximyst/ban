SELECT id, type, address
FROM identities
WHERE (type = 1
    OR type = 2)
  AND address = :address;