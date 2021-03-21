SELECT id, type, address, NULL::VARCHAR AS username
FROM ban.identities
WHERE type IN ('IPV4', 'IPV6')
  AND address = :address;