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

package com.proximyst.ban;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.proximyst.ban.commands.BanCommand;
import com.proximyst.ban.commands.HistoryCommand;
import com.proximyst.ban.commands.KickCommand;
import com.proximyst.ban.commands.MuteCommand;
import com.proximyst.ban.commands.UnbanCommand;
import com.proximyst.ban.commands.UnmuteCommand;
import com.proximyst.ban.config.ConfigUtil;
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.data.jdbi.PunishmentJdbiRowMapper;
import com.proximyst.ban.data.jdbi.UsernameHistoryEntryJdbiRowMapper;
import com.proximyst.ban.data.jdbi.UuidJdbiFactory;
import com.proximyst.ban.event.subscriber.BannedPlayerJoinSubscriber;
import com.proximyst.ban.event.subscriber.CacheUpdatePlayerSubscriber;
import com.proximyst.ban.event.subscriber.MutedPlayerChatSubscriber;
import com.proximyst.ban.inject.PlatformModule;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.platform.IBanPlugin;
import com.proximyst.ban.platform.VelocityAudience;
import com.proximyst.ban.service.IDataService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
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
  private final @NonNull Path dataDirectory;
  private final @NonNull Injector injector;
  private final @NonNull PluginContainer pluginContainer;

  private @MonotonicNonNull Configuration configuration;
  private @MonotonicNonNull HikariDataSource hikariDataSource;
  private @MonotonicNonNull Jdbi jdbi;

  @Inject
  public BanPlugin(final @NonNull ProxyServer proxyServer,
      final @NonNull Logger logger,
      final @NonNull @DataDirectory Path dataDirectory,
      final @NonNull Injector pluginInjector,
      final @NonNull PluginContainer pluginContainer) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
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

  @Subscribe
  public void onProxyInitialisation(final @NonNull ProxyInitializeEvent event) {
    if (!this.proxyServer.getConfiguration().isOnlineMode()) {
      this.logger.error("This plugin cannot function on offline mode.");
      this.logger.error("This plugin depends on Mojang's API and the presence of online mode players.");
      this.logger.error("Please either enable online mode, or find a new punishments plugin.");
      return;
    }

    final long start = System.nanoTime();
    final TimeMeasurer tm = this.injector.getInstance(TimeMeasurer.class);

    tm.start("Reading configuration file");
    // Just to ensure the parents exist.
    //noinspection ResultOfMethodCallIgnored
    this.dataDirectory.toFile().mkdirs();

    // Load configuration.
    try {
      final Path path = this.dataDirectory.resolve("config.conf");
      // TODO: Use TOML configuration.
      final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
          .setPath(path)
          .build();

      // Loading...
      final ConfigurationNode rawConfigurationNode = loader.load();
      this.configuration = ConfigUtil.loadConfiguration(rawConfigurationNode);

      // Saving...
      ConfigUtil.saveConfiguration(this.getConfiguration(), rawConfigurationNode);
      loader.save(rawConfigurationNode);
    } catch (IOException | ObjectMappingException ex) {
      this.logger.error("Cannot read configuration", ex);
      return;
    }

    tm.start("Opening database pool");
    try {
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    } catch (final SQLException ex) {
      this.logger.error("Could not register a SQL driver", ex);
      return;
    }

    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(this.getConfiguration().sql.jdbcUri);
    hikariConfig.setUsername(this.getConfiguration().sql.username);
    hikariConfig.setPassword(this.getConfiguration().sql.password);
    hikariConfig.setMaximumPoolSize(this.getConfiguration().sql.maxConnections);
    this.hikariDataSource = new HikariDataSource(hikariConfig);
    this.jdbi = Jdbi.create(this.hikariDataSource)
        .setSqlLogger(new SqlLogger() {
          @Override
          public void logException(@Nullable final StatementContext context, @NonNull final SQLException ex) {
            BanPlugin.this.logger.warn("Could not execute JDBI statement.", ex);
          }
        })
        .registerArgument(this.injector.getInstance(UuidJdbiFactory.class))
        .registerRowMapper(this.injector.getInstance(PunishmentJdbiRowMapper.class))
        .registerRowMapper(this.injector.getInstance(UsernameHistoryEntryJdbiRowMapper.class));

    tm.start("Preparing database");
    try {
      final IDataService service = this.injector.getInstance(IDataService.class);
      final Flyway flyway = Flyway.configure(this.getClass().getClassLoader())
          .baselineOnMigrate(true)
          .locations("classpath:" + service.getClassPathPrefix() + "migrations")
          .dataSource(this.hikariDataSource)
          .load();
      flyway.migrate();
    } catch (final Exception ex) {
      this.logger.error("Could not prepare database", ex);
      return;
    }

    tm.start("Initialising plugin essentials");
    final VelocityCommandManager<IBanAudience> velocityCommandManager = new VelocityCommandManager<>(
        this.pluginContainer,
        this.proxyServer,
        AsynchronousCommandExecutionCoordinator.<IBanAudience>newBuilder()
            .withAsynchronousParsing()
            .withExecutor(this.injector.getInstance(Key.get(Executor.class, BanAsyncExecutor.class)))
            .build(),
        VelocityAudience::getAudience,
        audience -> audience.<VelocityAudience>castAudience().getCommandSource()
    );
    // Cache the audience for the console.
    VelocityAudience.getAudience(this.proxyServer.getConsoleCommandSource());

    tm.start("Registering subscribers");
    this.proxyServer.getEventManager()
        .register(this, this.injector.getInstance(BannedPlayerJoinSubscriber.class));
    this.proxyServer.getEventManager()
        .register(this, this.injector.getInstance(MutedPlayerChatSubscriber.class));
    this.proxyServer.getEventManager()
        .register(this, this.injector.getInstance(CacheUpdatePlayerSubscriber.class));

    tm.start("Registering commands");
    this.injector.getInstance(BanCommand.class).register(velocityCommandManager);
    this.injector.getInstance(HistoryCommand.class).register(velocityCommandManager);
    this.injector.getInstance(KickCommand.class).register(velocityCommandManager);
    this.injector.getInstance(MuteCommand.class).register(velocityCommandManager);
    this.injector.getInstance(UnbanCommand.class).register(velocityCommandManager);
    this.injector.getInstance(UnmuteCommand.class).register(velocityCommandManager);

    tm.finish();
    this.logger.info("Plugin has finished initialisation in {}ms.",
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
  }

  @Subscribe
  public void onProxyShutdown(final @NonNull ProxyShutdownEvent event) {
    final long start = System.nanoTime();
    final TimeMeasurer tm = this.injector.getInstance(TimeMeasurer.class);

    tm.start("Unregistering listeners");
    this.proxyServer.getEventManager().unregisterListeners(this);

    tm.start("Closing database");
    if (this.hikariDataSource != null) {
      this.hikariDataSource.close();
    }

    tm.finish();
    this.logger.info("Plugin disabled correctly in {}ms.",
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
  }

  public @NonNull Configuration getConfiguration() {
    return this.configuration;
  }

  public @NonNull Jdbi getJdbi() {
    return this.jdbi;
  }

  private static class TimeMeasurer {
    private final @NonNull Logger logger;
    private @NonNegative long start;
    private @MonotonicNonNull String current;

    @Inject
    private TimeMeasurer(final @NonNull Logger logger) {
      this.logger = logger;
    }

    public void start(final @NonNull String stage) {
      if (this.start != 0) {
        this.finish();
      }

      this.start = System.nanoTime();
      this.current = stage;
    }

    public void finish() {
      if (this.start == 0) {
        return;
      }

      final long duration = System.nanoTime() - this.start;
      this.start = 0;
      this.logger.info("Finished stage ({}ms): {}", TimeUnit.NANOSECONDS.toMillis(duration), this.current);
    }
  }
}
