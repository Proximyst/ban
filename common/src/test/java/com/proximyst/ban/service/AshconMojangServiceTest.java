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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.proximyst.ban.model.BanUser;
import com.proximyst.ban.model.UsernameHistory;
import com.proximyst.ban.model.UsernameHistory.Entry;
import com.proximyst.ban.platform.IBanServer;
import com.proximyst.ban.rest.IAshconMojangApi;
import com.proximyst.ban.rest.IAshconMojangApi.AshconUser;
import com.proximyst.ban.service.impl.ImplAshconMojangService;
import com.proximyst.ban.test.TestSetup;
import com.proximyst.ban.test.TestSetup.ConstantsModule;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import org.apache.commons.lang3.RandomStringUtils;
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
  void getUserByUsername() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    Optional<BanUser> optional = this.service.getUser(TestSetup.USER_NAME).join();
    verify(this.ashconMojangApi).getUser(TestSetup.USER_NAME);
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // Must be cached now.
    optional = this.service.getUser(TestSetup.USER_NAME).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // The cache contains more than just username and UUID.
    this.verifyCachedUser();
  }

  @Test
  void getUserByUuid() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    Optional<BanUser> optional = this.service.getUser(TestSetup.USER_UUID).join();
    verify(this.ashconMojangApi).getUser(TestSetup.USER_UUID.toString());
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // Must be cached.
    optional = this.service.getUser(TestSetup.USER_UUID).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // The name method also works with UUIDs.
    optional = this.service.getUser(TestSetup.USER_UUID.toString()).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    optional = this.service.getUser(TestSetup.USER_UUID.toString().replace("-", "")).join();
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isPresent();
    assertThat(optional).get().isEqualTo(this.validUser.toBanUser());

    // The cache contains more than just username and UUID.
    this.verifyCachedUser();
  }

  @Test
  void getUserEmptyApiResponse() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.empty());

    Optional<BanUser> optional = this.service.getUser(TestSetup.USER_UUID).join();
    verify(this.ashconMojangApi).getUser(TestSetup.USER_UUID.toString());
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isNotPresent();

    optional = this.service.getUser(TestSetup.USER_NAME).join();
    verify(this.ashconMojangApi).getUser(TestSetup.USER_NAME);
    verifyNoMoreInteractions(this.ashconMojangApi);
    assertThat(optional).isNotPresent();
  }

  @Test
  void getUserInvalidUuid() {
    final String identifier = RandomStringUtils.randomAlphabetic(40);
    assertThatThrownBy(() -> this.service.getUser(identifier).join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username/UUID \"%s\" has an invalid length.", identifier)
        .hasNoCause();
    assertThatThrownBy(() -> this.service.getUuid(identifier).join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username/UUID \"%s\" has an invalid length.", identifier)
        .hasNoCause();

    assertThatThrownBy(() -> this.service.getUser("e").join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username \"%s\" is too short.", "e")
        .hasNoCause();
    assertThatThrownBy(() -> this.service.getUuid("e").join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username \"%s\" is too short.", "e")
        .hasNoCause();

    final String identifier2 = RandomStringUtils.randomAlphabetic(32);
    assertThatThrownBy(() -> this.service.getUser(identifier2).join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();
    assertThatThrownBy(() -> this.service.getUuid(identifier2).join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();

    final String identifier3 = RandomStringUtils.randomAlphabetic(36);
    assertThatThrownBy(() -> this.service.getUser(identifier3).join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();
    assertThatThrownBy(() -> this.service.getUuid(identifier3).join())
        .isInstanceOf(CompletionException.class)
        .getCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasNoCause();

    verifyNoInteractions(this.ashconMojangApi);
  }

  @Test
  void getUuidByUsername() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    assertThat(this.service.getUuid(TestSetup.USER_NAME).join())
        .isPresent()
        .get()
        .isEqualTo(TestSetup.USER_UUID);
    verify(this.ashconMojangApi).getUser(TestSetup.USER_NAME);
    verifyNoMoreInteractions(this.ashconMojangApi);

    // The cache contains more than just username and UUID.
    this.verifyCachedUser();
  }

  @Test
  void getUsernameByUuid() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    assertThat(this.service.getUsername(TestSetup.USER_UUID).join())
        .isPresent()
        .get()
        .isEqualTo(TestSetup.USER_NAME);
    verify(this.ashconMojangApi).getUser(TestSetup.USER_UUID.toString());
    verifyNoMoreInteractions(this.ashconMojangApi);

    // The cache contains more than just username and UUID.
    this.verifyCachedUser();
  }

  @Test
  void getUsernameHistoryByUuid() {
    when(this.ashconMojangApi.getUser(anyString())).thenReturn(Optional.of(this.validUser));

    assertThat(this.service.getUsernameHistory(TestSetup.USER_UUID).join())
        .isPresent()
        .get()
        .isEqualTo(new UsernameHistory(TestSetup.USER_UUID, List.of(new Entry(TestSetup.ORIGINAL_NAME, null))));
    verify(this.ashconMojangApi).getUser(TestSetup.USER_UUID.toString());
    verifyNoMoreInteractions(this.ashconMojangApi);

    // The cache contains more than just username and UUID.
  }

  private void verifyCachedUser() {
    assertThat(this.service.getUser(TestSetup.USER_UUID).join())
        .isPresent()
        .get()
        .isEqualTo(this.validUser.toBanUser());

    assertThat(this.service.getUser(TestSetup.USER_UUID.toString()).join())
        .isPresent()
        .get()
        .isEqualTo(this.validUser.toBanUser());

    assertThat(this.service.getUser(TestSetup.USER_UUID.toString().replace("-", "")).join())
        .isPresent()
        .get()
        .isEqualTo(this.validUser.toBanUser());

    assertThat(this.service.getUser(TestSetup.USER_NAME).join())
        .isPresent()
        .get()
        .isEqualTo(this.validUser.toBanUser());

    assertThat(this.service.getUsername(TestSetup.USER_UUID).join())
        .isPresent()
        .get()
        .isEqualTo(TestSetup.USER_NAME);

    assertThat(this.service.getUuid(TestSetup.USER_NAME).join())
        .isPresent()
        .get()
        .isEqualTo(TestSetup.USER_UUID);

    assertThat(this.service.getUuid(TestSetup.USER_UUID.toString()).join())
        .isPresent()
        .get()
        .isEqualTo(TestSetup.USER_UUID);

    assertThat(this.service.getUuid(TestSetup.USER_UUID.toString().replace("-", "")).join())
        .isPresent()
        .get()
        .isEqualTo(TestSetup.USER_UUID);

    assertThat(this.service.getUsernameHistory(TestSetup.USER_UUID).join())
        .isPresent()
        .get()
        .isEqualTo(new UsernameHistory(TestSetup.USER_UUID, List.of(new Entry(TestSetup.ORIGINAL_NAME, null))));

    verifyNoMoreInteractions(this.ashconMojangApi);
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
