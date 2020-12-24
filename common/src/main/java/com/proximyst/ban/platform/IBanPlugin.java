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

package com.proximyst.ban.platform;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.proximyst.ban.inject.ServiceModule;
import com.proximyst.ban.inject.ConfigurationModule;
import com.proximyst.ban.inject.factory.BanExceptionalFutureLoggerFactoryModule;
import com.proximyst.ban.inject.factory.CloudArgumentFactoryModule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.slf4j.Logger;

/**
 * The {@code ban} plugin on a specific platform, bound by the {@link IBanServer} and its {@link IBanAudience}.
 */
public interface IBanPlugin {
  /**
   * The Guice modules that are standard to the plugin, regardless of platform.
   */
  @NonNull Module @NonNull [] STANDARD_MODULES = new Module[]{
      new ConfigurationModule(),
      new BanExceptionalFutureLoggerFactoryModule(),
      new CloudArgumentFactoryModule(),
      new ServiceModule()
  };

  /**
   * @return The ID of the plugin on the platform. The meaning of the "ID" will vary depending on what platform the
   * plugin is running on.
   */
  @Pure
  @NonNull String pluginId();

  /**
   * @return The plugin's own logger.
   */
  @Pure
  @NonNull Logger pluginLogger();

  /**
   * @return The plugin's Guice injector. This is the entrypoint to any instance of the plugin's classes (et al.).
   */
  @Pure
  @NonNull Injector pluginInjector();
}
