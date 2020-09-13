package com.proximyst.ban.boilerplate.model;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A migration entry with the data version and path to its SQL file.
 */
public final class MigrationIndexEntry {
  @NonNegative
  private int version;

  @MonotonicNonNull
  private String path;

  public MigrationIndexEntry(int version, String path) {
    this.version = version;
    this.path = path;
  }

  public MigrationIndexEntry() {
  }

  /**
   * @return The version in the database this migration represents.
   */
  public int getVersion() {
    return version;
  }

  /**
   * @return The path for the migration SQL file.
   */
  @NonNull
  public String getPath() {
    return path;
  }
}
