package com.proximyst.ban.utils;

import com.google.common.io.CharStreams;
import com.proximyst.ban.BanPlugin;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ResourceReader {
  private ResourceReader() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  /**
   * Read a resource on the jar's classpath into a string.
   * <p>
   * Please note this is potentially a heavy I/O operation which should always be cached.
   * </p>
   *
   * @param path The path on the classpath to read.
   * @return The read string.
   */
  @NonNull
  public static String readResource(@NonNull String path) {
    try (InputStream stream = BanPlugin.class.getResourceAsStream("/" + path);
        Reader reader = new InputStreamReader(stream)) {
      return CharStreams.toString(reader);
    } catch (IOException ex) {
      ThrowableUtils.sneakyThrow(ex);
      throw new RuntimeException();
    }
  }
}
