package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BanCommand implements Command {
  @NonNull
  private final ProxyServer proxyServer;

  @Inject
  public BanCommand(
      @NonNull ProxyServer proxyServer
  ) {
    this.proxyServer = proxyServer;
  }

  @Override
  public void execute(CommandSource source, @NonNull String[] args) {
    // TODO(Proximyst)
  }

  @Override
  public List<String> suggest(CommandSource source, @NonNull String[] currentArgs) {
    return CommandUtils.suggestPlayerNames(proxyServer, currentArgs);
  }

  @Override
  public boolean hasPermission(CommandSource source, @NonNull String[] args) {
    return source.hasPermission(BanPermissions.COMMAND_BAN);
  }
}
