package com.proximyst.ban.utils;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class HttpUtils {
  private HttpUtils() throws IllegalAccessException {
    throw new IllegalAccessException(getClass().getSimpleName() + " cannot be instantiated.");
  }

  @NonNull
  public static Optional<String> get(@NonNull String url) {
    try {
      URL u = new URL(url);
      HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setDoOutput(true);
      conn.setRequestProperty("User-Agent",
          "Mozilla/5.0 Proximyst/ban Velocity-plugin <https://github.com/Proximyst/ban>");
      conn.connect();
      if (conn.getResponseCode() >= 300 || conn.getResponseCode() < 200) {
        conn.disconnect();
        return Optional.empty();
      }

      try (InputStream stream = conn.getInputStream();
          InputStreamReader reader = new InputStreamReader(stream)) {
        return Optional.of(CharStreams.toString(reader));
      } finally {
        conn.disconnect();
      }
    } catch (IOException ignored) {
      return Optional.empty();
    }
  }
}
