package com.proximyst.ban.boilerplate;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

/**
 * A proxy for a {@link java.util.logging.Logger JUL Logger} to a {@link Logger SLF4J Logger}.
 */
public final class Slf4jLoggerProxy extends java.util.logging.Logger {
  @NonNull
  private final Logger logger;

  public Slf4jLoggerProxy(@NonNull Logger logger) {
    super(null, null);

    this.logger = logger;
  }

  @Override
  public void log(LogRecord record) {
    if (record.getLevel() == Level.INFO) {
      logger.info(record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.CONFIG) {
      logger.info("[CONFIG] " + record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.FINE || record.getLevel() == Level.FINER
        || record.getLevel() == Level.FINEST) {
      logger.trace(record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.WARNING) {
      logger.warn(record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.SEVERE) {
      logger.error(record.getMessage(), record.getThrown());
    } else {
      logger.info("[JUL LVL " + record.getLevel().intValue() + "] " + record.getMessage(), record.getThrown());
    }
  }
}
