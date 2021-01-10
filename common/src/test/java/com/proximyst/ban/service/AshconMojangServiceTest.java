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

package com.proximyst.ban.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.rest.IAshconMojangApi;
import com.proximyst.ban.rest.IAshconMojangApi.AshconUser;
import com.proximyst.ban.service.impl.ImplAshconMojangService;
import com.proximyst.ban.test.TestSetup;
import com.proximyst.ban.test.TestSetup.ConstantsModule;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AshconMojangServiceTest {
  @Mock
  private IAshconMojangApi ashconMojangApi;

  @Inject
  private ImplAshconMojangService service;

  @Inject
  @Named("constant")
  private AshconUser validUser;

  @BeforeEach
  void setUp() {
    this.ashconMojangApi = mock(IAshconMojangApi.class);
    Guice.createInjector(new ConstantsModule(), new MockModule(this)).injectMembers(this);
  }

  @Test
  void fetchByUsername() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    Optional<BanUser> optional = this.service.getUser(TestSetup.USER_NAME).join();
    verify(this.ashconMojangApi).getUser(TestSetup.USER_NAME);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    optional = this.service.getUser(TestSetup.USER_NAME).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // The cache uses UUIDs.
    optional = this.service.getUser(TestSetup.USER_UUID).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());
  }

  @Test
  void fetchByUuid() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    Optional<BanUser> optional = this.service.getUser(TestSetup.USER_UUID).join();
    verify(this.ashconMojangApi).getUser(TestSetup.USER_UUID.toString());
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    optional = this.service.getUser(TestSetup.USER_UUID).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // We should have cached the name now.
    optional = this.service.getUser(TestSetup.USER_NAME).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());
  }

  static class MockModule extends AbstractModule {
    private final AshconMojangServiceTest test;

    MockModule(final AshconMojangServiceTest test) {
      this.test = test;
    }

    @Provides
    @NonNull IAshconMojangApi api() {
      return this.test.ashconMojangApi;
    }

    @Provides
    @NonNull IBanServer banServer() {
      return mock(IBanServer.class);
    }
  }
}
