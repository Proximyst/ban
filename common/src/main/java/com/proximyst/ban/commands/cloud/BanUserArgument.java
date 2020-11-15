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
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.platform.BanAudience;
import com.proximyst.ban.platform.BanServer;
import com.proximyst.ban.service.IUserService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class BanUserArgument extends CommandArgument<@NonNull BanAudience, @NonNull BanUser> {
  @AssistedInject
  public BanUserArgument(
      final @NonNull IUserService userService,
      final @NonNull BanServer banServer,
      final @Assisted("required") boolean required,
      final @Assisted("name") @NonNull String name,
      final @Assisted("online") boolean online
  ) {
    super(
        required,
        name,
        new BanUserParser(userService, banServer, online),
        BanUser.class
    );
  }

  @AssistedInject
  public BanUserArgument(
      final @NonNull IUserService userService,
      final @NonNull BanServer banServer,
      final @Assisted("required") boolean required,
      final @Assisted("name") @NonNull String name
  ) {
    this(userService, banServer, required, name, false);
  }

  public static final class BanUserParser implements ArgumentParser<@NonNull BanAudience, BanUser> {
    private final @NonNull IUserService userService;
    private final @NonNull BanServer banServer;
    private final boolean online;

    public BanUserParser(final @NonNull IUserService userService,
        final @NonNull BanServer banServer,
        final boolean online) {
      this.userService = userService;
      this.banServer = banServer;
      this.online = online;
    }

    @Override
    public @NonNull ArgumentParseResult<BanUser> parse(
        final @NonNull CommandContext<BanAudience> commandContext,
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
          final UUID uuid = UUID.fromString(input);
          if (this.online && this.banServer.audienceOf(uuid) == null) {
            // The player isn't online, yet is required to be.
            return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Expected online player"));
          }
        } catch (final IllegalArgumentException ignored) {
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Invalid UUID '" + input + "'"));
        }
      } else {
        // This is a username.
        if (this.online && this.banServer.audienceOf(input) == null) {
          // The player isn't online, yet is required to be.
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Expected online player"));
        }
      }

      return this.userService.getUser(input)
          .exceptionally(ex -> Optional.empty())
          .join()
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
        final @NonNull CommandContext<BanAudience> commandContext,
        final @NonNull String input
    ) {
      final String lowercaseInput = input.toLowerCase(Locale.ENGLISH).trim();
      final ImmutableList.Builder<String> builder = ImmutableList.builder();

      for (final BanAudience player : this.banServer.onlineAudiences()) {
        if (lowercaseInput.isEmpty() || player.username().toLowerCase(Locale.ENGLISH).startsWith(lowercaseInput)) {
          builder.add(player.username());
        }
      }

      return builder.build();
    }

    @Override
    public boolean isContextFree() {
      return true;
    }
  }

  public static final class InvalidPlayerIdentifierException extends IllegalArgumentException {
    private static final long serialVersionUID = -6500019324607183855L;

    public InvalidPlayerIdentifierException(final @NonNull String message) {
      super(message);
    }
  }
}
