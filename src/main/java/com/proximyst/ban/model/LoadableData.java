package com.proximyst.ban.model;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LoadableData<T> {
  private final Supplier<T> dataSupplier;

  private CompletableFuture<T> data = null;
  private boolean loaded = false;

  public LoadableData(T data) {
    this.loaded = true;
    this.data = CompletableFuture.completedFuture(data);
    this.dataSupplier = () -> data;
  }

  public LoadableData(@NonNull Supplier<T> loader) {
    this.dataSupplier = loader;
  }

  public LoadableData(@NonNull CompletableFuture<T> data) {
    this.dataSupplier = null;
    this.loaded = true;
    this.data = data;
  }

  public Optional<T> getIfPresent() {
    if (loaded) {
      return Optional.ofNullable(data.join());
    }

    return Optional.empty();
  }

  public CompletableFuture<Optional<T>> getAndLoad() {
    if (loaded) {
      // Immutable copy of result.
      return CompletableFuture.completedFuture(Optional.ofNullable(data.join()));
    }

    data = CompletableFuture.supplyAsync(dataSupplier);
    data.thenRun(() -> {
      loaded = true;
    });

    return data.thenApply(Optional::ofNullable);
  }

  public boolean supply(T element) {
    if (isLoaded() || data != null) return false;

    loaded = true;
    data = CompletableFuture.completedFuture(element);

    return true;
  }

  public boolean isLoaded() {
    return loaded;
  }
}
