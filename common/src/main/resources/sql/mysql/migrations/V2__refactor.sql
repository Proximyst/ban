-- New schema:
CREATE TABLE identities
(
    id      BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    -- 0 for UUID, 1 for IPv4, 2 for IPv6, 3 for CONSOLE
    type    TINYINT UNSIGNED NOT NULL,
    uuid    CHAR(36)    DEFAULT NULL,
    address BINARY(128) DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX (type, uuid, address)
);

CREATE TABLE ip_addresses
(
    -- 0 for IPv4, 1 for IPv6
    type    TINYINT UNSIGNED NOT NULL,
    uuid    CHAR(36)         NOT NULL,
    address BINARY(128)      NOT NULL,

    PRIMARY KEY (type, uuid, address)
);

-- We don't want to store username history.
DROP TABLE usernames;

-- Insert CONSOLE first.
INSERT INTO identities (id, type, uuid)
VALUES (0, 3, '00000000-0000-0000-0000-000000000000');

-- Add identities to existing users.
ALTER TABLE users
    ADD identity BIGINT UNSIGNED DEFAULT NULL
        REFERENCES identities (id);

-- Copy all existing users into their new identities.
-- We use type 0 for everyone because no IPs can be stored yet.
INSERT INTO identities (type, uuid)
SELECT 0, uuid
FROM users
WHERE uuid <> '00000000-0000-0000-0000-000000000000';

-- Now assign identities!
-- We don't check type because all entries should be type 0 now.
UPDATE users, identities
SET users.identity = identities.id
WHERE users.uuid = identities.uuid;

-- And remove the nullability...
ALTER TABLE users
    MODIFY identity BIGINT UNSIGNED NOT NULL;

-- Now let's do the same for punishments.
ALTER TABLE punishments
    ADD target_identity   BIGINT UNSIGNED DEFAULT NULL
        REFERENCES identities (id),
    ADD punisher_identity BIGINT UNSIGNED DEFAULT NULL
        REFERENCES identities (id);

-- Copy all existing users that have not yet been copied into the identities.
INSERT IGNORE INTO identities (type, uuid)
SELECT 0, target
FROM punishments
WHERE target <> '00000000-0000-0000-0000-000000000000';

INSERT IGNORE INTO identities (type, uuid)
SELECT 0, punisher
FROM punishments
WHERE punisher <> '00000000-0000-0000-0000-000000000000';

-- And assign the identities.
UPDATE punishments, identities
SET punishments.target_identity = identities.id
WHERE punishments.target = identities.uuid;

UPDATE punishments, identities
SET punishments.punisher_identity = identities.id
WHERE punishments.punisher = identities.uuid;

-- We don't want to refer to targets and punishers as UUIDs, so drop those.
ALTER TABLE punishments
    DROP target,
    DROP punisher,
    CHANGE target_identity target     BIGINT UNSIGNED NOT NULL,
    CHANGE punisher_identity punisher BIGINT UNSIGNED NOT NULL;
