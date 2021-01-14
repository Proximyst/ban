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

package com.proximyst.ban.inject;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.proximyst.ban.rest.IAshconMojangApi;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.http2client.Http2Client;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class FeignModule extends AbstractModule {
  @Singleton
  @Provides
  @NonNull IAshconMojangApi ashconMojangApi(final @NonNull Gson gson) {
    return Feign.builder()
        .encoder(new GsonEncoder(gson))
        .decoder(new GsonDecoder(gson))
        .client(new Http2Client())
        .target(IAshconMojangApi.class, "https://api.ashcon.app/mojang/v2");
  }
}
