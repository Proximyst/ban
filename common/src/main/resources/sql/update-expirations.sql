UPDATE ban.punishments
SET lifted = TRUE
WHERE lifted = FALSE
  AND duration > 0
  AND timestamp + duration * INTERVAL '1 millisecond' <= CURRENT_TIMESTAMP;