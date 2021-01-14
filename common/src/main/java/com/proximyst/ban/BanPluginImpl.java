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

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.proximyst.ban.config.ConfigUtil;
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.data.jdbi.BanIdentityJdbiRowMapper;
import com.proximyst.ban.data.jdbi.PunishmentJdbiRowMapper;
import com.proximyst.ban.data.jdbi.UuidJdbiFactory;
import com.proximyst.ban.inject.annotation.PluginData;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.service.IDataService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Inject;
import javax.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;

/**
 * The implementation for a plugin. Most/all logic should be deferred to this class.
 */
@Singleton
public final class BanPluginImpl {
  private final @NonNull IBanServer banServer;
  private final @NonNull Logger logger;
  private final @NonNull Path dataDirectory;
  private final @NonNull Injector injector;

  private @MonotonicNonNull Configuration configuration;
  private @MonotonicNonNull HikariDataSource hikariDataSource;
  private @MonotonicNonNull Jdbi jdbi;

  @Inject
  BanPluginImpl(final @NonNull IBanServer banServer,
      final @NonNull Logger logger,
      final @PluginData @NonNull Path dataDirectory,
      final @NonNull Injector injector) {
    this.banServer = banServer;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
    this.injector = injector;
  }

  public boolean enable() {
    if (!this.banServer.isOnlineMode()) {
      this.logger.warn("This plugin will not provide any kind of support on offline mode.");
      this.logger.warn("This plugin may not function whatsoever on offline mode.");
      this.logger.warn("You may not receive any kind of support while running offline mode.");
    }

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
      ConfigUtil.saveConfiguration(this.configuration, rawConfigurationNode);
      loader.save(rawConfigurationNode);
    } catch (IOException | ObjectMappingException ex) {
      this.logger.error("Cannot read configuration", ex);
      return false;
    }

    try {
      DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
    } catch (final SQLException ex) {
      this.logger.error("Could not register a data driver", ex);
      return false;
    }

    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(this.configuration.sql.jdbcUri);
    hikariConfig.setUsername(this.configuration.sql.username);
    hikariConfig.setPassword(this.configuration.sql.password);
    hikariConfig.setMaximumPoolSize(this.configuration.sql.maxConnections);
    this.hikariDataSource = new HikariDataSource(hikariConfig);
    this.jdbi = Jdbi.create(this.hikariDataSource)
        .setSqlLogger(new SqlLogger() {
          @Override
          public void logException(@Nullable final StatementContext context, @NonNull final SQLException ex) {
            BanPluginImpl.this.logger.warn("Could not execute JDBI statement.", ex);
          }
        })
        .registerArgument(this.injector.getInstance(UuidJdbiFactory.class))
        .registerRowMapper(this.injector.getInstance(PunishmentJdbiRowMapper.class))
        .registerRowMapper(this.injector.getInstance(BanIdentityJdbiRowMapper.class));

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
      return false;
    }

    return true;
  }

  public void disable() {
    if (this.hikariDataSource != null) {
      this.hikariDataSource.close();
    }
  }

  public static final class BanPluginImplModule extends AbstractModule {
    @Singleton
    @Provides
    @NonNull Configuration configuration(final @NonNull BanPluginImpl banPluginImpl) {
      return banPluginImpl.configuration;
    }

    @Singleton
    @Provides
    @NonNull Jdbi jdbi(final @NonNull BanPluginImpl banPluginImpl) {
      return banPluginImpl.jdbi;
    }
  }
}
