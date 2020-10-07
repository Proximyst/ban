//
// ban - A punishment suite for Velocity.
// Copyright (C) 2020 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package com.proximyst.ban.commands.cloud;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import com.google.common.collect.ImmutableList;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.manager.UserManager;
import com.proximyst.ban.model.BanUser;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiFunction;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BanUserArgument<C> extends CommandArgument<C, BanUser> {
  private static final TypeToken<BanUser> BAN_USER_TYPE_TOKEN = TypeToken.get(BanUser.class);

  private BanUserArgument(
      final boolean required,
      final @NonNull String name,
      final @NonNull String defaultValue,
      final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
      final @NonNull BanPlugin banPlugin
  ) {
    super(
        required,
        name,
        new BanUserParser<>(banPlugin.getUserManager(), banPlugin.getProxyServer()),
        defaultValue,
        BAN_USER_TYPE_TOKEN,
        suggestionsProvider
    );
  }

  public static <@NonNull C> @NonNull Builder<C> newBuilder(
      final @NonNull String name,
      final @NonNull BanPlugin banPlugin
  ) {
    return new BanUserArgument.Builder<>(name, banPlugin);
  }

  public static <@NonNull C> @NonNull CommandArgument<C, @NonNull BanUser> of(
      final @NonNull String name,
      final @NonNull BanPlugin plugin
  ) {
    return BanUserArgument.<C>newBuilder(name, plugin).asRequired().build();
  }

  public static <@NonNull C> @NonNull CommandArgument<C, @NonNull BanUser> optional(
      final @NonNull String name,
      final @NonNull BanPlugin plugin
  ) {
    return BanUserArgument.<C>newBuilder(name, plugin).asOptional().build();
  }

  public static final class Builder<C> extends CommandArgument.Builder<C, BanUser> {
    private final @NonNull BanPlugin banPlugin;

    public Builder(
        final @NonNull String name,
        final @NonNull BanPlugin banPlugin
    ) {
      super(BAN_USER_TYPE_TOKEN, name);
      this.banPlugin = banPlugin;
    }

    @Override
    public @NonNull BanUserArgument<C> build() {
      return new BanUserArgument<>(
          this.isRequired(),
          this.getName(),
          this.getDefaultValue(),
          this.getSuggestionsProvider(),
          this.banPlugin
      );
    }
  }

  public static final class BanUserParser<C> implements ArgumentParser<C, BanUser> {
    private final @NonNull UserManager userManager;
    private final @NonNull ProxyServer proxyServer;

    public BanUserParser(final @NonNull UserManager userManager, final @NonNull ProxyServer proxyServer) {
      this.userManager = userManager;
      this.proxyServer = proxyServer;
    }

    @Override
    public @NonNull ArgumentParseResult<BanUser> parse(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull Queue<String> inputQueue
    ) {
      final String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.failure(new NullPointerException("Expected player name/UUID"));
      }

      if (input.length() < 3) {
        // Too short for a player name.
        return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Expected player name/UUID"));
      }
      if (input.length() > 16) {
        // This must be a UUID.
        if (input.length() != 32 && input.length() != 36) {
          // Neither UUID without dashes nor with dashes.
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Expected player name/UUID"));
        }

        try {
          // We only want to make sure the UUID is valid here.
          //noinspection ResultOfMethodCallIgnored
          UUID.fromString(input);
        } catch (final IllegalArgumentException ignored) {
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Invalid UUID '" + input + "'"));
        }
      }

      return this.userManager.getUser(input).join()
          .map(user -> {
            inputQueue.remove();
            return ArgumentParseResult.success(user);
          })
          .orElseGet(() -> ArgumentParseResult.failure(
              new InvalidPlayerIdentifierException("Invalid player '" + input + "'")
          ));
    }

    @Override
    public @NonNull List<String> suggestions(
        final @NonNull CommandContext<C> commandContext,
        final @NonNull String input
    ) {
      final String lowercaseInput = input.toLowerCase(Locale.ENGLISH).trim();
      final ImmutableList.Builder<String> builder = ImmutableList.builder();
      for (final Player player : this.proxyServer.getAllPlayers()) {
        if (lowercaseInput.isEmpty() || player.getUsername().toLowerCase(Locale.ENGLISH).startsWith(lowercaseInput)) {
          builder.add(player.getUsername());
        }
      }
      return builder.build();
    }
  }

  public static final class InvalidPlayerIdentifierException extends IllegalArgumentException {
    public InvalidPlayerIdentifierException(@NonNull final String message) {
      super(message);
    }
  }
}
