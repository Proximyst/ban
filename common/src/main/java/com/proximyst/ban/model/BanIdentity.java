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

package com.proximyst.ban.model;

import com.google.inject.assistedinject.Assisted;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.sql.IdentityType;
import com.proximyst.ban.model.sql.IpAddressType;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.platform.IBanAudience.IBanConsole;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.service.IDataService;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class BanIdentity {
  private final long id;

  private BanIdentity(final long id) {
    this.id = id;
  }

  /**
   * @return Whether this identity directly represents a player.
   */
  public abstract boolean isPlayer();

  /**
   * @return Whether this identity represents a collection of players.
   */
  public abstract boolean isCollectionOfPlayers();

  /**
   * @return The type of this identity.
   */
  public abstract @NonNull IdentityType type();

  /**
   * @return The currently possible audiences for this identity.
   */
  public abstract @NonNull CompletableFuture<@NonNull Iterable<@NonNull IBanAudience>> audiences();

  /**
   * @return A single {@link Audience} to refer to all audiences this identity may represent.
   */
  public abstract @NonNull CompletableFuture<@NonNull Audience> audience();

  /**
   * @return The ID of this identity.
   */
  public long getId() {
    return this.id;
  }

  /**
   * @return An {@link Optional} of this instance as a {@link UuidIdentity}, if this is one.
   */
  public final @NonNull Optional<@NonNull UuidIdentity> asUuidIdentity() {
    if (this instanceof UuidIdentity) {
      return Optional.of((UuidIdentity) this);
    }

    return Optional.empty();
  }

  /**
   * @return An {@link Optional} of this instance as an {@link IpIdentity}, if this is one.
   */
  public final @NonNull Optional<@NonNull IpIdentity> asIpIdentity() {
    if (this instanceof IpIdentity) {
      return Optional.of((IpIdentity) this);
    }

    return Optional.empty();
  }

  public static class UuidIdentity extends BanIdentity {
    private final @NonNull UUID uuid;
    private final @NonNull String username;
    private final @NonNull IBanServer banServer;

    @Inject
    UuidIdentity(final @Assisted long id,
        final @Assisted @NonNull UUID uuid,
        final @Assisted @NonNull String username,
        final @NonNull IBanServer banServer) {
      super(id);
      this.uuid = uuid;
      this.username = username;
      this.banServer = banServer;
    }

    @Override
    public boolean isPlayer() {
      return true;
    }

    @Override
    public boolean isCollectionOfPlayers() {
      return false;
    }

    @Override
    public @NonNull IdentityType type() {
      return IdentityType.UUID;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull IBanAudience>> audiences() {
      final IBanAudience audience = this.banServer.audienceOf(this.uuid);
      if (audience == null) {
        return CompletableFuture.completedFuture(List.of());
      } else {
        return CompletableFuture.completedFuture(List.of(audience));
      }
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Audience> audience() {
      final IBanAudience audience = this.banServer.audienceOf(this.uuid);
      return CompletableFuture.completedFuture(Objects.requireNonNullElseGet(audience, Audience::empty));
    }

    public @NonNull UUID uuid() {
      return this.uuid;
    }

    public @NonNull String username() {
      return this.username;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      return this == o || o instanceof BanIdentity && this.getId() == ((BanIdentity) o).getId();
    }

    @Override
    public int hashCode() {
      return Long.hashCode(this.getId());
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", this.getId())
          .append("uuid", this.uuid())
          .append("username", this.username())
          .toString();
    }
  }

  public static final class IpIdentity extends BanIdentity {
    private final @NonNull InetAddress address;
    private final @NonNull IDataService dataService;
    private final @NonNull IBanServer banServer;
    private final @NonNull Executor executor;

    @Inject
    IpIdentity(final @Assisted long id,
        final @Assisted @NonNull InetAddress address,
        final @NonNull IDataService dataService,
        final @NonNull IBanServer banServer,
        final @NonNull @BanAsyncExecutor Executor executor) {
      super(id);
      this.address = address;
      this.dataService = dataService;
      this.banServer = banServer;
      this.executor = executor;
    }

    @Override
    public boolean isPlayer() {
      return false;
    }

    @Override
    public boolean isCollectionOfPlayers() {
      return true;
    }

    @Override
    public @NonNull IdentityType type() {
      return this.address instanceof Inet4Address
          ? IdentityType.IPV4
          : IdentityType.IPV6;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull IBanAudience>> audiences() {
      return CompletableFuture.supplyAsync(() -> this.dataService.getUsersByIp(this.address), this.executor)
          .thenApply(users -> users.stream()
              .map(user -> this.banServer.audienceOf(user.uuid()))
              .filter(Objects::nonNull)
              .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Audience> audience() {
      return this.audiences().thenApply(Audience::audience);
    }

    public @NonNull InetAddress address() {
      return this.address;
    }

    public @NonNull IpAddressType ipType() {
      if (this.address instanceof Inet4Address) {
        return IpAddressType.IPV4;
      } else if (this.address instanceof Inet6Address) {
        return IpAddressType.IPV6;
      }

      throw new IllegalStateException("Unknown InetAddress type: " + this.address.getClass().getName());
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      return this == o || o instanceof BanIdentity && this.getId() == ((BanIdentity) o).getId();
    }

    @Override
    public int hashCode() {
      return Long.hashCode(this.getId());
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", this.getId())
          .append("address", this.address().getHostAddress())
          .append("type", this.ipType())
          .toString();
    }
  }

  @Singleton
  public static final class ConsoleIdentity extends UuidIdentity {
    private final @NonNull IBanAudience audience;

    @Inject
    ConsoleIdentity(final @NonNull IBanServer banServer) {
      super(0, IBanConsole.UUID, IBanConsole.USERNAME, banServer);
      this.audience = banServer.consoleAudience();
    }

    @Override
    public boolean isPlayer() {
      return false;
    }

    @Override
    public boolean isCollectionOfPlayers() {
      return false;
    }

    @Override
    public @NonNull IdentityType type() {
      return IdentityType.CONSOLE;
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Iterable<@NonNull IBanAudience>> audiences() {
      return CompletableFuture.completedFuture(List.of(this.audience));
    }

    @Override
    public @NonNull CompletableFuture<@NonNull Audience> audience() {
      return CompletableFuture.completedFuture(this.audience);
    }

    public @NonNull IBanAudience directAudience() {
      return this.audience;
    }

    public @NonNull String name() {
      return IBanConsole.USERNAME;
    }

    public @NonNull UUID uuid() {
      return IBanConsole.UUID;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      return o instanceof BanIdentity;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public String toString() {
      return "ConsoleIdentity";
    }
  }
}
