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
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.config.SqlConfig;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.nio.file.Path;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

public final class PluginModule extends AbstractModule {
  @NonNull
  private final BanPlugin main;

  public PluginModule(@NonNull BanPlugin main) {
    this.main = main;
  }

  @Override
  protected void configure() {
    bind(BanPlugin.class).toInstance(main);
    bind(Logger.class).toInstance(main.getLogger());
    bind(ProxyServer.class).toInstance(main.getProxyServer());
    bind(Path.class).annotatedWith(DataDirectory.class).toInstance(main.getDataDirectory());
    bind(File.class).annotatedWith(DataDirectory.class).toProvider(() -> main.getDataDirectory().toFile());

    bind(Configuration.class).toProvider(main::getConfiguration);
    bind(SqlConfig.class).toProvider(() -> main.getConfiguration().sql);
    bind(MessagesConfig.class).toProvider(() -> main.getConfiguration().messages);
  }
}
