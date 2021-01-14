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

package com.proximyst.ban.rest;

import com.google.gson.annotations.SerializedName;
import feign.Param;
import feign.RequestLine;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface IAshconMojangApi {
  @RequestLine("GET /user/{identifier}")
  @NonNull Optional<@NonNull AshconUser> getUser(final @NonNull @Param("identifier") String identifier);

  @NonNull
  class AshconUser {
    @SerializedName("uuid")
    public UUID uuid;

    @SerializedName("username")
    public String username;
  }
}
