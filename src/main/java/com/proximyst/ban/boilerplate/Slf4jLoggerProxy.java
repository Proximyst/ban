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

  public Slf4jLoggerProxy(@NonNull final Logger logger) {
    super(null, null);

    this.logger = logger;
  }

  @Override
  public void log(@NonNull final LogRecord record) {
    if (record.getLevel() == Level.INFO) {
      this.logger.info(record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.CONFIG) {
      this.logger.info("[CONFIG] " + record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.FINE || record.getLevel() == Level.FINER
        || record.getLevel() == Level.FINEST) {
      this.logger.trace(record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.WARNING) {
      this.logger.warn(record.getMessage(), record.getThrown());
    } else if (record.getLevel() == Level.SEVERE) {
      this.logger.error(record.getMessage(), record.getThrown());
    } else {
      this.logger.info("[JUL LVL " + record.getLevel().intValue() + "] " + record.getMessage(), record.getThrown());
    }
  }
}
