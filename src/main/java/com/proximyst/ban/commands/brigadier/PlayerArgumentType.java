package com.proximyst.ban.commands.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.proximyst.ban.data.IMojangApi;
import com.velocitypowered.api.command.CommandSource;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlayerArgumentType {
  private static final SimpleCommandExceptionType EXPECTED_PLAYER = new SimpleCommandExceptionType(
      new LiteralMessage("Expected player name/UUID")
  );
  private static final DynamicCommandExceptionType INVALID_UUID = new DynamicCommandExceptionType(
      found -> new LiteralMessage("Invalid UUID '" + found + "'")
  );
  private static final DynamicCommandExceptionType INVALID_PLAYER_NAME = new DynamicCommandExceptionType(
      found -> new LiteralMessage("Invalid player name '" + found + "'")
  );

  private PlayerArgumentType() {
  }

  @NonNull
  public static UUID getPlayer(@NonNull IMojangApi mojangApi, CommandContext<CommandSource> ctx, @NonNull String name)
      throws CommandSyntaxException {
    return parse(mojangApi, ctx.getArgument(name, String.class));
  }

  private static UUID parse(@NonNull IMojangApi mojangApi, @NonNull String string) throws CommandSyntaxException {
    if (string.length() < 3) {
      // Too short for a player name.
      throw EXPECTED_PLAYER.create();
    }
    if (string.length() > 16) {
      // This must be a UUID.
      if (string.length() != 32 && string.length() != 36) {
        // Neither UUID without dashes nor with dashes.
        throw EXPECTED_PLAYER.create();
      }
    }

    if (string.length() == 36) {
      try {
        return UUID.fromString(string);
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.create(string);
      }
    }

    if (string.length() == 32) {
      // No dashes, add them first.
      StringBuilder builder = new StringBuilder(string);
      builder.insert(8, '-');
      builder.insert(13, '-');
      builder.insert(18, '-');
      builder.insert(23, '-');
      try {
        return UUID.fromString(builder.toString());
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.create(string);
      }
    }

    UUID uuid = mojangApi.getUuidFromUsername(string).orElse(null);
    if (uuid == null) {
      throw INVALID_PLAYER_NAME.create(string);
    }

    return uuid;
  }
}
