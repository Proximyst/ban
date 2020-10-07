package com.proximyst.ban.commands.cloud;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import com.google.common.collect.ImmutableList;
import com.proximyst.ban.BanPlugin;
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

public final class PlayerArgument<C> extends CommandArgument<C, Player> {
  private static final TypeToken<Player> PLAYER_TYPE_TOKEN = TypeToken.get(Player.class);

  private PlayerArgument(
      final boolean required,
      final @NonNull String name,
      final @NonNull String defaultValue,
      final @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
      final @NonNull BanPlugin banPlugin
  ) {
    super(
        required,
        name,
        new PlayerParser<>(banPlugin.getProxyServer()),
        defaultValue,
        PLAYER_TYPE_TOKEN,
        suggestionsProvider
    );
  }

  public static <@NonNull C> @NonNull Builder<C> newBuilder(
      final @NonNull String name,
      final @NonNull BanPlugin banPlugin
  ) {
    return new PlayerArgument.Builder<>(name, banPlugin);
  }

  public static <@NonNull C> @NonNull CommandArgument<C, @NonNull Player> of(
      final @NonNull String name,
      final @NonNull BanPlugin plugin
  ) {
    return PlayerArgument.<C>newBuilder(name, plugin).asRequired().build();
  }

  public static <@NonNull C> @NonNull CommandArgument<C, @NonNull Player> optional(
      final @NonNull String name,
      final @NonNull BanPlugin plugin
  ) {
    return PlayerArgument.<C>newBuilder(name, plugin).asOptional().build();
  }

  public static final class Builder<C> extends CommandArgument.Builder<C, Player> {
    private final @NonNull BanPlugin banPlugin;

    public Builder(
        final @NonNull String name,
        final @NonNull BanPlugin banPlugin
    ) {
      super(PLAYER_TYPE_TOKEN, name);
      this.banPlugin = banPlugin;
    }

    @Override
    public @NonNull PlayerArgument<C> build() {
      return new PlayerArgument<>(
          this.isRequired(),
          this.getName(),
          this.getDefaultValue(),
          this.getSuggestionsProvider(),
          this.banPlugin
      );
    }
  }

  public static final class PlayerParser<C> implements ArgumentParser<C, Player> {
    private final @NonNull ProxyServer proxyServer;

    public PlayerParser(final @NonNull ProxyServer proxyServer) {
      this.proxyServer = proxyServer;
    }

    @Override
    public @NonNull ArgumentParseResult<Player> parse(
        final @NonNull CommandContext<C> commandContext,
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
          //noinspection ResultOfMethodCallIgnored
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
