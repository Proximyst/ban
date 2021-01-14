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

package com.proximyst.ban.model.sql;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum IpAddressType {
  IPV4(0),
  IPV6(1),
  ;

  private static final Map<Byte, IpAddressType> IP_ADDRESS_TYPES = Arrays.stream(values())
      .collect(Collectors.toUnmodifiableMap(IpAddressType::type, Function.identity()));

  private final byte type;

  IpAddressType(final int type) {
    this.type = (byte) type;
  }

  public byte type() {
    return this.type;
  }

  public static @NonNull Optional<@NonNull IpAddressType> fromType(final byte type) {
    return Optional.ofNullable(IP_ADDRESS_TYPES.get(type));
  }
}
