UPDATE ban.punishments
SET lifted    = :lifted,
    lifted_by = :lifted_by
WHERE id = :id;