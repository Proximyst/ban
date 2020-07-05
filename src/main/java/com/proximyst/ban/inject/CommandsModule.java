package com.proximyst.ban.inject;

import co.aikar.commands.CommandManager;
import co.aikar.commands.VelocityCommandManager;
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
    bind(VelocityCommandManager.class).toProvider(plugin::getCommandManager);
    bind(CommandManager.class).toProvider(plugin::getCommandManager);
  }
}
