//
// ban - A punishment suite for Velocity.
// Copyright (C) 2020 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package com.proximyst.ban.data.jdbi;

import java.sql.Types;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class UuidJdbiFactory extends AbstractArgumentFactory<UUID> {
  public UuidJdbiFactory() {
    super(Types.CHAR);
  }

  @Override
  protected Argument build(@Nullable final UUID value, @Nullable final ConfigRegistry config) {
    return (position, statement, $) -> {
      if (value == null) {
        statement.setNull(position, Types.CHAR);
      } else {
        statement.setString(position, value.toString());
      }
    };
  }
}
