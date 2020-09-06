package com.proximyst.ban.data.jdbi;

import java.sql.Types;
import java.util.UUID;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class UuidJdbiFactory extends AbstractArgumentFactory<UUID> {
  public UuidJdbiFactory() {
    super(Types.CHAR);
  }

  @Override
  protected Argument build(UUID value, ConfigRegistry config) {
    return (position, statement, $) -> {
      if (value == null) {
        statement.setNull(position, Types.CHAR);
      } else {
        statement.setString(position, value.toString());
      }
    };
  }
}
