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

package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.factory.ICloudArgumentFactory;
import com.proximyst.ban.factory.IMessageFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class FactoryModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
        .build(ICloudArgumentFactory.class));
    install(new FactoryModuleBuilder()
        .build(IMessageFactory.class));
  }

  @Provides
  @Singleton
  @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory(
      final IBanExceptionalFutureLoggerFactory.@NonNull ImplBanExceptionalFutureLoggerFactory factory) {
    return factory;
  }
}
