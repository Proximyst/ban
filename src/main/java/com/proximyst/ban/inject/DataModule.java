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
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.data.IDataInterface;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.manager.MessageManager;
import com.proximyst.ban.manager.PunishmentManager;
import com.proximyst.ban.manager.UserManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;

public final class DataModule extends AbstractModule {
  @NonNull
  private final BanPlugin main;

  public DataModule(@NonNull final BanPlugin main) {
    this.main = main;
  }

  @Override
  protected void configure() {
    this.bind(IDataInterface.class).toProvider(this.main::getDataInterface);
    this.bind(PunishmentManager.class).toProvider(this.main::getPunishmentManager);
    this.bind(MessageManager.class).toProvider(this.main::getMessageManager);
    this.bind(UserManager.class).toProvider(this.main::getUserManager);
    this.bind(IMojangApi.class).toProvider(this.main::getMojangApi);
    this.bind(Jdbi.class).toProvider(this.main::getJdbi);
  }
}
