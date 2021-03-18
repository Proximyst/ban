SELECT id, type, address
FROM ban.identities
WHERE type IN ('ipv4', 'ipv6')
  AND address = :address;