package com.proximyst.ban.message;

import com.google.common.collect.Multimap;
import com.proximyst.moonshine.component.placeholder.IPlaceholderResolver;
import com.proximyst.moonshine.component.placeholder.PlaceholderContext;
import com.proximyst.moonshine.component.placeholder.ResolveResult;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class OptionalPlaceholderResolver<R> implements IPlaceholderResolver<R, Optional> {
  @Override
  public ResolveResult resolve(final String placeholderName, final Optional value, final PlaceholderContext<R> ctx,
      final Multimap<String, @Nullable Object> flags) {
    if (value.isPresent()) {
      return ResolveResult.ok(placeholderName, value.get());
    }

    return ResolveResult.finished(placeholderName, "");
  }
}
