REPLACE INTO users
(uuid,
 username,
 timestamp)
VALUES (:uuid, :username, CURRENT_TIMESTAMP);