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

package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.proximyst.ban.message.BanIdentityPlaceholderResolver;
import com.proximyst.ban.message.BanMessageParser;
import com.proximyst.ban.message.BanMessageSender;
import com.proximyst.ban.message.BanMessageSource;
import com.proximyst.ban.message.CompletableFuturePlaceholderResolver;
import com.proximyst.ban.message.ComponentPlaceholderResolver;
import com.proximyst.ban.message.ServerReceiverResolver;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IMojangService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.IUserService;
import com.proximyst.ban.service.impl.ImplAshconMojangService;
import com.proximyst.ban.service.impl.ImplGenericSqlDataService;
import com.proximyst.ban.service.impl.ImplPunishmentService;
import com.proximyst.ban.service.impl.ImplUserService;
import com.proximyst.moonshine.Moonshine;
import java.util.concurrent.CompletableFuture;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ServiceModule extends AbstractModule {
  // TODO(Proximyst): Support non-SQL data?
  @Provides
  @Singleton
  @NonNull IDataService dataService(final @NonNull ImplGenericSqlDataService sqlDataService) {
    return sqlDataService;
  }

  // TODO(Proximyst): Support official Mojang API
  @Provides
  @Singleton
  @NonNull IMojangService mojangService(final @NonNull ImplAshconMojangService ashconMojangService) {
    return ashconMojangService;
  }

  @Provides
  @Singleton
  @NonNull IPunishmentService punishmentService(final @NonNull ImplPunishmentService punishmentService) {
    return punishmentService;
  }

  @Provides
  @Singleton
  @NonNull IUserService userService(final @NonNull ImplUserService userService) {
    return userService;
  }

  @Provides
  @Singleton
  @NonNull IMessageService messageService(final @NonNull ServerReceiverResolver serverReceiverResolver,
      final @NonNull CompletableFuturePlaceholderResolver<Audience> completableFuturePlaceholderResolver,
      final @NonNull ComponentPlaceholderResolver<Audience> componentPlaceholderResolver,
      final @NonNull BanIdentityPlaceholderResolver<Audience> banIdentityPlaceholderResolver,
      final @NonNull BanMessageSource banMessageSource,
      final @NonNull BanMessageParser banMessageParser,
      final @NonNull BanMessageSender banMessageSender) {
    return Moonshine.<Audience>builder()
        .receiver(serverReceiverResolver)
        .placeholder(CompletableFuture.class, completableFuturePlaceholderResolver)
        .placeholder(Component.class, componentPlaceholderResolver)
        .placeholder(BanIdentity.class, banIdentityPlaceholderResolver)
        .source(banMessageSource)
        .parser(banMessageParser)
        .sender(banMessageSender)
        .create(IMessageService.class, this.getClass().getClassLoader());
  }
}
