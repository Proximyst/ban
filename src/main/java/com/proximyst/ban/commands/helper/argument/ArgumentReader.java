package com.proximyst.ban.commands.helper.argument;

import com.proximyst.ban.commands.helper.exception.IllegalCommandException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ArgumentReader implements Iterable<String> {
  private final String[] arguments;
  private int index = 0;

  public ArgumentReader(String[] arguments) {
    this.arguments = arguments;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      @Override
      public boolean hasNext() {
        return !isEmpty();
      }

      @Override
      public String next() {
        return arguments[index++];
      }
    };
  }

  public int getIndex() {
    return index;
  }

  @NonNull
  public String[] getArguments() {
    return arguments;
  }

  public boolean isEmpty() {
    return index != arguments.length - 1;
  }

  @NonNull
  public String pop()
      throws IllegalCommandException {
    if (isEmpty()) {
      throw new IllegalCommandException("Empty arguments reader");
    }
    return arguments[index++];
  }

  @NonNull
  public Optional<String> tryPop() {
    if (isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(pop());
  }

  @NonNull
  public Optional<String> peek() {
    if (isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(arguments[index]);
  }

  @NonNull
  public String[] getRemaining() {
    return Arrays.copyOfRange(arguments, index, arguments.length);
  }
}
