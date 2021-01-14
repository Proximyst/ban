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

package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.inject.annotation.PluginData;
import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.proximyst.ban.platform.IBanPlugin;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.platform.VelocityBanSchedulerExecutor;
import com.proximyst.ban.platform.VelocityConsoleAudience;
import com.proximyst.ban.platform.VelocityServer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlatformModule extends AbstractModule {
  @Singleton
  @Provides
  @NonNull IBanPlugin banPlugin(final @NonNull BanPlugin banPlugin) {
    return banPlugin;
  }

  @Singleton
  @Provides
  @NonNull IBanServer banServer(final @NonNull VelocityServer server) {
    return server;
  }

  @Singleton
  @Provides
  @BanAsyncExecutor @NonNull Executor asyncExecutor(final @NonNull VelocityBanSchedulerExecutor executorImpl) {
    return executorImpl;
  }

  @Singleton
  @Provides
  @PluginData @NonNull Path dataDirectory(final @NonNull @DataDirectory Path dataDirectory) {
    return dataDirectory;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Singleton
  @Provides
  @PluginData @NonNull File dataDirectoryFile(final @NonNull @DataDirectory Path dataDirectory) {
    final File file = dataDirectory.toFile();
    file.mkdirs();
    return file;
  }

  @Singleton
  @Provides
  @NonNull IBanConsole banConsole(final @NonNull VelocityConsoleAudience velocityConsoleAudience) {
    return velocityConsoleAudience;
  }
}
