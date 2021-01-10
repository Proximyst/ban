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
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.model.UsernameHistory.Entry;
import feign.Param;
import feign.RequestLine;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface IAshconMojangApi {
  @RequestLine("GET /user/{identifier}")
  @NonNull Optional<@NonNull AshconUser> getUser(final @NonNull @Param("identifier") String identifier);

  @NonNull
  class AshconUser {
    @SerializedName("uuid")
    public UUID uuid;

    @SerializedName("username")
    public String username;

    @SerializedName("username_history")
    public @Nullable List<@NonNull Entry> history;

    public @NonNull BanUser toBanUser() {
      return new BanUser(this.uuid,
          this.username,
          new UsernameHistory(this.uuid,
              this.history == null
                  ? Collections.singleton(new UsernameHistory.Entry(this.username, null))
                  : this.history));
    }
  }
}
