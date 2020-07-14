package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.config.MessageConfig;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.CommandUtils;
import com.proximyst.ban.utils.StringUtils;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand implements Command {
  private static final String USAGE = "/ban <player> [reason]";

  private final BanPlugin main;
  private final MessageConfig messageConfig;

  @Inject
  public BanCommand(
      @NonNull BanPlugin main,
      @NonNull MessageConfig messageConfig
  ) {
    this.main = main;
    this.messageConfig = messageConfig;
  }

  @Override
  public void execute(CommandSource source, @NonNull String[] args) {
    if (args.length == 0 || args[0].length() < 3) {
      source.sendMessage(MiniMessage.get().parse(
          messageConfig.getUsage(),
          "usage", USAGE
      ));
      return;
    }

    String player = args[0];
    @Nullable String reason = args.length > 1 ? StringUtils.join(" ", 1, args) : null;
    main.getProxyServer().getScheduler().buildTask(
        main,
        () -> {
          UUID uuid = main.getMojangApi().getUuidFromUsername(player).orElse(null);
          if (uuid == null) {
            source.sendMessage(MiniMessage.get().parse(
                messageConfig.getUnknownPlayer(),
                "player", player
            ));
            return;
          }

          main.getPunishmentManager().addPunishment(
              new PunishmentBuilder()
                  .type(PunishmentType.BAN)
                  .punisher(CommandUtils.getSourceUuid(source))
                  .target(uuid)

                  .reason(reason)
                  .build()
          );
        }
    ).schedule();
  }

  @Override
  public List<String> suggest(CommandSource source, @NonNull String[] currentArgs) {
    return CommandUtils.suggestPlayerNames(main.getProxyServer(), currentArgs);
  }

  @Override
  public boolean hasPermission(CommandSource source, @NonNull String[] args) {
    return source.hasPermission(BanPermissions.COMMAND_BAN);
  }
}
