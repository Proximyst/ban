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

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.proximyst.ban.boilerplate.VelocityBanSchedulerExecutor;
import com.proximyst.ban.commands.BanCommand;
import com.proximyst.ban.commands.UnbanCommand;
import com.proximyst.ban.config.ConfigUtil;
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.data.IDataInterface;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.data.impl.MojangApiAshcon;
import com.proximyst.ban.data.impl.MySqlInterface;
import com.proximyst.ban.data.jdbi.UuidJdbiFactory;
import com.proximyst.ban.event.subscriber.BannedPlayerJoinSubscriber;
import com.proximyst.ban.event.subscriber.CacheUpdatePlayerJoinSubscriber;
import com.proximyst.ban.event.subscriber.MutedPlayerChatSubscriber;
import com.proximyst.ban.inject.DataModule;
import com.proximyst.ban.inject.PluginModule;
import com.proximyst.ban.manager.MessageManager;
import com.proximyst.ban.manager.PunishmentManager;
import com.proximyst.ban.manager.UserManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;

@Plugin(
    id = BanPlugin.PLUGIN_ID,
    name = BanPlugin.PLUGIN_NAME,
    version = BanPlugin.PLUGIN_VERSION,
    description = BanPlugin.PLUGIN_DESCRIPTION,
    authors = "Proximyst"
)
public class BanPlugin {
  public static final String PLUGIN_ID = "ban";
  public static final String PLUGIN_NAME = "ban";
  public static final String PLUGIN_VERSION = "0.1.0";
  public static final String PLUGIN_DESCRIPTION = "A simple punishment suite for Velocity.";

  public static final Gson COMPACT_GSON = new Gson();

  @NonNull
  private final ProxyServer proxyServer;

  @NonNull
  private final Logger logger;

  @NonNull
  private final Path dataDirectory;

  @NonNull
  private final Injector injector;

  @NonNull
  private final VelocityBanSchedulerExecutor schedulerExecutor;

  private ConfigurationNode rawConfigurationNode;
  private Configuration configuration;
  private IDataInterface dataInterface;
  private PunishmentManager punishmentManager;
  private MessageManager messageManager;
  private UserManager userManager;
  private IMojangApi mojangApi;
  private HikariDataSource hikariDataSource;
  private Jdbi jdbi;

  @Inject
  public BanPlugin(
      @NonNull final ProxyServer proxyServer,
      @NonNull final Logger logger,
      @NonNull @DataDirectory final Path dataDirectory
  ) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
    this.schedulerExecutor = new VelocityBanSchedulerExecutor(this);

