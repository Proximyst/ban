REPLACE INTO punishments
(id,
 type,
 target,
 punisher,
 reason,
 lifted,
 lifted_by,
 time,
 duration)
VALUES (:id, :type, :target, :punisher, :reason, :lifted, :lifted_by, :time, :duration);