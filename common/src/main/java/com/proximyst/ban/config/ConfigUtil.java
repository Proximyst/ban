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

package com.proximyst.ban.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.transformation.ConfigurationTransformation;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ConfigUtil {
  private static final @NonNull ObjectMapper<@NonNull Configuration> OBJECT_MAPPER;
  private static final int VERSION = -1;

  static {
    try {
      OBJECT_MAPPER = ObjectMapper.forClass(Configuration.class);
    } catch (final ObjectMappingException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  private ConfigUtil() throws IllegalAccessException {
    throw new IllegalAccessException(this.getClass().getSimpleName() + " cannot be instantiated.");
  }

  public static @NonNull Configuration loadConfiguration(final @NonNull ConfigurationNode node)
      throws ObjectMappingException {
    if (!node.isVirtual()) {
      // https://github.com/SpongePowered/Configurate/blob/3.x/configurate-examples/src/main/java/ninja/leaping/configurate/examples/Transformations.java

      // Only update existing configurations.
      final ConfigurationTransformation transformation = ConfigurationTransformation.versionedBuilder()
          .build();
      transformation.apply(node);
    }

    return OBJECT_MAPPER.bindToNew().populate(node);
  }

  public static void saveConfiguration(final @NonNull Configuration configuration,
      final @NonNull ConfigurationNode node)
      throws ObjectMappingException {
    OBJECT_MAPPER.bind(configuration).serialize(node);
  }
}
