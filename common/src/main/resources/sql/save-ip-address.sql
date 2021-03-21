INSERT INTO ban.ip_addresses (type, uuid, address)
VALUES (:type, :uuid, :address)
ON CONFLICT DO NOTHING;