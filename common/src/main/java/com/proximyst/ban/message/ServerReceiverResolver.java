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

package com.proximyst.ban.message;

import com.google.common.collect.Streams;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.moonshine.component.receiver.IReceiver;
import com.proximyst.moonshine.component.receiver.IReceiverResolver;
import com.proximyst.moonshine.component.receiver.ReceiverContext;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ServerReceiverResolver implements IReceiverResolver<Audience> {
  private final @NonNull IBanServer banServer;

  @Inject
  ServerReceiverResolver(final @NonNull IBanServer banServer) {
    this.banServer = banServer;
  }

  @Override
  public Optional<IReceiver<Audience>> resolve(final Method method) {
    final ServerReceiver annotation = method.getAnnotation(ServerReceiver.class);
    if (annotation == null) {
      return Optional.empty();
    }

    return Optional.of(new Resolver(this.banServer, annotation.permission()));
  }

  private static class Resolver implements IReceiver<Audience> {
    private final @NonNull IBanServer banServer;
    private final @Nullable String permission;

    private Resolver(final @NonNull IBanServer banServer,
        final @Nullable String permission) {
      this.banServer = banServer;
      this.permission = permission;
    }

    @Override
    public Audience find(final ReceiverContext ctx) {
      if (this.permission == null) {
        return this.banServer;
      }

      return Audience.audience(Stream.concat(Stream.of(this.banServer.consoleAudience()),
          Streams.stream(this.banServer.onlineAudiences()).filter(audience -> audience.hasPermission(this.permission)))
          .collect(Collectors.toList()));
    }
  }
}
