package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.brigadier.PlayerArgumentType;
import com.proximyst.ban.commands.brigadier.VelocityBrigadierUtils;
import com.proximyst.ban.config.MessageConfig;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand {
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

  public void register() {
    main.getCommandDispatcher().register(
        VelocityBrigadierUtils.literal("ban")
            .requires(src -> src.hasPermission(BanPermissions.COMMAND_BAN))
            .then(VelocityBrigadierUtils.requiredArg("target", StringArgumentType.string())
                .then(VelocityBrigadierUtils.requiredArg("reason", StringArgumentType.greedyString())
                    .executes(this::execute))
                .executes(this::execute)
            )
    );
  }

  private int execute(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    UUID target = PlayerArgumentType.getPlayer(main.getMojangApi(), ctx, "target");
    @Nullable String reason = VelocityBrigadierUtils
        .getOptionalArgument(() -> StringArgumentType.getString(ctx, "reason"))
        .orElse(null);

    ctx.getSource().sendMessage(
        MiniMessage.get().parse(
            "<green>Banned <yellow><target></yellow>!",
            "target", main.getMojangApi().getUsernameFromUuid(target).orElse(target.toString())
        )
    );

    main.getPunishmentManager().addPunishment(
        new PunishmentBuilder()
            .type(PunishmentType.BAN)
            .punisher(CommandUtils.getSourceUuid(ctx.getSource()))
            .target(target)

            .reason(reason)
            .build()
    );

    return 1;
  }
}
