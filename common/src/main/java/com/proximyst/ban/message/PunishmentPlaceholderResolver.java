package com.proximyst.ban.message;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.service.IMessageService;
import com.proximyst.moonshine.component.placeholder.IPlaceholderResolver;
import com.proximyst.moonshine.component.placeholder.PlaceholderContext;
import com.proximyst.moonshine.component.placeholder.ResolveResult;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class PunishmentPlaceholderResolver<R> implements IPlaceholderResolver<R, Punishment> {
  private final @NonNull Provider<@NonNull IMessageService> messageServiceProvider;

  @Inject
  PunishmentPlaceholderResolver(final @NonNull Provider<@NonNull IMessageService> messageServiceProvider) {
    this.messageServiceProvider = messageServiceProvider;
  }

  @Override
  public ResolveResult resolve(final String placeholderName, final Punishment value, final PlaceholderContext<R> ctx,
      final Multimap<String, @Nullable Object> flags) {
    return ResolveResult.ok(ImmutableMap.<String, Object>builder()
        .put(placeholderName + "Id", value.getId())
        .put(placeholderName + "Date", value.getDate())
        .put(placeholderName + "Duration", value.getDuration() == 0
            ? this.messageServiceProvider.get().formattingPermanently()
            : this.messageServiceProvider.get().formattingDuration(
                DurationFormatUtils.formatDurationWords(value.getDuration(), true, true)))
        .put(placeholderName + "Expiration", value.getExpirationDate().map(Object.class::cast)
            .orElseGet(this.messageServiceProvider.get()::formattingNever))
        .put(placeholderName + "Verb", this.messageServiceProvider.get().formattingVerbsPast(value.getPunishmentType()))
        .put(placeholderName + "Punisher", value.getPunisher())
        .put(placeholderName + "Target", value.getTarget())
        .put(placeholderName + "Lifted", value.isLifted())
        .put(placeholderName + "LiftedBy", value.getLiftedBy().map(UUID::toString).orElse(""))
        .put(placeholderName + "Applies", value.currentlyApplies())
        .build());
  }
}
