package com.proximyst.ban.data;

import co.aikar.idb.DB;
import com.google.inject.Inject;
import com.proximyst.ban.boilerplate.model.MigrationIndexEntry;
import com.proximyst.ban.utils.ResourceReader;
import com.proximyst.ban.utils.ThrowableUtils;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

/**
 * A MySQL based interface to the data of this plugin.
 */
public class MySqlInterface implements IDataInterface {
  @NonNull
  private final Logger logger;

  @Inject
  public MySqlInterface(@NonNull Logger logger) {
    this.logger = logger;
  }

  @Override
  public void applyMigrations(@NonNull List<MigrationIndexEntry> migrations) throws SQLException {
    // Ensure the table exists first.
    SqlQueries.CREATE_VERSION_TABLE.forEachQuery(DB::executeUpdate);

    int version = Optional.ofNullable(DB.getFirstRow(SqlQueries.SELECT_VERSION.getQuery()))
        .map(row -> row.getInt("version"))
        .orElse(0);
    migrations.stream()
        .filter(mig -> mig.getVersion() > version)
        .sorted(Comparator.comparingInt(MigrationIndexEntry::getVersion))
        .forEach(mig -> {
          logger.info("Migrating database to version " + mig.getVersion() + "...");
          String queries = ResourceReader.readResource("sql/migrations/" + mig.getPath());
          for (String query : queries.split(";")) {
            if (query.trim().isEmpty()) {
              continue;
            }

            try {
              DB.executeUpdate(query);
              DB.executeUpdate(SqlQueries.UPDATE_VERSION.getQuery(), mig.getVersion());
            } catch (SQLException ex) {
              // Streams are kinda stupid...
              ThrowableUtils.sneakyThrow(ex);
            }
          }
        });
  }
}
