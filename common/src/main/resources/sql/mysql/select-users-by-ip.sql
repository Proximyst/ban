SELECT B.*
FROM ip_addresses A,
     users B
WHERE A.address = :address
  AND B.uuid = A.uuid;