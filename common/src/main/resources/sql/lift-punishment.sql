UPDATE ban.punishments
SET lifted    = :lifted
  , lifted_by = (SELECT identities.id FROM ban.identities WHERE uuid = :lifted_by)
WHERE id = :id;