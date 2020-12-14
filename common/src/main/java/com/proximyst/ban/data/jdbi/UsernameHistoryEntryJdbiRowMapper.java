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

import com.proximyst.ban.model.UsernameHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class UsernameHistoryEntryJdbiRowMapper implements RowMapper<UsernameHistory.Entry> {
  @Override
  public UsernameHistory.@NonNull Entry map(final @NonNull ResultSet rs, final @NonNull StatementContext ctx)
      throws SQLException {
    return new UsernameHistory.Entry(rs.getString("username"),
        rs.getObject("timestamp") == null
            ? null
            : Date.from(rs.getTimestamp("timestamp").toInstant()));
  }
}
