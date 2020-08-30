package com.proximyst.ban.event.subscriber;

import com.google.inject.Inject;
import com.proximyst.ban.config.MessagesConfig;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.manager.PunishmentManager;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.LoadableData;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.time4j.ClockUnit;
import net.time4j.PrettyTime;
import net.time4j.format.TextWidth;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MutedPlayerChatSubscriber {
  @NonNull
  private final PunishmentManager manager;

  @NonNull
  private final MessagesConfig messagesConfig;

  @NonNull
  private final IMojangApi mojangApi;

  @Inject
  public MutedPlayerChatSubscriber(
      @NonNull PunishmentManager manager,
      @NonNull MessagesConfig messagesConfig,
      @NonNull IMojangApi mojangApi
  ) {
    this.manager = manager;
    this.messagesConfig = messagesConfig;
    this.mojangApi = mojangApi;
  }

  @Subscribe
  public void onChat(PlayerChatEvent event) {
    manager.getActiveMute(event.getPlayer().getUniqueId())
        .join() // This *should* be fast, and only on one player's connection thread
        .ifPresent(mute -> {
          event.setResult(PlayerChatEvent.ChatResult.denied());
          event.getPlayer().sendMessage(
              MiniMessage.get()
                  .parse(
                      mute.getReason()
                          .map($ -> messagesConfig.getMuteMessageReason())
                          .orElse(messagesConfig.getMuteMessageReasonless()),

                      "reason", mute.getReason().orElse(""),
                      "duration", PrettyTime.of(event.getPlayer().getPlayerSettings().getLocale())
                          .print(mute.getExpiration() - System.currentTimeMillis(), ClockUnit.MILLIS, TextWidth.SHORT),
                      "punisher", mojangApi.getUser(mute.getPunisher())
                          .map(BanUser::getUsername)
                          .map(LoadableData::getAndLoad)
                          // Still only on one player's connection thread, and they're not welcome anyways
                          .flatMap(CompletableFuture::join)
                          .orElse("Unknown")
                  )
          );
        });
  }
}
