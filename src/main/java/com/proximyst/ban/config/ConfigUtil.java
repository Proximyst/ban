package com.proximyst.ban.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.transformation.ConfigurationTransformation;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ConfigUtil {
  private static final ObjectMapper<Configuration> OBJECT_MAPPER;
  private static final int VERSION = -1;

  static {
    try {
      OBJECT_MAPPER = ObjectMapper.forClass(Configuration.class);
    } catch (ObjectMappingException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  @NonNull
  public static Configuration loadConfiguration(@NonNull ConfigurationNode node)
      throws ObjectMappingException {
    if (!node.isVirtual()) {
      // https://github.com/SpongePowered/Configurate/blob/3.x/configurate-examples/src/main/java/ninja/leaping/configurate/examples/Transformations.java

      // Only update existing configurations.
      ConfigurationTransformation transformation = ConfigurationTransformation.versionedBuilder()
          .build();
      transformation.apply(node);
    }

    return OBJECT_MAPPER.bindToNew().populate(node);
  }

  public static void saveConfiguration(@NonNull Configuration configuration, @NonNull ConfigurationNode node)
      throws ObjectMappingException {
    OBJECT_MAPPER.bind(configuration).serialize(node);
  }
}
