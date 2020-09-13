package com.proximyst.ban.data;

import com.proximyst.ban.boilerplate.model.MigrationIndexEntry;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An interface to the data storage.
 */
public interface IDataInterface {
  /**
   * Apply all migrations to the database.
   *
   * @param migrations The migrations available.
   */
  void applyMigrations(@NonNull List<MigrationIndexEntry> migrations);

  /**
   * Get all current punishments where the given {@link UUID} is the target.
   *
   * @param target The target of the punishments.
   * @return The punishments of the target in a mutable list.
   */
  @NonNull
  List<Punishment> getPunishmentsForTarget(@NonNull UUID target);

  /**
   * Add a punishment to the database.
   *
   * @param punishment The punishment to add.
   */
  void addPunishment(@NonNull Punishment punishment);

  /**
   * Lift a punishment in the database.
   *
   * @param punishment The punishment to lift.
   */
  void liftPunishment(@NonNull Punishment punishment);

  @NonNull
  Optional<@NonNull BanUser> getUser(@NonNull UUID uuid);

  @NonNull
  Optional<@NonNull BanUser> getUser(@NonNull String username);

  void saveUser(@NonNull BanUser user);
}
