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

package com.proximyst.ban.factory;

import com.proximyst.ban.utils.BanExceptionalFutureLogger;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

public interface IBanExceptionalFutureLoggerFactory {
  <T> @NonNull BanExceptionalFutureLogger<? extends T> createLogger(final @NonNull Class<?> owner);

  @Singleton
  class ImplBanExceptionalFutureLoggerFactory implements IBanExceptionalFutureLoggerFactory {
    private final @NonNull Provider<@NonNull Logger> loggerProvider;

    @Inject
    public ImplBanExceptionalFutureLoggerFactory(final @NonNull Provider<@NonNull Logger> loggerProvider) {
      this.loggerProvider = loggerProvider;
    }

    @Override
    public @NonNull <T> BanExceptionalFutureLogger<? extends T> createLogger(final @NonNull Class<?> owner) {
      return new BanExceptionalFutureLogger<>(this.loggerProvider.get(), owner.getName());
    }
  }
}
