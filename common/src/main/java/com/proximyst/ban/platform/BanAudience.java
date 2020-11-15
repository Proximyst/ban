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

package com.proximyst.ban.platform;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

public interface BanAudience extends Identified, Identity, Audience {
  @Override
  @SideEffectFree
  default @NonNull Identity identity() {
    return this;
  }

  @Override
  @Pure
  @NonNull UUID uuid();

  @SideEffectFree
  @NonNull String username();

  boolean hasPermission(@NonNull final String permission);

  void disconnect(final @NonNull Component reason);

  default void disconnect(final @NonNull ComponentLike reason) {
    this.disconnect(reason.asComponent());
  }

  @SuppressWarnings("unchecked") // This is intentional.
  default <A extends Audience> @NonNull A castAudience() {
    return (A) this;
  }
}
