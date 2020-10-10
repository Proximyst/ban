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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class PlayerArgument extends CommandArgument<@NonNull CommandSource, @NonNull Player> {
  @Inject
  public PlayerArgument(
      final @NonNull ProxyServer proxyServer,
      final @Assisted("required") boolean required,
      final @NonNull @Assisted("name") String name
  ) {
    super(
        required,
        name,
        new PlayerParser(proxyServer),
        Player.class
    );
  }

  public static final class PlayerParser implements ArgumentParser<@NonNull CommandSource, @NonNull Player> {
    private final @NonNull ProxyServer proxyServer;

    public PlayerParser(final @NonNull ProxyServer proxyServer) {
      this.proxyServer = proxyServer;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Player> parse(
        final @NonNull CommandContext<CommandSource> commandContext,
        final @NonNull Queue<String> inputQueue
    ) {
      final String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.failure(new NullPointerException("Expected player name/UUID"));
      }

      Player player = null;
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
          final UUID uuid = UUID.fromString(input);
          player = this.proxyServer.getPlayer(uuid).orElse(null);
        } catch (final IllegalArgumentException ignored) {
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Invalid UUID '" + input + "'"));
        }
      } else {
        player = this.proxyServer.getPlayer(input).orElse(null);
      }

      if (player == null) {
        return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Unknown player '" + input + "'"));
      }

      inputQueue.remove();
      return ArgumentParseResult.success(player);
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
        final @NonNull CommandContext<CommandSource> commandContext,
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
    public InvalidPlayerIdentifierException(final @NonNull String message) {
      super(message);
    }
  }
}
