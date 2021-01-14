//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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
import com.google.common.net.InetAddresses;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.IpIdentity;
import com.proximyst.ban.model.BanIdentity.UuidIdentity;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IUserService;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

public final class BanIdentityArgument<I extends BanIdentity> extends
    CommandArgument<@NonNull IBanAudience, @NonNull I> {
  @AssistedInject
  BanIdentityArgument(final @NonNull IUserService userService,
      final @NonNull IBanServer banServer,
      final @NonNull IDataService dataService,
      final @Assisted("required") boolean required,
      final @Assisted("name") @NonNull String name,
      final @Assisted("online") boolean online,
      final @Assisted("type") @NonNull Class<I> type) {
    super(required,
        name,
        new BanIdentityParser<>(type, userService, banServer, dataService, online),
        type);
  }

  @AssistedInject
  BanIdentityArgument(final @NonNull IUserService userService,
      final @NonNull IBanServer banServer,
      final @NonNull IDataService dataService,
      final @Assisted("required") boolean required,
      final @Assisted("name") @NonNull String name,
      final @Assisted("type") @NonNull Class<I> type) {
    this(userService, banServer, dataService, required, name, false, type);
  }

  public static final class BanIdentityParser<I extends BanIdentity>
      implements ArgumentParser<@NonNull IBanAudience, @NonNull I> {
    private static final @NonNull Throwable SUGGESTIONS_FAILURE = new Throwable("suggestions shall not be parsed");
    private static final @NonNull Throwable EXPECTED_ANY = new InvalidPlayerIdentifierException("Expected identity");
    private static final @NonNull Throwable EXPECTED_ONLINE = new InvalidPlayerIdentifierException(
        "Expected online player");
    private static final @NonNull Throwable INVALID_TYPE = new InvalidPlayerIdentifierException(
        "Unexpected type of identity");

    private final @NonNull Class<I> type;
    private final @NonNull IUserService userService;
    private final @NonNull IBanServer banServer;
    private final @NonNull IDataService dataService;
    private final boolean online;

    private BanIdentityParser(final @NonNull Class<I> type,
        final @NonNull IUserService userService,
        final @NonNull IBanServer banServer,
        final @NonNull IDataService dataService,
        final boolean online) {
      this.type = type;
      this.userService = userService;
      this.banServer = banServer;
      this.dataService = dataService;
      this.online = online;
    }

    @Override
    public @NonNull ArgumentParseResult<I> parse(final @NonNull CommandContext<IBanAudience> commandContext,
        final @NonNull Queue<String> inputQueue) {
      if (commandContext.isSuggestions()) {
        // The result here is irrelevant, as long as it's present.
        return ArgumentParseResult.failure(SUGGESTIONS_FAILURE);
      }

      final String input = inputQueue.peek();
      if (input == null) {
        return ArgumentParseResult.failure(EXPECTED_ANY);
      }

      if (input.length() < 3) {
        // Too short for a player name and IP address.
        return ArgumentParseResult.failure(EXPECTED_ANY);
      }

      // We have to accept IPv6 addresses to just fall-through.
      // IPv4 addresses can be max 15 characters long, therefore aren't necessary to care about.
      if (input.length() > 16 && input.indexOf(':') == -1) {
        if (!UuidIdentity.class.isAssignableFrom(this.type)) {
          return ArgumentParseResult.failure(INVALID_TYPE);
        }

        if (input.length() != 32 && input.length() != 36) {
          // Neither UUID without dashes nor with dashes.
          return ArgumentParseResult.failure(EXPECTED_ANY);
        }

        final UUID uuid;
        try {
          uuid = UUID.fromString(input);
        } catch (final IllegalArgumentException ignored) {
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Invalid UUID '" + input + "'"));
        }

        if (this.online && this.banServer.audienceOf(uuid) == null) {
          // The player isn't online, yet is required to be.
          return ArgumentParseResult.failure(EXPECTED_ONLINE);
        }

        return this.userService.getUser(uuid)
            .join() // We want the exceptions.
            .map(identity -> {
              inputQueue.remove();
              return ArgumentParseResult.success(this.type.cast(identity));
            })
            .orElseGet(() -> ArgumentParseResult.failure(
                new InvalidPlayerIdentifierException("Invalid UUID '" + input + "'")));
      }

      if (!IpIdentity.class.isAssignableFrom(this.type)
          && !UuidIdentity.class.isAssignableFrom(this.type)) {
        return ArgumentParseResult.failure(INVALID_TYPE);
      }

      // Alright, we now need to figure out if this is an IP address.
      // No `.` in usernames, but they are required in IPv4 addresses.
      // No `:` in usernames, but they are required in IPv6 addresses.
      if (input.indexOf('.') != -1 || input.indexOf(':') != -1) {
        // This is an IP address.
        if (!IpIdentity.class.isAssignableFrom(this.type)) {
          return ArgumentParseResult.failure(INVALID_TYPE);
        }

        final InetAddress address;
        try {
          //noinspection UnstableApiUsage
          address = InetAddresses.forString(input);
        } catch (final IllegalArgumentException ignored) {
          return ArgumentParseResult.failure(new InvalidPlayerIdentifierException("Invalid IP '" + input + "'"));
        }

        final IpIdentity identity = this.dataService.createIdentity(address);
        inputQueue.remove();

        return ArgumentParseResult.success(this.type.cast(identity));
      } else {
        // This is a username.
        if (!UuidIdentity.class.isAssignableFrom(this.type)) {
          return ArgumentParseResult.failure(INVALID_TYPE);
        }

        if (this.online && this.banServer.audienceOf(input) == null) {
          // The player isn't online, yet is required to be.
          return ArgumentParseResult.failure(EXPECTED_ONLINE);
        }

        return this.userService.getUser(input)
            .join() // We want the exception
            .map(identity -> {
              inputQueue.remove();
              return ArgumentParseResult.success(this.type.cast(identity));
            })
            .orElseGet(() -> ArgumentParseResult.failure(
                new InvalidPlayerIdentifierException("Invalid username '" + input + "'")));
      }
    }

    @Override
    public @NonNull List<String> suggestions(final @NonNull CommandContext<IBanAudience> commandContext,
        final @NonNull String input) {
      final String lowercaseInput = input.toLowerCase(Locale.ENGLISH).trim();
      final ImmutableList.Builder<String> builder = ImmutableList.builder();

      for (final IBanAudience player : this.banServer.onlineAudiences()) {
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

    private InvalidPlayerIdentifierException(final @NonNull String message) {
      super(message);
    }

    @Override
    public synchronized @This @NonNull Throwable fillInStackTrace() {
      return this;
    }
  }
}
