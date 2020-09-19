package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.boilerplate.model.Pair;
import com.proximyst.ban.commands.helper.BaseCommand;
import com.proximyst.ban.commands.helper.UserArgument;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    UserArgument.getUser(
        getMain().getUserManager(),
        getMain().getProxyServer(),
        StringArgumentType.getString(ctx, "target")
    ).thenComposeAsync(
        user -> getMain().getPunishmentManager().getActiveBan(user.getUuid())
            .thenApply(opt -> new Pair<>(user, opt)),
        getMain().getSchedulerExecutor()
    ).thenAccept(pair -> {
      BanUser user = pair.getFirst();
      Punishment punishment = pair.getSecond().orElse(null);
      if (punishment == null) {
        ctx.getSource().sendMessage(MiniMessage.get().parse(
            getMain().getConfiguration().messages.errors.noBan,

            "targetName", user.getUsername(),
            "targetUuid", user.getUuid().toString()
        ));
        return;
      }

      // TODO(Proximyst): Broadcast unban
      punishment.setLiftedBy(CommandUtils.getSourceUuid(ctx.getSource()));
      getMain().getSchedulerExecutor().execute(() -> getMain().getDataInterface().addPunishment(punishment));
    });
  }
}
