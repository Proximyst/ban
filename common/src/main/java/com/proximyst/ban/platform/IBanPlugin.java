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

package com.proximyst.ban.platform;

import cloud.commandframework.CommandManager;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.proximyst.ban.BanPluginImpl.BanPluginImplModule;
import com.proximyst.ban.commands.BanCommand;
import com.proximyst.ban.commands.HistoryCommand;
import com.proximyst.ban.commands.KickCommand;
import com.proximyst.ban.commands.MuteCommand;
import com.proximyst.ban.commands.UnbanCommand;
import com.proximyst.ban.commands.UnmuteCommand;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.inject.ConfigurationModule;
import com.proximyst.ban.inject.FactoryModule;
import com.proximyst.ban.inject.ServiceModule;
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
      new FactoryModule(),
      new ServiceModule(),
      new BanPluginImplModule()
  };

  /**
   * The {@link BaseCommand} types to be registered to the {@link #commandManager()}.
   */
  @NonNull Class<? extends BaseCommand> @NonNull [] COMMAND_CLASSES = createArray(
      BanCommand.class,
      HistoryCommand.class,
      KickCommand.class,
      MuteCommand.class,
      UnbanCommand.class,
      UnmuteCommand.class
  );

  /**
   * @return The ID of the plugin on the platform. The meaning of the "ID" will vary depending on what platform the
   * plugin is running on.
   */
  @Pure
  @NonNull String pluginId();

  /**
   * @return The plugin's own {@link Logger}.
   */
  @Pure
  @NonNull Logger pluginLogger();

  /**
   * @return The plugin's Guice {@link Injector}. This is the entrypoint to any instance of the plugin's classes (et
   * al.).
   */
  @Pure
  @NonNull Injector pluginInjector();

  /**
   * @return The plugin's {@link CommandManager} for use in registering commands.
   */
  @Pure
  @NonNull CommandManager<IBanAudience> commandManager();

  /**
   * Create an array of the given type. This is used because we can't make type-safe array creation with generics.
   *
   * @param array The array to be created.
   * @param <T>   The type of the items in the array.
   * @return The array created.
   */
  @SafeVarargs
  private static <T> @NonNull T @NonNull [] createArray(final @NonNull T @NonNull ... array) {
    return array;
  }
}
