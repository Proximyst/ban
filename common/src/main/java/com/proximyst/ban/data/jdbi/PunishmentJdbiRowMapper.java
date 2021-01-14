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

import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.service.IDataService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.inject.Provider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class PunishmentJdbiRowMapper implements RowMapper<Punishment> {
  private final @NonNull Provider<@NonNull IDataService> dataService;

  public PunishmentJdbiRowMapper(final @NonNull Provider<@NonNull IDataService> dataService) {
    this.dataService = dataService;
  }

  @Override
  public @NonNull Punishment map(final @NonNull ResultSet rs, final @NonNull StatementContext ctx)
      throws SQLException {
    final ColumnMapper<UUID> uuidMapper = ctx.findColumnMapperFor(UUID.class)
        .orElseThrow(() -> new IllegalStateException("no column mapper for UUID"));

    final long id = rs.getLong("id");
    final byte rawType = rs.getByte("type");
    final long targetId = rs.getLong("target");
    final long punisherId = rs.getLong("punisher");
    final String reason = rs.getString("reason");
    final boolean lifted = rs.getBoolean("lifted");
    final UUID liftedBy = lifted ? uuidMapper.map(rs, "lifted_by", ctx) : null;
    final long time = rs.getLong("time");
    final long duration = rs.getLong("duration");

    final IDataService dataService = this.dataService.get();
    final PunishmentType type = PunishmentType.getById(rawType)
        .orElseThrow(() -> new IllegalStateException("punishment (" + id + "): type id " + rawType + " is unknown"));
    final BanIdentity target = dataService.getUser(targetId)
        .orElseThrow(() -> new IllegalStateException("punishment (" + id + "): target id " + targetId + " is unknown"));
    final BanIdentity punisher = dataService.getUser(punisherId)
        .orElseThrow(() -> new IllegalStateException("punishment (" + id + "): target id " + targetId + " is unknown"));

    return new Punishment(id,
        type,
        target,
        punisher,
        reason,
        lifted,
        liftedBy,
        time,
        duration);
  }
}
