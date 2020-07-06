package com.proximyst.ban.boilerplate.model;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class MigrationIndexEntry {
  private int version;
  private String path;

  public MigrationIndexEntry(int version, String path) {
    this.version = version;
    this.path = path;
  }

  public MigrationIndexEntry() {
  }

  public int getVersion() {
    return version;
  }

  @NonNull
  public String getPath() {
    return path;
  }
}
