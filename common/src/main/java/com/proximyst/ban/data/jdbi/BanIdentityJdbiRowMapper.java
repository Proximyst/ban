//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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

import com.proximyst.ban.factory.IIdentityFactory;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.ConsoleIdentity;
import com.proximyst.ban.model.sql.IdentityType;
import com.proximyst.ban.utils.ThrowableUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class BanIdentityJdbiRowMapper implements RowMapper<BanIdentity> {
  private final @NonNull IIdentityFactory identityFactory;
  private final @NonNull ConsoleIdentity consoleIdentity;

  @Inject
  BanIdentityJdbiRowMapper(final @NonNull IIdentityFactory identityFactory,
      final @NonNull ConsoleIdentity consoleIdentity) {
    this.identityFactory = identityFactory;
    this.consoleIdentity = consoleIdentity;
  }

  @Override
  public @NonNull BanIdentity map(final @NonNull ResultSet rs, final @NonNull StatementContext ctx)
      throws SQLException {
    final IdentityType type = ctx.findColumnMapperFor(IdentityType.class).orElseThrow()
        .map(rs, "type", ctx);

    if (type == IdentityType.CONSOLE) {
      return this.consoleIdentity;
    }

    final long id = rs.getLong("id");

    if (type == IdentityType.UUID) {
      final String uuidString = rs.getString("uuid");
      final UUID uuid = UUID.fromString(uuidString);
      final String username = rs.getString("username");

      return this.identityFactory.uuid(id, uuid, username);
    }

    // IPV4 or IPV6
    final byte[] addressBytes = rs.getBytes("address");
    final byte[] addressBytesFixed = type == IdentityType.IPV4 ? Arrays.copyOf(addressBytes, 4) : addressBytes;
    final InetAddress inetAddress;
    try {
      inetAddress = InetAddress.getByAddress(addressBytesFixed);
    } catch (final UnknownHostException ex) {
      ThrowableUtils.sneakyThrow(ex);
      throw new RuntimeException();
    }

    return this.identityFactory.ip(id, inetAddress);
  }
}
