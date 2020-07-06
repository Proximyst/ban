package com.proximyst.ban.data;

import com.proximyst.ban.boilerplate.model.MigrationIndexEntry;
import java.sql.SQLException;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An interface to the data storage.
 */
public interface IDataInterface {
  void applyMigrations(@NonNull List<MigrationIndexEntry> migrations) throws SQLException;
}
