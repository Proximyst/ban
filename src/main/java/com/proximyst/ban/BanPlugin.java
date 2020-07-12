package com.proximyst.ban;

import co.aikar.idb.DB;
import co.aikar.idb.DatabaseOptions;
import co.aikar.idb.PooledDatabaseOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.proximyst.ban.boilerplate.Slf4jLoggerProxy;
import com.proximyst.ban.boilerplate.model.MigrationIndexEntry;
import com.proximyst.ban.commands.BanCommand;
import com.proximyst.ban.config.ConfigUtil;
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.data.IDataInterface;
import com.proximyst.ban.data.MySqlInterface;
import com.proximyst.ban.data.PunishmentManager;
import com.proximyst.ban.inject.CommandsModule;
import com.proximyst.ban.inject.DataModule;
import com.proximyst.ban.inject.PluginModule;
import com.proximyst.ban.utils.ResourceReader;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;
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

  private ConfigurationNode rawConfigurationNode;
  private Configuration configuration;
  private IDataInterface dataInterface;
  private PunishmentManager punishmentManager;

  @Inject
  public BanPlugin(
      @NonNull ProxyServer proxyServer,
      @NonNull Logger logger,
      @NonNull @DataDirectory Path dataDirectory
  ) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.dataDirectory = dataDirectory;

    injector = Guice.createInjector(
        new PluginModule(this),
        new CommandsModule(this),
        new DataModule(this)
    );
  }

  @Subscribe
  public void onProxyInitialisation(ProxyInitializeEvent event) {
    getLogger().info("Reading configuration file...");
    // Just to ensure the parents exist.
    //noinspection ResultOfMethodCallIgnored
    getDataDirectory().toFile().mkdirs();

    // Load configuration.
    try {
      Path path = getDataDirectory().resolve("config.conf");
      // TODO: Use TOML configuration.
      HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
          .setPath(path)
          .build();

      // Loading...
      rawConfigurationNode = loader.load();
      configuration = ConfigUtil.loadConfiguration(getRawConfigurationNode());

      // Saving...
      ConfigUtil.saveConfiguration(getConfiguration(), getRawConfigurationNode());
      loader.save(getRawConfigurationNode());
    } catch (IOException | ObjectMappingException ex) {
      getLogger().error("Cannot read configuration", ex);
      return;
    }
    getLogger().info("Read configuration!");

    getLogger().info("Opening a pooled database...");
    DB.setGlobalDatabase(PooledDatabaseOptions.builder()
        .options(
            DatabaseOptions.builder()
                .mysql(
                    getConfiguration().getSql().getUsername(),
                    getConfiguration().getSql().getPassword(),
                    getConfiguration().getSql().getDatabase(),
                    getConfiguration().getSql().getHostname() + ":" + getConfiguration().getSql().getPort()
                )
                .useOptimizations(false)
                .logger(new Slf4jLoggerProxy(getLogger()))
                .build()
        )
        .maxConnections(getConfiguration().getSql().getMaxConnections())
        .createHikariDatabase());
    dataInterface = getInjector().getInstance(MySqlInterface.class);
    getLogger().info("Database pool opened!");

    getLogger().info("Preparing database...");
    String migrationsIndexJson = ResourceReader.readResource("sql/migrations/migrations-index.json");
    List<MigrationIndexEntry> migrationIndexEntries = COMPACT_GSON
        .fromJson(
            migrationsIndexJson,
            new TypeToken<List<MigrationIndexEntry>>() {
            }.getType()
        );
    try {
      getDataInterface().applyMigrations(migrationIndexEntries);
    } catch (SQLException ex) {
      getLogger().error("Could not prepare database", ex);
      return;
    }
    getLogger().info("Database prepared!");

    getLogger().info("Initialising plugin essentials...");
    punishmentManager = getInjector().getInstance(PunishmentManager.class);
    getLogger().info("Plugin essentials initialised!");

    getLogger().info("Registering commands...");
    getProxyServer().getCommandManager().register("ban", getInjector().getInstance(BanCommand.class));
    getLogger().info("Finished registering commands!");

    getLogger().info("Plugin has finished initialisation.");
  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    getLogger().info("Closing database...");
    DB.close();
    getLogger().info("Closed database!");

    getLogger().info("Plugin disabled correctly.");
  }

  @NonNull
  public ProxyServer getProxyServer() {
    return proxyServer;
  }

  @NonNull
  public Logger getLogger() {
    return logger;
  }

  @NonNull
  public Injector getInjector() {
    return injector;
  }

  @NonNull
  public Path getDataDirectory() {
    return dataDirectory;
  }

  @NonNull
  public Configuration getConfiguration() {
    return configuration;
  }

  @NonNull
  public ConfigurationNode getRawConfigurationNode() {
    return rawConfigurationNode;
  }

  @NonNull
  public IDataInterface getDataInterface() {
    return dataInterface;
  }

  @NonNull
  public PunishmentManager getPunishmentManager() {
    return punishmentManager;
  }
}
