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
   *
   * @param path The path on the classpath to read.
   * @return The read string.
   */
  public static @NonNull String readResource(final @NonNull String path) {
    // We use this class as the "entry point" to our jar.
    // All classes should be loaded by the plugin's classloader anyways; at least this very class...
    try (final InputStream stream = ResourceReader.class.getResourceAsStream("/" + path);
        final Reader reader = new InputStreamReader(stream)) {
      return CharStreams.toString(reader);
    } catch (final IOException ex) {
      ThrowableUtils.sneakyThrow(ex);
      throw new RuntimeException();
    }
  }
}
