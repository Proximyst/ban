package com.proximyst.ban.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .initialCapacity(512)
      .maximumSize(512)
      .build(CacheLoader.from(uuid -> {
        Objects.requireNonNull(uuid, "uuid must not be null");
        return getDataInterface().getPunishmentsForTarget(uuid);
      }));

  @NonNull
  private final SewerSystem<@NonNull PunishmentBuilder, @NonNull Punishment> addPunishmentPipeline = SewerSystem
      .builder("build", PunishmentBuilder::build, null,
          punishment ->
              getMain().getProxyServer().getEventManager().fire(new PunishmentAddedEvent(punishment))
                  .join()
                  .getResult()
                  .isAllowed()
      )
      .pipe("push to sql", punishment -> {
        getDataInterface().addPunishment(punishment);
        return punishment;
      })
      .pipe("caching", punishment -> {
        punishmentCache.asMap().compute(punishment.getTarget(), (uuid, list) -> {
          if (list == null) {
            return Lists.newArrayList(punishment);
          } else {
            list.add(punishment);
            return list;
          }
        });
        return punishment;
      })
      .build();

  @NonNull
  private final SewerSystem<@NonNull UUID, @NonNull ImmutableList<@NonNull Punishment>> retrievePunishmentsPipeline = SewerSystem
      .<UUID, List<Punishment>>builder("fetch from cache", punishmentCache::get)
      .<ImmutableList<Punishment>>pipe("immutablelist",
          list -> list == null ? ImmutableList.of() : ImmutableList.copyOf(list))
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
    return retrievePunishmentsPipeline.pumpAsync(target, getMain().getSchedulerExecutor())
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
  public CompletableFuture<Optional<Punishment>> addPunishment(@NonNull PunishmentBuilder builder) {
    return addPunishmentPipeline.pumpAsync(builder, getMain().getSchedulerExecutor())
        .thenApply(result -> {
          if (result.isExceptional()) {
            return Optional.empty();
          }

          return Optional.of(result.asSuccess().getResult());
        });
  }

  @NonNull
  private BanPlugin getMain() {
    return main;
  }

  @NonNull
  private IDataInterface getDataInterface() {
    return main.getDataInterface();
  }
}
