package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.config.Configuration;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.config.SqlConfig;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.File;
import java.nio.file.Path;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

public final class PluginModule extends AbstractModule {
  @NonNull
  private final BanPlugin main;

  public PluginModule(@NonNull BanPlugin main) {
    this.main = main;
  }

  @Override
  protected void configure() {
    bind(BanPlugin.class).toInstance(main);
    bind(Logger.class).toInstance(main.getLogger());
    bind(ProxyServer.class).toInstance(main.getProxyServer());
    bind(Path.class).annotatedWith(DataDirectory.class).toInstance(main.getDataDirectory());
    bind(File.class).annotatedWith(DataDirectory.class).toProvider(() -> main.getDataDirectory().toFile());

    bind(Configuration.class).toProvider(main::getConfiguration);
    bind(SqlConfig.class).toProvider(() -> main.getConfiguration().getSql());
    bind(MessagesConfig.class).toProvider(() -> main.getConfiguration().getMessages());
  }
}
