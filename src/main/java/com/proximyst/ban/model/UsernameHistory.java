package com.proximyst.ban.model;

import com.google.common.collect.ImmutableList;
import com.proximyst.ban.data.IMojangApi;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UsernameHistory extends LoadableData<ImmutableList<UsernameHistory.Entry>> {
  @NonNull
  private final UUID uuid;

  public UsernameHistory(@NonNull UUID uuid, @NonNull Iterable<? extends UsernameHistory.Entry> entries) {
    super(ImmutableList.sortedCopyOf(
        Comparator.comparingLong(
            entry -> entry
                .getChangedAt()
                .map(Date::getTime)
                .orElse(Long.MIN_VALUE) // Original is first in the list.
        ),
        entries
    ));
    this.uuid = uuid;
  }

  public UsernameHistory(@NonNull UUID uuid, @NonNull IMojangApi mojangApi) {
    super(mojangApi.getUsernameHistory(uuid)
        .map(future ->
            future
                .thenApply(opt -> opt
                    .map(entries -> ImmutableList.sortedCopyOf(
                        Comparator.comparingLong(
                            entry -> entry
                                .getChangedAt()
                                .map(Date::getTime)
                                .orElse(Long.MIN_VALUE) // Original is first in the list.
                        ),
                        entries
                    ))
                    .orElse(ImmutableList.of())
                )
        )
        .orElseThrow(() -> new IllegalArgumentException("unknown user \"" + uuid + "\""))
    );
    this.uuid = uuid;
  }

  public static class Entry {
    @NonNull
    private final String username;

    @Nullable
    private final Date changedAt;

    public Entry(@NonNull String username, @Nullable Date changedAt) {
      this.username = username;
      this.changedAt = changedAt;
    }

    @NonNull
    public String getUsername() {
      return username;
    }

    @NonNull
    public Optional<Date> getChangedAt() {
      return Optional.ofNullable(changedAt);
    }

    public boolean isOriginal() {
      return changedAt == null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Entry entry = (Entry) o;
      return getUsername().equals(entry.getUsername()) &&
          Objects.equals(getChangedAt(), entry.getChangedAt());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getUsername(), getChangedAt());
    }

    @Override
    public String toString() {
      return "Entry{" +
          "username='" + username + '\'' +
          ", changedAt=" + changedAt +
          '}';
    }
  }
}
