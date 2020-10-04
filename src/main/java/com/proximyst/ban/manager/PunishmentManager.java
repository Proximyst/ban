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
import com.proximyst.ban.utils.ThrowableUtils;
import com.proximyst.sewer.Module;
import com.proximyst.sewer.SewerSystem;
import com.proximyst.sewer.piping.FilteredResult;
import com.proximyst.sewer.piping.NamedPipeResult;
import com.proximyst.sewer.piping.PipeResult;
import com.proximyst.sewer.piping.SuccessfulResult;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class PunishmentManager {
  private final @NonNull BanPlugin main;

  private final @NonNull LoadingCache<UUID, @NonNull List<@NonNull Punishment>> punishmentCache = CacheBuilder
      .newBuilder()
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .initialCapacity(512)
      .removalListener((RemovalListener<UUID, List<Punishment>>) notification -> {
        // Recache punishments of online players.
        if (this.getMain().getProxyServer().getPlayer(notification.getKey()).isPresent()) {
          this.getPunishmentCache().put(notification.getKey(), notification.getValue());
        }
      })
      .build(CacheLoader.from(uuid -> {
        Objects.requireNonNull(uuid, "uuid must not be null");
        return this.getDataInterface().getPunishmentsForTarget(uuid);
      }));

  private final @NonNull SewerSystem<@NonNull PunishmentBuilder, @NonNull Punishment> addPunishmentPipeline = SewerSystem
      .builder("build", Module.immediatelyWrapping(PunishmentBuilder::build))
      .module(
          "fire PunishmentAddedEvent",
          // CHECKSTYLE:OFF - FIXME
          punishment -> PunishmentManager.this.getMain().getProxyServer().getEventManager()
              // CHECKSTYLE:ON
              .fire(new PunishmentAddedEvent(punishment))
              .thenApply(event -> {
                if (event.getResult().isAllowed()) {
                  return new SuccessfulResult<>(punishment);
                }

                return new FilteredResult<>();
              }))
      .module("announce", Module.immediatelyWrapping(punishment -> {
        punishment.broadcast(this.getMain());
        return punishment;
      }))
      .module("apply to online player", Module.immediatelyWrapping(punishment -> {
        if (punishment.getPunishmentType().isApplicable()) {
          // TODO(Proximyst): Make this.. not ugly.
          this.getMain().getProxyServer().getPlayer(punishment.getTarget())
              .ifPresent(player -> this.getMain().getMessageManager().formatMessageWith(
                  punishment.getPunishmentType() == PunishmentType.KICK
                      ? (punishment.getReason()
                      .map($ -> this.getMain().getConfiguration().messages.applications.kickReason)
                      .orElse(this.getMain().getConfiguration().messages.applications.kickReasonless))
                      : (punishment.getReason()
                          .map($ -> this.getMain().getConfiguration().messages.applications.banReason)
                          .orElse(this.getMain().getConfiguration().messages.applications.banReasonless)),
                  punishment
              ).thenAccept(player::disconnect));
        }

        return punishment;
      }))
      .module("push to sql", punishment -> {
        this.getMain().getSchedulerExecutor().execute(() -> this.getDataInterface().addPunishment(punishment));
        return CompletableFuture.completedFuture(new SuccessfulResult<>(punishment));
      })
      .module("caching", Module.immediatelyWrapping(punishment -> {
        this.punishmentCache.asMap().compute(punishment.getTarget(), (uuid, list) -> {
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

  private final @NonNull SewerSystem<@NonNull UUID, @NonNull ImmutableList<@NonNull Punishment>> retrievePunishmentsPipeline = SewerSystem
      .<UUID, List<Punishment>>builder("fetch from cache", uuid -> CompletableFuture.supplyAsync(
          // CHECKSTYLE:OFF - FIXME
          () -> {
            try {
              return new SuccessfulResult<>(this.punishmentCache.get(uuid));
            } catch (final ExecutionException ex) {
              ThrowableUtils.sneakyThrow(ex);
              throw new RuntimeException();
            }
          },
          this.getMain().getSchedulerExecutor()
      ))
      // CHECKSTYLE:ON
      .<ImmutableList<Punishment>>module("immutablelist",
          Module.immediatelyWrapping(list -> list == null ? ImmutableList.of() : ImmutableList.copyOf(list)))
      .build();

  public PunishmentManager(@NonNull final BanPlugin main) {
    this.main = main;
  }

  /**
   * @param target The target whose punishments are requested.
   * @return An immutable copy of the punishments of the player where order is not guaranteed.
   */
  @NonNull
  public CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(@NonNull final UUID target) {
    return this.retrievePunishmentsPipeline.pump(target)
        .thenApply(result -> result.asOptional().orElseGet(ImmutableList::of));
  }

  @NonNull
  public CompletableFuture<@NonNull Optional<@NonNull Punishment>> getActiveBan(@NonNull final UUID target) {
    return this.getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.BAN
                && punishment.currentlyApplies(this.main))
            .max(Comparator.comparingLong(Punishment::getTime))
        );
  }

  @NonNull
  public CompletableFuture<@NonNull Optional<@NonNull Punishment>> getActiveMute(@NonNull final UUID target) {
    return this.getPunishments(target)
        .thenApply(list -> list.stream()
            .filter(punishment -> punishment.getPunishmentType() == PunishmentType.MUTE
                && punishment.currentlyApplies(this.main))
            .max(Comparator.comparingLong(Punishment::getTime))
        );
  }

  @NonNull
  public CompletableFuture<@NonNull PipeResult<@NonNull Punishment>> addPunishment(
      @NonNull final PunishmentBuilder builder
  ) {
    // We don't care about the pipe this errs in, so we just do #getResult
    return this.addPunishmentPipeline.pump(builder).thenApply(NamedPipeResult::getResult);
  }

  @NonNull
  private BanPlugin getMain() {
    return this.main;
  }

  @NonNull
  private IDataInterface getDataInterface() {
    return this.getMain().getDataInterface();
  }

  @NonNull
  private LoadingCache<@NonNull UUID, @NonNull List<@NonNull Punishment>> getPunishmentCache() {
    return this.punishmentCache;
  }
}
