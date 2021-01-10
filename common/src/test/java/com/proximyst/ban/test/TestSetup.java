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

package com.proximyst.ban.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.proximyst.ban.factory.IBanExceptionalFutureLoggerFactory;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import com.proximyst.ban.model.UsernameHistory.Entry;
import com.proximyst.ban.rest.IAshconMojangApi.AshconUser;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.RandomStringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestSetup {
  public static final @NonNull UUID USER_UUID = UUID.fromString("8c123c25-49ed-49ea-a7a5-b5e4ef3108f9");
  public static final @NonNull String USER_NAME = "Proximyst";
  public static final @NonNull String ORIGINAL_NAME = "OldProximyst";

  public static final @NonNull Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  public static final @NonNull Executor EXECUTOR = Executors.newSingleThreadExecutor();

  private TestSetup() {
  }

  public static class ConstantsModule extends AbstractModule {
    @Singleton
    @Provides
    @NonNull Gson gson() {
      return GSON;
    }

    @Singleton
    @Provides
    @Named("constant")
    @NonNull AshconUser ashconUser() {
      final AshconUser ashconUser = new AshconUser();
      ashconUser.uuid = USER_UUID;
      ashconUser.username = USER_NAME;
      ashconUser.history = List.of(new Entry(ORIGINAL_NAME, null));
      return ashconUser;
    }

    @Provides
    @Named("random")
    @NonNull AshconUser randomAshconUser() {
      final AshconUser ashconUser = new AshconUser();
      ashconUser.uuid = UUID.randomUUID();
      ashconUser.username = RandomStringUtils.randomAlphabetic(6, 12);
      ashconUser.history = List.of(new Entry(RandomStringUtils.randomAlphabetic(7, 14), null));
      return ashconUser;
    }

    @Singleton
    @Provides
    @NonNull @BanAsyncExecutor Executor executor() {
      return EXECUTOR;
    }

    @Singleton
    @Provides
    @NonNull Logger logger() {
      return LoggerFactory.getLogger("ban-tests");
    }

    @Singleton
    @Provides
    @NonNull IBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory(
        final IBanExceptionalFutureLoggerFactory.@NonNull ImplBanExceptionalFutureLoggerFactory banExceptionalFutureLoggerFactory) {
      return banExceptionalFutureLoggerFactory;
    }
  }
}
