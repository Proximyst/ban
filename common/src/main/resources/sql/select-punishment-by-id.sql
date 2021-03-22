SELECT a.id
     , a.type
     , a.target
     , a.punisher
     , a.reason
     , a.lifted
     , lifted_by.uuid AS lifted_by
     , a.timestamp
     , a.duration
FROM ban.punishments a
         LEFT JOIN ban.identities lifted_by
                   ON a.lifted_by = lifted_by.id
WHERE a.id = :id;