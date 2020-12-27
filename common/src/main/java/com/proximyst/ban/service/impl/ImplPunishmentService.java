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

package com.proximyst.ban.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.service.MessageService;
import com.proximyst.ban.utils.ThrowableUtils;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplPunishmentService implements IPunishmentService {
  private static final int MAXIMUM_PUNISHMENT_CACHE_CAPACITY =
      Integer.getInteger("ban.maximumPunishmentCacheCapacity", 512);

  private final @NonNull IDataService dataService;
  private final @NonNull MessageService messageService;
  private final @NonNull Executor executor;
  private final @NonNull IBanServer banServer;

  private final @NonNull Cache<@NonNull UUID, @NonNull List<@NonNull Punishment>> punishmentCache =
      CacheBuilder.newBuilder()
          .initialCapacity(512)
          .maximumSize(MAXIMUM_PUNISHMENT_CACHE_CAPACITY)
          .expireAfterAccess(5, TimeUnit.MINUTES)
          .build();

  @Inject
  public ImplPunishmentService(final @NonNull IDataService dataService,
      final @NonNull MessageService messageService,
      final @NonNull @BanAsyncExecutor Executor executor,
      final @NonNull IBanServer banServer) {
    this.dataService = dataService;
    this.messageService = messageService;
    this.executor = executor;
    this.banServer = banServer;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(
      final @NonNull UUID target) {
    final List<Punishment> existingPunishments = this.punishmentCache.getIfPresent(target);
    if (existingPunishments != null) {
      return CompletableFuture.completedFuture(ImmutableList.copyOf(existingPunishments));
    }

    // Okay, we've got to load them and cache them thereafter.
    return ThrowableUtils.supplyAsyncSneaky(() -> ImmutableList.copyOf(this.punishmentCache
            .get(target, () -> this.dataService.getPunishmentsForTarget(target))),
        this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> savePunishment(final @NonNull Punishment punishment) {
    return this.getPunishments(punishment.getTarget()) // Ensure we have their punishments loaded
        .thenComposeAsync($ -> {
          this.punishmentCache.asMap().compute(punishment.getTarget(), (uuid, list) -> {
            if (list == null) {
              return Lists.newArrayList(punishment);
            }

            if (!list.contains(punishment)) { // Referential comparison
              list.add(punishment);
            }
            return list;
          });

          this.dataService.savePunishment(punishment);
          return CompletableFuture.completedFuture(null);
        }, this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> applyPunishment(final @NonNull Punishment punishment) {
    if (!punishment.getPunishmentType().isApplicable()) {
      return CompletableFuture.completedFuture(null);
    }

    final IBanAudience target = this.banServer
        .audienceOf(punishment.getTarget());
    if (target == null) {
      // We have no-one to apply the punishment on.
      return CompletableFuture.completedFuture(null);
    }

    final String bypassPermission = punishment.getPunishmentType().getBypassPermission().orElse(null);
    if (bypassPermission != null && target.hasPermission(bypassPermission)) {
      // Don't apply the punishment; they can bypass it.
      return CompletableFuture.completedFuture(null);
    }

    return this.messageService.punishmentMessage(
        punishment
            .getPunishmentType()
            .getApplicationMessage(punishment.getReason().isPresent())
            .orElseThrow(() -> new IllegalStateException("Applicable punishment without punishment type")),
        punishment)
        .component()
        .thenAccept(component -> {
          switch (punishment.getPunishmentType()) {
            case BAN:
              // Fall through.
            case KICK:
              target.disconnect(component);
              break;

            case MUTE:
              // Fall through.
            default:
              target.sendMessage(Identity.nil(), component);
              break;
          }
        });
  }
}
