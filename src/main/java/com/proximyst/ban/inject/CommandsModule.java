package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.proximyst.ban.BanPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CommandsModule extends AbstractModule {
  @NonNull
  private final BanPlugin plugin;

  public CommandsModule(@NonNull BanPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void configure() {
  }
}
