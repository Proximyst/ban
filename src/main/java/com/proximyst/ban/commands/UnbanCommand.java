package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.helper.BaseCommand;
import com.proximyst.ban.commands.helper.UserArgument;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnbanCommand extends BaseCommand {
  @Inject
  public UnbanCommand(@NonNull BanPlugin main) {
    super(main);
  }

  @Override
  public void register(@NonNull CommandManager commandManager) {
    commandManager.register(new BrigadierCommand(
        literal("unban")
            .requires(src -> src.hasPermission(BanPermissions.COMMAND_UNBAN))
            .then(argRequired("target", StringArgumentType.string())
                .suggests(UserArgument.createSuggestions(getMain()))
                .executes(execute(this::execute)))
    ));
  }

  private void execute(@NonNull CommandContext<CommandSource> ctx) throws CommandSyntaxException {
    UserArgument.getUuid(
        getMain().getUserManager(),
        getMain().getProxyServer(),
        StringArgumentType.getString(ctx, "target")
    ).thenComposeAsync(
        target -> getMain().getPunishmentManager().getActiveBan(target),
        getMain().getSchedulerExecutor()
    ).thenAccept(opt -> {
      Punishment punishment = opt.orElse(null);
      if (punishment == null) {
        ctx.getSource().sendMessage(TextComponent.of("No ban on " + StringArgumentType.getString(ctx, "target"))); // TODO(Proximyst)
        return;
      }

      punishment.setLiftedBy(CommandUtils.getSourceUuid(ctx.getSource()));
      getMain().getDataInterface().addPunishment(punishment);
    });
  }
}
