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

package com.proximyst.ban.message;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.proximyst.ban.model.BanIdentity;
import com.proximyst.ban.model.BanIdentity.IpIdentity;
import com.proximyst.ban.platform.IBanAudience;
import com.proximyst.moonshine.component.placeholder.IPlaceholderResolver;
import com.proximyst.moonshine.component.placeholder.PlaceholderContext;
import com.proximyst.moonshine.component.placeholder.ResolveResult;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class BanIdentityPlaceholderResolver<R> implements IPlaceholderResolver<R, BanIdentity> {
  @Override
  public ResolveResult resolve(final String placeholderName, final BanIdentity value,
      final PlaceholderContext<R> ctx, final Multimap<String, @Nullable Object> flags) {
    final IBanAudience[] audiences = Iterables.toArray(value.audiences().join(), IBanAudience.class);

    final String name;
    final String uuid;

    if (value instanceof IpIdentity) {
      final IpIdentity ipIdentity = (IpIdentity) value;
      name = ipIdentity.address().getHostAddress();
      uuid = "";
    } else {
      name = Arrays.stream(audiences).map(IBanAudience::username).collect(Collectors.joining(", "));
      uuid = Arrays.stream(audiences).map(a -> a.uuid().toString()).collect(Collectors.joining(", "));
    }

    final Map<String, Object> extra = value.asIpIdentity()
        .map(identity -> ImmutableMap.<String, Object>of(placeholderName + "Ip", identity.address().getHostAddress()))
        .orElse(ImmutableMap.of());

    return ResolveResult.ok(ImmutableMap.<String, Object>builder()
        .put(placeholderName + "Name", name)
        .put(placeholderName + "Uuid", uuid)
        .putAll(extra)
        .build());
  }
}
