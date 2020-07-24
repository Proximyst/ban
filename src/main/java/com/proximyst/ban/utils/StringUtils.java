package com.proximyst.ban.utils;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class StringUtils {
  private StringUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static String join(@NonNull String delimiter, int from, @NonNull String... strings) {
    if (strings.length - 1 == from) {
      return strings[strings.length - 1];
    } else if (strings.length - 1 < from) {
      throw new IllegalArgumentException("from > strings.length - 1");
    }

    StringBuilder builder = new StringBuilder();
    for (int i = from; i < strings.length; ++i) {
      builder.append(strings[i]);
      if (i != strings.length - 1) {
        builder.append(delimiter);
      }
    }
    return builder.toString();
  }

  @NonNull
  public static String rehyphenUuid(@NonNull String string) {
    if (string.length() == 32) {
      // No dashes, add them first.
      StringBuilder builder = new StringBuilder(string);
      builder.insert(8, '-');
      builder.insert(13, '-');
      builder.insert(18, '-');
      builder.insert(23, '-');
      return builder.toString();
    }

    return string;
  }
}
