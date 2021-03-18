package com.proximyst.ban.message;

import com.google.common.collect.Multimap;
import com.proximyst.moonshine.component.placeholder.IPlaceholderResolver;
import com.proximyst.moonshine.component.placeholder.PlaceholderContext;
import com.proximyst.moonshine.component.placeholder.ResolveResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DatePlaceholderResolver<R> implements IPlaceholderResolver<R, Date> {
  @Override
  public ResolveResult resolve(final String placeholderName, final Date value, final PlaceholderContext<R> ctx,
      final Multimap<String, @Nullable Object> flags) {
    return ResolveResult.ok(placeholderName, SimpleDateFormat.getDateTimeInstance().format(value));
  }
}
