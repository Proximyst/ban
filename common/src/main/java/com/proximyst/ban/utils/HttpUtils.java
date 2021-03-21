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

package com.proximyst.ban.utils;

import com.google.common.base.Charsets;
import com.proximyst.ban.inject.annotation.BanAsyncExecutor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodySubscribers;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public final class HttpUtils {
  private static final int STATUS_RANGE_OK = 200;
  private static final int STATUS_RANGE_LENGTH = 100;
  private static final int STATUS_NO_CONTENT = 204;

  private final @NonNull HttpClient httpClient;

  @Inject
  HttpUtils(final @BanAsyncExecutor @NonNull Executor executor) {
    this.httpClient = HttpClient.newBuilder()
        .executor(executor)
        .version(Version.HTTP_2)
        .followRedirects(Redirect.NORMAL)
        .build();
  }

  public @NonNull CompletableFuture<@NonNull Optional<@NonNull String>> get(final @NonNull String url) {
    final URI u;
    try {
      u = URI.create(url);
    } catch (final IllegalArgumentException ex) {
      ThrowableUtils.sneakyThrow(ex);
      throw new RuntimeException();
    }

    return this.httpClient.sendAsync(HttpRequest.newBuilder()
            .GET()
            .setHeader("User-Agent",
                "Mozilla/5.0 Incendo/ban plugin <https://github.com/Incendo/ban>")
            .uri(u)
            .build(),
        responseInfo -> BodySubscribers.ofString(Charsets.UTF_8))
        .thenApply(response -> {
          final int status = response.statusCode();
          if (status == STATUS_NO_CONTENT ||
              status < STATUS_RANGE_OK ||
              status >= STATUS_RANGE_OK + STATUS_RANGE_LENGTH) {
            return Optional.empty();
          }

          return Optional.ofNullable(response.body());
        });
  }
}
