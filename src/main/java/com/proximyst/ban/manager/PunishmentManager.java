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

package com.proximyst.ban.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.data.IDataInterface;
import com.proximyst.ban.event.event.PunishmentAddedEvent;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentBuilder;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.sewer.SewerSystem;
import com.proximyst.sewer.piping.ImmediatePipeHandler;
import com.proximyst.sewer.piping.PipeResult;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class PunishmentManager {
  @NonNull
  private final BanPlugin main;

  private final LoadingCache<UUID, List<Punishment>> punishmentCache = CacheBuilder.newBuilder()
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .initialCapacity(512)
      .removalListener((RemovalListener<UUID, List<Punishment>>) notification -> {
        // Recache punishments of online players.
        if (getMain().getProxyServer().getPlayer(notification.getKey()).isPresent()) {
          getPunishmentCache().put(notification.getKey(), notification.getValue());
        }
      })
      .build(CacheLoader.from(uuid -> {
        Objects.requireNonNull(uuid, "uuid must not be null");
        return getDataInterface().getPunishmentsForTarget(uuid);
      }));

  @NonNull
  private final SewerSystem<@NonNull PunishmentBuilder, @NonNull Punishment> addPunishmentPipeline = SewerSystem
      .builder("build", ImmediatePipeHandler.of(PunishmentBuilder::build), null,
          punishment ->
              getMain().getProxyServer().getEventManager().fire(new PunishmentAddedEvent(punishment))
                  .thenApply(event -> event.getResult().isAllowed())
      )
      .pipe("announce", ImmediatePipeHandler.of(punishment -> {
        punishment.broadcast(getMain());
        return punishment;
      }))
      .pipe("apply to online player", ImmediatePipeHandler.of(punishment -> {
        if (punishment.getPunishmentType().isApplicable()) {
          // TODO(Proximyst): Make this.. not ugly.
          getMain().getProxyServer().getPlayer(punishment.getTarget())
              .ifPresent(player -> getMain().getMessageManager().formatMessageWith(
                  punishment.getPunishmentType() == PunishmentType.KICK
                      ? (punishment.getReason().map($ -> getMain().getConfiguration().messages.applications.kickReason)
                      .orElse(getMain().getConfiguration().messages.applications.kickReasonless))
                      : (punishment.getReason().map($ -> getMain().getConfiguration().messages.applications.banReason)
                          .orElse(getMain().getConfiguration().messages.applications.banReasonless)),
                  punishment
              ).thenAccept(player::disconnect));
        }

        return punishment;
      }))
      .pipe("push to sql", ImmediatePipeHandler.of(punishment -> {
        getDataInterface().addPunishment(punishment);
        return punishment;
      }))
      .pipe("caching", ImmediatePipeHandler.of(punishment -> {
        punishmentCache.asMap().compute(punishment.getTarget(), (uuid, list) -> {
          if (list == null) {
            return Lists.newArrayList(punishment);
          } else {
            list.add(punishment);
            return list;
          }
        });
        return punishment;
      }))
      .build();

  @NonNull
  private final SewerSystem<@NonNull UUID, @NonNull ImmutableList<@NonNull Punishment>> retrievePunishmentsPipeline = SewerSystem
      .<UUID, List<Punishment>>builder("fetch from cache", ImmediatePipeHandler.of(punishmentCache::get))
      .<ImmutableList<Punishment>>pipe("immutablelist",
          ImmediatePipeHandler.of(list -> list == null ? ImmutableList.of() : ImmutableList.copyOf(list)))
      .build();

  public PunishmentManager(@NonNull BanPlugin main) {
    this.main = main;
  }

  /**
   * @param target The target whose punishments are requested.
   * @return An immutable copy of the punishments of the player where order is not guaranteed.
   */
  @NonNull
  public CompletableFuture<ImmutableList<Punishment>> getPunishments(@NonNull UUID target) {
    return retrievePunishmentsPipeline.pump(target, getMain().getSchedulerExecutor())
        .thenApply(result -> {
          if (result.isExceptional()) {
            return ImmutableList.of();
          }

          return result.asSuccess().getResult();
        });
  }

  @NonNull
  public CompletableFuture<Optional<Punishment>> getActiveBan(@NonNull UUID target) {
    return getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.BAN
                && punishment.currentlyApplies(main))
            .max(Comparator.comparingLong(Punishment::getTime))
        );
  }

  @NonNull
  public CompletableFuture<Optional<Punishment>> getActiveMute(@NonNull UUID target) {
    return getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.MUTE
                && punishment.currentlyApplies(main))
            .max(Comparator.comparingLong(Punishment::getTime))
        );
  }

  @NonNull
  public CompletableFuture<PipeResult<Punishment>> addPunishment(@NonNull PunishmentBuilder builder) {
    return addPunishmentPipeline.pump(builder, getMain().getSchedulerExecutor());
  }

  @NonNull
  private BanPlugin getMain() {
    return main;
  }

  @NonNull
  private IDataInterface getDataInterface() {
    return getMain().getDataInterface();
  }

  @NonNull
  private LoadingCache<UUID, List<Punishment>> getPunishmentCache() {
    return punishmentCache;
  }
}
