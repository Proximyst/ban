package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.helper.BaseCommand;
import com.proximyst.ban.commands.helper.UserArgument;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand extends BaseCommand {
  @Inject
  public BanCommand(@NonNull BanPlugin main) {
    super(main);
  }

  @Override
  public void register(@NonNull CommandManager commandManager) {
    RequiredArgumentBuilder<CommandSource, String> logic =
        argRequired("target", StringArgumentType.string())
            .then(
                argRequired("reason", StringArgumentType.greedyString())
                    .executes(executeAsync(this::execute))
            )
            .executes(executeAsync(this::execute));
    commandManager.register(new BrigadierCommand(
        literal("ban")
            .requires(src -> src.hasPermission(BanPermissions.COMMAND_BAN))
            .then( // -s flag
                literal("-s")
                    .requires(src -> src.hasPermission(BanPermissions.COMMAND_BAN_SILENT))
                    .then(logic)
            )
            .then(logic) // no -s flag
    ));
  }

  private void execute(@NonNull CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    // TODO(Proximyst): Support silent flag
    UUID target = UserArgument.getUuid(
        getMain().getMojangApi(),
        getMain().getProxyServer(),
        StringArgumentType.getString(ctx, "target")
    );
    @Nullable String reason = getOptionalArgument(() -> StringArgumentType.getString(ctx, "reason"))
        .map(String::trim)
        .filter(str -> !str.isEmpty())
        .orElse(null);

    getMain().getPunishmentManager().addPunishment(
        new PunishmentBuilder()
            .type(PunishmentType.BAN)
            .punisher(CommandUtils.getSourceUuid(ctx.getSource()))
            .target(target)

//            .silent(silent) // TODO(Proximyst)
            .reason(reason)
    );
  }
}
