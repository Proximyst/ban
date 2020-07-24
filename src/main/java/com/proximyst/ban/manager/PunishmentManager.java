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
import com.proximyst.ban.model.PunishmentType;
import com.proximyst.ban.utils.ThrowableUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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

  public PunishmentManager(@NonNull BanPlugin main) {
    this.main = main;
  }

  public void addPunishment(@NonNull Punishment punishment) {
    main.getProxyServer().getEventManager().fire(new PunishmentAddedEvent(punishment))
        .thenAcceptAsync(event -> {
          if (!event.getResult().isAllowed()) {
            main.getLogger().info(
                "Punishment on {} by {} was denied.",
                punishment.getTarget(),
                punishment.getPunisher()
            );
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

          try {
            getDataInterface().addPunishment(punishment);
          } catch (Exception ex) {
            main.getLogger().error("Could not save punishment", ex);
          }

          punishment.broadcast(main)
              .thenAccept(success -> {
                if (!success) {
                  main.getLogger().info("Punishment was unsuccessful in broadcasting: " + punishment);
                }
              });
        }, main.getSchedulerExecutor());
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
    return main.getDataInterface();
  }
}
