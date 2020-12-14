INSERT IGNORE INTO usernames
(uuid,
 username,
 timestamp)
VALUES (:uuid, :username, :timestamp);