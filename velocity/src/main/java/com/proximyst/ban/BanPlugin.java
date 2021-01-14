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

package com.proximyst.ban;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.event.subscriber.BannedPlayerJoinSubscriber;
import com.proximyst.ban.event.subscriber.CacheUpdatePlayerSubscriber;
import com.proximyst.ban.event.subscriber.MutedPlayerChatSubscriber;
import com.proximyst.ban.inject.PlatformModule;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.proximyst.ban.platform.IBanPlugin;
import com.proximyst.ban.platform.VelocityPlayerAudience;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

@Singleton
@Plugin(
    id = BanPlugin.PLUGIN_ID,
    name = BanPlugin.PLUGIN_NAME,
    version = BanPlugin.PLUGIN_VERSION,
    description = BanPlugin.PLUGIN_DESCRIPTION,
    authors = {"Proximyst"}
)
public final class BanPlugin implements IBanPlugin {
  public static final @NonNull String PLUGIN_ID = "ban";
  public static final @NonNull String PLUGIN_NAME = "ban";
  public static final @NonNull String PLUGIN_VERSION = "0.1.0";
  public static final @NonNull String PLUGIN_DESCRIPTION = "A simple punishment suite for Velocity.";

  private final @NonNull ProxyServer proxyServer;
  private final @NonNull Logger logger;
  private final @NonNull Injector injector;
  private final @NonNull PluginContainer pluginContainer;

  private @MonotonicNonNull VelocityCommandManager<IBanAudience> commandManager;
  private @MonotonicNonNull BanPluginImpl banPluginImpl;

  @Inject
  BanPlugin(final @NonNull ProxyServer proxyServer,
      final @NonNull Logger logger,
      final @NonNull Injector pluginInjector,
      final @NonNull PluginContainer pluginContainer) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.pluginContainer = pluginContainer;

    this.injector = pluginInjector.createChildInjector(ArrayUtils.add(STANDARD_MODULES, new PlatformModule()));
  }

  @Override
  public @NonNull String pluginId() {
    return PLUGIN_ID;
  }

  @Override
  public @NonNull Logger pluginLogger() {
    return this.logger;
  }

  @Override
  public @NonNull Injector pluginInjector() {
    return this.injector;
  }

  @Override
  public @NonNull CommandManager<IBanAudience> commandManager() {
    return this.commandManager;
  }

  @Subscribe
  public void onProxyInitialisation(final @NonNull ProxyInitializeEvent event) {
    this.banPluginImpl = this.injector.getInstance(BanPluginImpl.class);
    if (!this.banPluginImpl.enable()) {
      return;
    }

    this.commandManager = new VelocityCommandManager<>(
        this.pluginContainer,
        this.proxyServer,
        AsynchronousCommandExecutionCoordinator.<IBanAudience>newBuilder()
            .withAsynchronousParsing()
            .withExecutor(this.injector.getInstance(Key.get(Executor.class, BanAsyncExecutor.class)))
            .build(),
        (CommandSource sender) -> sender instanceof Player
            ? VelocityPlayerAudience.getAudience((Player) sender)
            : this.injector.getInstance(IBanConsole.class),
        (IBanAudience audience) -> audience instanceof VelocityPlayerAudience
            ? ((VelocityPlayerAudience) audience).player()
            : this.proxyServer.getConsoleCommandSource()
    );

    this.proxyServer.getEventManager()
        .register(this, this.injector.getInstance(BannedPlayerJoinSubscriber.class));
    this.proxyServer.getEventManager()
        .register(this, this.injector.getInstance(MutedPlayerChatSubscriber.class));
    this.proxyServer.getEventManager()
        .register(this, this.injector.getInstance(CacheUpdatePlayerSubscriber.class));

    for (final Class<? extends BaseCommand> commandType : COMMAND_CLASSES) {
      this.injector.getInstance(commandType).register(this.commandManager);
    }
  }

  @Subscribe
  public void onProxyShutdown(final @NonNull ProxyShutdownEvent event) {
    this.banPluginImpl.disable();
  }
}
