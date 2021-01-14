SELECT A.id as id, A.type as type, A.uuid as uuid
FROM identities A,
     users B
WHERE A.type = 0
  AND A.uuid = B.uuid
  AND B.username = :username;
