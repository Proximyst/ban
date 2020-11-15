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
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.platform.BanServer;
import com.proximyst.ban.platform.VelocityBanSchedulerExecutor;
import com.proximyst.ban.platform.VelocityServer;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;

public class PlatformModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
        .build(ICloudArgumentFactory.class));
  }

  @Singleton
  @Provides
  @NonNull BanServer banServer(final @NonNull VelocityServer server) {
    return server;
  }

  @Singleton
  @Provides
  @BanAsyncExecutor
  @NonNull Executor asyncExecutor(final @NonNull VelocityBanSchedulerExecutor executorImpl) {
    return executorImpl;
  }

  @Singleton
  @Provides
  @NonNull Configuration configuration(final @NonNull BanPlugin banPlugin) {
    return banPlugin.getConfiguration();
  }

  @Singleton
  @Provides
  @NonNull Jdbi jdbi(final @NonNull BanPlugin plugin) {
    return plugin.getJdbi();
  }
}
