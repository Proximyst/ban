package com.proximyst.ban.config;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

@ConfigSerializable
public final class Configuration {
  @Setting(comment = "The SQL server settings.")
  private SqlConfig sql = new SqlConfig();

  @NonNull
  public SqlConfig getSql() {
    return sql;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Configuration that = (Configuration) o;
    return getSql().equals(that.getSql());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSql());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("sql", sql)
        .toString();
  }
}
