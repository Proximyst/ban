UPDATE punishments
SET lifted = TRUE
WHERE lifted <> TRUE
  AND duration > 0
  AND time + duration <= UNIX_TIMESTAMP(NOW(3)) * 1000;