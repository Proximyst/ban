package com.proximyst.ban.utils;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
  void accept(@NonNull T input) throws E;

  default Consumer<T> toConsumer() {
    return input -> {
      try {
        this.accept(input);
      } catch (Exception ex) {
        ThrowableUtils.sneakyThrow(ex);
      }
    };
  }
}
