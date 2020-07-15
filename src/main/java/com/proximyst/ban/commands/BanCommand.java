package com.proximyst.ban.commands;

import com.google.inject.Inject;
import com.proximyst.ban.BanPermissions;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.commands.helper.BaseCommand;
import com.proximyst.ban.commands.helper.argument.ArgumentReader;
import com.proximyst.ban.commands.helper.argument.FlagArgument;
import com.proximyst.ban.commands.helper.argument.UuidArgument;
import com.proximyst.ban.commands.helper.exception.IllegalCommandException;
import com.proximyst.ban.commands.helper.exception.UsageException;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.CommandUtils;
import com.velocitypowered.api.command.CommandSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanCommand extends BaseCommand {
  @Inject
  public BanCommand(@NonNull BanPlugin main) {
    super("ban [-s] <target> [reason]", main);
  }

  @Override
  protected void exec(@NonNull CommandSource source, @NonNull ArgumentReader args) throws IllegalCommandException {
    if (args.isEmpty()) {
      throw new UsageException();
    }

    boolean silent = FlagArgument.getFlags(args).contains("s");
    UUID target = UuidArgument.getUuid(getMain().getMojangApi(), args);
    @Nullable String reason = Optional.of(String.join(" ", args.getRemaining()))
        .filter(str -> !str.isEmpty())
        .orElse(null);

    getMain().getPunishmentManager().addPunishment(
        new PunishmentBuilder()
            .type(PunishmentType.BAN)
            .punisher(CommandUtils.getSourceUuid(source))
            .target(target)

            .silent(silent)
            .reason(reason)
            .build()
    );
  }

  @Override
  protected List<String> suggest(@NonNull CommandSource source, @NonNull ArgumentReader args) {
    return CommandUtils.suggestPlayerNames(getMain().getProxyServer(), args, true);
  }

  @Override
  public boolean hasPermission(@NonNull CommandSource source, @NonNull ArgumentReader args) {
    if (FlagArgument.getFlags(args).contains("s")) {
      return source.hasPermission(BanPermissions.COMMAND_BAN_SILENT);
    }

    return source.hasPermission(BanPermissions.COMMAND_BAN);
  }
}
