//
// ban - A punishment suite for Velocity.
// Copyright (C) 2021 Mariell Hoversholm
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

package com.proximyst.ban.message;

import com.proximyst.ban.inject.annotation.PluginData;
import com.proximyst.ban.platform.IBanPlugin;
import com.proximyst.moonshine.message.IMessageSource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;

// TODO(Mariell Hoversholm): Support more than just English.
@Singleton
public final class BanMessageSource implements IMessageSource<String, Audience> {
  private final @NonNull Properties properties;

  @Inject
  BanMessageSource(final @NonNull @PluginData Path dataDir,
      final @NonNull IBanPlugin plugin) throws IOException {
    // We need to read the existing file, merge the missing keys, and write the file.
    this.properties = new Properties();
    final File file = dataDir.resolve("messages-en.properties").toFile();
    if (file.isFile()) {
      try (final Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
        this.properties.load(reader);
      }
    }

    boolean write = !file.isFile();
    try (final InputStream stream = plugin.getClass().getResourceAsStream("/messages-en.properties")) {
      final Properties packaged = new Properties();
      packaged.load(stream);
      for (final Entry<Object, Object> entry : packaged.entrySet()) {
        write |= this.properties.putIfAbsent(entry.getKey(), entry.getValue()) == null;
      }
    }

    if (write) {
      try (final Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
        this.properties.store(writer, null);
      }
    }
  }

  @Override
  public String message(final String key, final Audience receiver) {
    final String value = this.properties.getProperty(key);
    if (value == null) {
      throw new IllegalStateException("No message mapping for key " + key);
    }

    return value;
  }
}
