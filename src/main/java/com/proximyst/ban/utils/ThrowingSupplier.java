package com.proximyst.ban.utils;

import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception> {
  @NonNull
  T get() throws E;

  default Supplier<T> toSupplier() {
    return () -> {
      try {
        return this.get();
      } catch (Exception ex) {
        ThrowableUtils.sneakyThrow(ex);
        throw new RuntimeException();
      }
    };
  }
}
