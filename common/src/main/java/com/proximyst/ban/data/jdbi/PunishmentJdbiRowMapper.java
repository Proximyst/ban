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

import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class PunishmentJdbiRowMapper implements RowMapper<Punishment> {
  @Override
  public @NonNull Punishment map(final @NonNull ResultSet rs, final @NonNull StatementContext ctx)
      throws SQLException {
    final ColumnMapper<UUID> uuidMapper = ctx.findColumnMapperFor(UUID.class)
        .orElseThrow(() -> new IllegalStateException("no column mapper for UUID"));

    final long id = rs.getLong("id");
    final byte type = rs.getByte("type");

    return new PunishmentBuilder()
        .id(id)
        .type(
            PunishmentType.getById(type)
                .orElseThrow(() -> new IllegalStateException("punishment (" + id + ") type id " + type + " is unknown"))
        )
        .target(uuidMapper.map(rs, "target", ctx))
        .punisher(uuidMapper.map(rs, "punisher", ctx))
        .reason(rs.getString("reason"))
        .lifted(rs.getBoolean("lifted"))
        .liftedBy(rs.getObject("lifted_by") == null ? null : uuidMapper.map(rs, "lifted_by", ctx))
        .time(rs.getLong("time"))
        .duration(rs.getLong("duration"))
        .build();
  }
}
