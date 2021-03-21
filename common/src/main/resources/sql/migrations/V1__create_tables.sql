CREATE SCHEMA IF NOT EXISTS ban;

CREATE TYPE ban.identity_type AS ENUM ('UUID', 'IPV4', 'IPV6', 'CONSOLE');
CREATE TABLE ban.identities
(
    id      SERIAL PRIMARY KEY,
    type    ban.identity_type NOT NULL,
    uuid    uuid  DEFAULT NULL,
    address bytea DEFAULT NULL,

    UNIQUE (type, uuid, address)
);
CREATE UNIQUE INDEX identities_uuid_index ON ban.identities (uuid);
CREATE UNIQUE INDEX identities_address_index ON ban.identities (address);
-- Insert console into row 1.
INSERT INTO ban.identities (type, uuid, address)
VALUES ('CONSOLE', '00000000-0000-0000-0000-000000000000', NULL);

CREATE TYPE ban.ip_address_type AS ENUM ('IPV4', 'IPV6');
CREATE TABLE ban.ip_addresses
(
    type    ban.ip_address_type NOT NULL,
    uuid    uuid                NOT NULL,
    address bytea               NOT NULL,

    PRIMARY KEY (type, uuid, address)
);
CREATE INDEX ip_addresses_uuid ON ban.ip_addresses (uuid);
CREATE INDEX ip_addresses_address ON ban.ip_addresses (type, address);

CREATE TABLE ban.users
(
    uuid      uuid        NOT NULL PRIMARY KEY,
    username  VARCHAR(24) NOT NULL,
    timestamp timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    identity  SERIAL REFERENCES ban.identities (id)
        ON DELETE CASCADE
);
CREATE INDEX users_username ON ban.users (username);

CREATE TYPE ban.punishment_type AS ENUM ('BAN', 'KICK', 'MUTE', 'WARNING', 'NOTE');
CREATE TABLE ban.punishments
(
    id        SERIAL PRIMARY KEY,
    type      ban.punishment_type NOT NULL,
    target    SERIAL REFERENCES ban.identities (id)
        ON DELETE CASCADE,
    punisher  SERIAL REFERENCES ban.identities (id)
        ON DELETE CASCADE,
    reason    TEXT,
    lifted    BOOLEAN             NOT NULL DEFAULT FALSE,
    lifted_by INTEGER REFERENCES ban.identities (id)
        ON DELETE CASCADE,
    timestamp timestamptz         NOT NULL,
    duration  BIGINT              NOT NULL
);
CREATE INDEX punishments_target ON ban.punishments (target);
CREATE INDEX punishments_punisher ON ban.punishments (punisher);