    this.injector = Guice.createInjector(
        new PluginModule(this),
        new DataModule(this)
    );
  }

  @Subscribe
  public void onProxyInitialisation(final ProxyInitializeEvent event) {
    if (!this.getProxyServer().getConfiguration().isOnlineMode()) {
      this.getLogger().error("This plugin cannot function on offline mode.");
      this.getLogger().error("This plugin depends on Mojang's API and the presence of online mode players.");
      this.getLogger().error("Please either enable online mode, or find a new punishments plugin.");
      return;
    }

    final long start = System.currentTimeMillis();
    final TimeMeasurer tm = new TimeMeasurer(this.getLogger());

    tm.start("Reading configuration file");
    // Just to ensure the parents exist.
    //noinspection ResultOfMethodCallIgnored
    this.getDataDirectory().toFile().mkdirs();

    // Load configuration.
    try {
      final Path path = this.getDataDirectory().resolve("config.conf");
      // TODO: Use TOML configuration.
      final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
          .setPath(path)
          .build();

      // Loading...
      this.rawConfigurationNode = loader.load();
      this.configuration = ConfigUtil.loadConfiguration(this.getRawConfigurationNode());

      // Saving...
      ConfigUtil.saveConfiguration(this.getConfiguration(), this.getRawConfigurationNode());
      loader.save(this.getRawConfigurationNode());
    } catch (IOException | ObjectMappingException ex) {
      this.getLogger().error("Cannot read configuration", ex);
      return;
    }

    tm.start("Opening database pool");
    try {
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    } catch (final SQLException ex) {
      this.getLogger().error("Could not register a SQL driver", ex);
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
            BanPlugin.this.getLogger().warn("Could not execute JDBI statement.", ex);
          }
        })
        .registerArgument(new UuidJdbiFactory());
    this.dataInterface = new MySqlInterface(this.getLogger(), this.getJdbi());

    tm.start("Preparing database");
    try {
      this.getDataInterface().applyMigrations();
    } catch (final Exception ex) {
      this.getLogger().error("Could not prepare database", ex);
      return;
    }

    tm.start("Initialising plugin essentials");
    this.mojangApi = new MojangApiAshcon(this.getSchedulerExecutor());
    this.punishmentManager = new PunishmentManager(this);
    this.messageManager = new MessageManager(this, this.getConfiguration().messages);
    this.userManager = new UserManager(this);

    tm.start("Registering subscribers");
    this.getProxyServer().getEventManager()
        .register(this, this.getInjector().getInstance(BannedPlayerJoinSubscriber.class));
    this.getProxyServer().getEventManager()
        .register(this, this.getInjector().getInstance(MutedPlayerChatSubscriber.class));
    this.getProxyServer().getEventManager()
        .register(this, this.getInjector().getInstance(CacheUpdatePlayerJoinSubscriber.class));

    tm.start("Registering commands");
    this.getInjector().getInstance(BanCommand.class).register(this.getProxyServer().getCommandManager());
    this.getInjector().getInstance(UnbanCommand.class).register(this.getProxyServer().getCommandManager());

    tm.finish();
    this.getLogger().info("Plugin has finished initialisation in {}ms.", System.currentTimeMillis() - start);
  }

  @Subscribe
  public void onProxyShutdown(final ProxyShutdownEvent event) {
    final long start = System.currentTimeMillis();
    final TimeMeasurer tm = new TimeMeasurer(this.getLogger());

    tm.start("Unregistering listeners");
    this.getProxyServer().getEventManager().unregisterListeners(this);

    tm.start("Closing database");
    if (this.hikariDataSource != null) {
      this.hikariDataSource.close();
    }

    tm.finish();
    this.getLogger().info("Plugin disabled correctly in {}ms.", System.currentTimeMillis() - start);
  }

  @NonNull
  public ProxyServer getProxyServer() {
    return this.proxyServer;
  }

  @NonNull
  public Logger getLogger() {
    return this.logger;
  }

  @NonNull
  public Injector getInjector() {
    return this.injector;
  }

  @NonNull
  public Path getDataDirectory() {
    return this.dataDirectory;
  }

  @NonNull
  public Configuration getConfiguration() {
    return this.configuration;
  }

  @NonNull
  public ConfigurationNode getRawConfigurationNode() {
    return this.rawConfigurationNode;
  }

  @NonNull
  public IDataInterface getDataInterface() {
    return this.dataInterface;
  }

  @NonNull
  public PunishmentManager getPunishmentManager() {
    return this.punishmentManager;
  }

  @NonNull
  public MessageManager getMessageManager() {
    return this.messageManager;
  }

  @NonNull
  public UserManager getUserManager() {
    return this.userManager;
  }

  @NonNull
  public IMojangApi getMojangApi() {
    return this.mojangApi;
  }

  @NonNull
  public Jdbi getJdbi() {
    return this.jdbi;
  }

  @NonNull
  public VelocityBanSchedulerExecutor getSchedulerExecutor() {
    return this.schedulerExecutor;
  }

  private static class TimeMeasurer {
    private final Logger logger;
    private long start;
    private String current;

    private TimeMeasurer(final Logger logger) {
      this.logger = logger;
    }

    public void start(final String stage) {
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
