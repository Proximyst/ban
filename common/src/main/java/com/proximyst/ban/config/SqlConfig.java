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

package com.proximyst.ban.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
@NonNull
public final class SqlConfig {
  @Setting(comment = "The hostname of the database.")
  public String hostname = "localhost";

  @Setting(comment = "The port of the database.")
  public short port = 5432;

  @Setting(comment = "The database name of the database server.")
  public String database = "ban";

  @Setting(comment = "The username for the SQL server.")
  public String username = "root";

  @Setting(comment = "The password to use for the SQL server.")
  public String password = "";

  @Setting(comment = "The max connections to have open in the pool.")
  public @Positive int maxConnections = 10;
}
