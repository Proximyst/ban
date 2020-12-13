//
// ban - A punishment suite for Velocity.
// Copyright (C) 2020 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

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

  // TODO(Proximyst): java.net.http
  public static @NonNull Optional<@NonNull String> get(final @NonNull String url) {
    try {
      final URL u = new URL(url);
      final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setDoOutput(true);
      conn.setRequestProperty("User-Agent",
          "Mozilla/5.0 Proximyst/ban Velocity-plugin <https://github.com/Proximyst/ban>");
      conn.connect();
      if (conn.getResponseCode() >= 300 || conn.getResponseCode() < 200) {
        conn.disconnect();
        return Optional.empty();
      }

      try (final InputStream stream = conn.getInputStream();
          final InputStreamReader reader = new InputStreamReader(stream)) {
        return Optional.of(CharStreams.toString(reader));
      } finally {
        conn.disconnect();
      }
    } catch (final IOException ignored) {
      return Optional.empty();
    }
  }
}
