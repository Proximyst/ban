package com.proximyst.ban.manager;

import com.google.inject.Singleton;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.model.BanUser;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class UserManager {
  @NonNull
  private final BanPlugin main;

  public UserManager(@NonNull BanPlugin main) {
    this.main = main;
  }

  @SuppressWarnings("DuplicatedCode")
  @NonNull
  public CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(@NonNull UUID uuid) {
    CompletableFuture<Optional<@NonNull BanUser>> future = CompletableFuture
        .supplyAsync(() -> main.getDataInterface().getUser(uuid), main.getSchedulerExecutor());
    return future.thenCompose(user -> {
      if (!user.isPresent()) {
        return main.getMojangApi().getUser(uuid).getOrLoad()
            .thenApply(optionalUser -> {
              optionalUser.ifPresent(fetchedUser ->
                  main.getProxyServer().getScheduler()
                      .buildTask(main, () -> main.getDataInterface().saveUser(fetchedUser))
                      .schedule()
              );
              return optionalUser;
            });
      }

      return future;
    });
  }

  @SuppressWarnings("DuplicatedCode")
  @NonNull
  public CompletableFuture<@NonNull Optional<@NonNull BanUser>> getUser(@NonNull String name) {
    CompletableFuture<Optional<@NonNull BanUser>> future = CompletableFuture
        .supplyAsync(() -> main.getDataInterface().getUser(name), main.getSchedulerExecutor());
    return future.thenCompose(user -> {
      if (!user.isPresent()) {
        return main.getMojangApi().getUser(name).getOrLoad()
            .thenApply(optionalUser -> {
              optionalUser.ifPresent(fetchedUser ->
                  main.getProxyServer().getScheduler()
                      .buildTask(main, () -> main.getDataInterface().saveUser(fetchedUser))
                      .schedule()
              );
              return optionalUser;
            });
      }

      return future;
    });
  }
}
