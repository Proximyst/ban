package com.proximyst.ban.commands.helper.argument;

import com.proximyst.ban.commands.helper.exception.IllegalCommandException;
import com.proximyst.ban.data.IMojangApi;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UuidArgument {
  private static final Supplier<IllegalCommandException> EXPECTED_PLAYER = () ->
      new IllegalCommandException("Expected player name/UUID");
  private static final Function<Object, IllegalArgumentException> INVALID_UUID = found ->
      new IllegalArgumentException("Invalid UUID '" + found + "'");
  private static final Function<Object, IllegalArgumentException> INVALID_USERNAME = found ->
      new IllegalArgumentException("Invalid player name '" + found + "'");

  private UuidArgument() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static UUID getUuid(@NonNull IMojangApi mojangApi, @NonNull ArgumentReader arguments)
      throws IllegalCommandException {
    String string = arguments.tryPop().orElseThrow(EXPECTED_PLAYER);

    if (string.length() < 3) {
      // Too short for a player name.
      throw EXPECTED_PLAYER.get();
    }
    if (string.length() > 16) {
      // This must be a UUID.
      if (string.length() != 32 && string.length() != 36) {
        // Neither UUID without dashes nor with dashes.
        throw EXPECTED_PLAYER.get();
      }
    }

    if (string.length() == 36) {
      try {
        return UUID.fromString(string);
      } catch (IllegalArgumentException ex) {
        throw INVALID_UUID.apply(string);
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
        throw INVALID_UUID.apply(string);
      }
    }

    UUID uuid = mojangApi.getUuidFromUsername(string).orElse(null);
    if (uuid == null) {
      throw INVALID_USERNAME.apply(string);
    }

    return uuid;
  }
}
