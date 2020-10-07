package com.proximyst.ban.commands;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.cloud.BaseCommand;
import com.proximyst.ban.commands.cloud.PlayerArgument;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KickCommand extends BaseCommand {
  public KickCommand(@NonNull BanPlugin main) {
    super(main);
  }

  @Override
  public void register(@NonNull VelocityCommandManager<@NonNull CommandSource> commandManager) {
    commandManager.command(commandManager.commandBuilder("kick")
        .withPermission(BanPermissions.COMMAND_KICK)
        .argument(PlayerArgument.of("target", this.getMain()))
        .argument(StringArgument.of("reason", StringArgument.StringMode.GREEDY))
        .handler(this::execute));
  }

  private void execute(final @NonNull CommandContext<CommandSource> ctx) {
    final Player target = ctx.get("target");
    final @Nullable String reason = ctx.getOrDefault("reason", null);

    this.getMain().getPunishmentManager().addPunishment(
        new PunishmentBuilder()
            .type(PunishmentType.KICK)
            .punisher(CommandUtils.getSourceUuid(ctx.getSender()))
            .target(target.getUniqueId())
            .reason(reason)
    );
  }
}
