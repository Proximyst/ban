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

package com.proximyst.ban.service.impl;

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.ban.service.IPunishmentService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplPunishmentService implements IPunishmentService {
  private final @NonNull IDataService dataService;
  private final @NonNull IMessageService messageService;
  private final @NonNull Executor executor;

  @Inject
  ImplPunishmentService(final @NonNull IDataService dataService,
      final @NonNull IMessageService messageService,
      final @NonNull @BanAsyncExecutor Executor executor) {
    this.dataService = dataService;
    this.messageService = messageService;
    this.executor = executor;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(
      final @NonNull BanIdentity identity) {
    return CompletableFuture.supplyAsync(() -> ImmutableList.copyOf(
        this.dataService.getPunishmentsForTarget(identity)), this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Punishment> savePunishment(
      final @NonNull PunishmentBuilder punishmentBuilder) {
    return CompletableFuture.supplyAsync(() -> this.dataService.savePunishment(punishmentBuilder), this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> applyPunishment(final @NonNull Punishment punishment) {
    if (!punishment.getPunishmentType().isApplicable()
        || punishment.getPunishmentType().canBeLifted() && punishment.isLifted()) {
      return CompletableFuture.completedFuture(null);
    }

    return punishment.getTarget().audiences()
        .thenAccept(target -> {
          switch (punishment.getPunishmentType()) {
            case KICK:
            case BAN: {
              final Component reason = punishment.applicationMessage(this.messageService);
              target.forEach(audience -> audience.disconnect(reason));
              break;
            }
            case MUTE:
            case WARNING: {
              final Component reason = punishment.applicationMessage(this.messageService);
              target.forEach(audience -> audience.sendMessage(reason));
              break;
            }

            case NOTE:
              // Fall-through
            default:
          }
        });
  }

  @Override
  public @NonNull CompletableFuture<@NonNull Punishment> liftPunishment(final @NonNull Punishment punishment,
      final @Nullable UUID liftedBy) {
    return CompletableFuture.supplyAsync(() -> this.dataService.liftPunishment(punishment, liftedBy), this.executor);
  }

  @Override
  public void announcePunishment(final @NonNull Punishment punishment) {
    switch (punishment.getPunishmentType()) {
      case BAN:
        if (punishment.currentlyApplies() && punishment.getReason().isPresent()) {
          this.messageService.broadcastsReasonedBan(punishment);
        } else if (punishment.currentlyApplies()) {
          this.messageService.broadcastsReasonlessBan(punishment);
        } else {
          this.messageService.broadcastsUnban(punishment);
        }
        break;
      case KICK:
        if (punishment.getReason().isPresent()) {
          this.messageService.broadcastsReasonedKick(punishment);
        } else {
          this.messageService.broadcastsReasonlessKick(punishment);
        }
        break;
      case MUTE:
        if (punishment.currentlyApplies() && punishment.getReason().isPresent()) {
          this.messageService.broadcastsReasonedMute(punishment);
        } else if (punishment.currentlyApplies()) {
          this.messageService.broadcastsReasonlessMute(punishment);
        } else {
          this.messageService.broadcastsUnmute(punishment);
        }
        break;
      case WARNING:
        if (punishment.getReason().isPresent()) {
          this.messageService.broadcastsReasonedWarn(punishment);
        } else {
          this.messageService.broadcastsReasonlessWarn(punishment);
        }
        break;

      case NOTE:
        // Fall-through
      default:
    }
  }
}
