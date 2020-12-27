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

package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IMojangService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.IUserService;
import com.proximyst.ban.service.impl.ImplAshconMojangService;
import com.proximyst.ban.service.impl.ImplGenericSqlDataService;
import com.proximyst.ban.service.impl.ImplPunishmentService;
import com.proximyst.ban.service.impl.ImplUserService;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ServiceModule extends AbstractModule {
  // TODO(Proximyst): Support non-SQL data?
  @Provides
  @Singleton
  @NonNull IDataService dataService(final @NonNull ImplGenericSqlDataService sqlDataService) {
    return sqlDataService;
  }

  // TODO(Proximyst): Support official Mojang API
  @Provides
  @Singleton
  @NonNull IMojangService mojangService(final @NonNull ImplAshconMojangService ashconMojangService) {
    return ashconMojangService;
  }

  @Provides
  @Singleton
  @NonNull IPunishmentService punishmentService(final @NonNull ImplPunishmentService punishmentService) {
    return punishmentService;
  }

  @Provides
  @Singleton
  @NonNull IUserService userService(final @NonNull ImplUserService userService) {
    return userService;
  }
}
