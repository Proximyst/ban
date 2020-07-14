package com.proximyst.ban.data;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.proximyst.ban.event.event.PunishmentAddedEvent;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.ThrowableUtils;
import com.velocitypowered.api.event.EventManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

@Singleton
public final class PunishmentManager {
  @NonNull
  private final IDataInterface dataInterface;

  @NonNull
  private final Logger logger;

  @NonNull
  private final EventManager eventManager;

  private final LoadingCache<UUID, List<Punishment>> punishmentCache = CacheBuilder.newBuilder()
      .expireAfterAccess(1, TimeUnit.MINUTES)
      .initialCapacity(512)
      .maximumSize(512)
      .build(CacheLoader.from(uuid -> {
        try {
          return getDataInterface().getPunishmentsForTarget(uuid);
        } catch (SQLException ex) {
          ThrowableUtils.sneakyThrow(ex);
          throw new RuntimeException();
        }
      }));

  public PunishmentManager(
      @NonNull IDataInterface dataInterface,
      @NonNull Logger logger,
      @NonNull EventManager eventManager
  ) {
    this.dataInterface = dataInterface;
    this.logger = logger;
    this.eventManager = eventManager;
  }

  public void addPunishment(@NonNull Punishment punishment) {
    eventManager.fire(new PunishmentAddedEvent(punishment)).thenAcceptAsync(event -> {
      if (!event.getResult().isAllowed()) {
        logger.info("Punishment on {} by {} was denied.", punishment.getTarget(), punishment.getPunisher());
        return;
      }

      punishmentCache.asMap().compute(punishment.getTarget(), (uuid, list) -> {
        if (list == null) {
          return Lists.newArrayList(punishment);
        } else {
          list.add(punishment);
          return list;
        }
      });
    });
  }

  /**
   * @param target The target whose punishments are requested.
   * @return An immutable copy of the punishments of the player where order is not guaranteed.
   */
  @NonNull
  public ImmutableList<Punishment> getPunishments(@NonNull UUID target) {
    try {
      List<Punishment> list = punishmentCache.get(target);
      if (list != null) {
        return ImmutableList.copyOf(list);
      }
    } catch (ExecutionException ex) {
      ThrowableUtils.sneakyThrow(ex);
      throw new RuntimeException();
    }

    return ImmutableList.of();
  }

  @NonNull
  public Optional<Punishment> getActiveBan(@NonNull UUID target) {
    return getPunishments(target)
        .stream()
        .filter(punishment -> punishment.getPunishmentType() == PunishmentType.BAN
            && punishment.currentlyApplies())
        .max(Comparator.comparingLong(Punishment::getTime));
  }

  @NonNull
  public Optional<Punishment> getActiveMute(@NonNull UUID target) {
    return getPunishments(target)
        .stream()
        .filter(punishment -> punishment.getPunishmentType() == PunishmentType.MUTE
            && punishment.currentlyApplies())
        .max(Comparator.comparingLong(Punishment::getTime));
  }

  @NonNull
  private IDataInterface getDataInterface() {
    return dataInterface;
  }
}
