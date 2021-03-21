INSERT INTO ban.users (uuid, username, identity)
VALUES (:uuid, :username, :identity)
ON CONFLICT (uuid) DO UPDATE SET timestamp = CURRENT_TIMESTAMP;
