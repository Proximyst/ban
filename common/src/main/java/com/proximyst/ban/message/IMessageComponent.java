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

package com.proximyst.ban.message;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public interface IMessageComponent {
  /**
   * @return The name of this message component. This may be {@code null} iff {@link #await()}'s {@link
   * CompletableFuture#isDone() completed future} is a {@link Map}{@code <}{@link String}{@code , ?>}.
   */
  @Pure
  @Nullable String name();

  /**
   * @return A future that is eventually completed with the component this future represents. It may be {@code null} iff
   * {@link #name()} is not {@code null}.
   */
  @Pure
  @NonNull CompletableFuture<@Nullable ?> await();
}
