WITH insert_rows (type, uuid, address) AS (
        (SELECT type, uuid, address FROM ban.identities LIMIT 0)
        UNION ALL
        SELECT :type, :uuid, :address
)
   , inserted AS (
    INSERT INTO ban.identities (type, uuid, address)
        SELECT type, uuid, address FROM insert_rows
        ON CONFLICT DO NOTHING
        RETURNING id
)
SELECT id
FROM inserted
UNION ALL
SELECT b.id
FROM insert_rows
         JOIN ban.identities b
              ON b.type = insert_rows.type
                  AND b.uuid IS NOT DISTINCT FROM insert_rows.uuid
                  AND b.address IS NOT DISTINCT FROM insert_rows.address
;
