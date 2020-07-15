package com.proximyst.ban.commands.helper.argument;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class FlagArgument {
  private FlagArgument() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static Set<String> getFlags(@NonNull ArgumentReader arguments) {
    if (arguments.isEmpty()) {
      return ImmutableSet.of();
    }

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    while (arguments.peek().filter(s -> s.startsWith("-") && s.length() > 1).isPresent()) {
      builder.add(arguments.pop().substring(1));
    }

    return builder.build();
  }
}
