INSERT INTO ban.punishments
(type, target, punisher, reason, lifted, lifted_by, timestamp, duration)
VALUES (:type, :target, :punisher, :reason, :lifted, :lifted_by, :time, :duration)
RETURNING id;