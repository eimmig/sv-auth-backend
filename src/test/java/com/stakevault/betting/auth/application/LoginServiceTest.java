package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.InvalidCredentialsException;
import com.stakevault.betting.auth.domain.model.LoginResult;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.AccessTokenIssuer;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class LoginServiceTest {

	private final ProvisionTenantSchemaUseCase provisionTenantSchema = mock(ProvisionTenantSchemaUseCase.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private final AccessTokenIssuer accessTokenIssuer = mock(AccessTokenIssuer.class);
	private final LoginService service = new LoginService(provisionTenantSchema, userRepository, passwordHasher,
			accessTokenIssuer);

	private User registeredUser(boolean mustChangePassword) {
		return new User(UUID.randomUUID(), "Ana", "ana@acme", "hashed-password", Role.MEMBER, mustChangePassword,
				Instant.now());
	}

	@Test
	void shouldReturnTokenAndMustChangePasswordOnSuccessfulLogin() {
		User user = registeredUser(true);
		when(provisionTenantSchema.exists("acme")).thenReturn(true);
		when(userRepository.findByEmail("ana@acme")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("raw-password", "hashed-password")).thenReturn(true);
		when(accessTokenIssuer.issue(user.id(), "acme")).thenReturn("v4.local.token");

		LoginResult result = service.login("acme", "ana@acme", "raw-password");

		assertThat(result).isEqualTo(new LoginResult("v4.local.token", true));
		verify(provisionTenantSchema).migrateIfPending("acme");
	}

	@Test
	void shouldRejectInvalidSlugWithoutTouchingAnyPort() {
		assertThatThrownBy(() -> service.login("1acme", "ana@acme", "raw-password"))
				.isInstanceOf(InvalidCredentialsException.class);

		verifyNoInteractions(provisionTenantSchema, userRepository, passwordHasher, accessTokenIssuer);
	}

	@Test
	void shouldRejectWhenTenantDoesNotExistButStillHashToKeepTimingConsistent() {
		when(provisionTenantSchema.exists("acme")).thenReturn(false);

		assertThatThrownBy(() -> service.login("acme", "ana@acme", "raw-password"))
				.isInstanceOf(InvalidCredentialsException.class);

		verify(passwordHasher).hash("raw-password");
		verify(provisionTenantSchema, never()).migrateIfPending(any());
		verifyNoInteractions(userRepository, accessTokenIssuer);
	}

	@Test
	void shouldRejectWhenTenantDoesNotExistAndPasswordIsTooLongForBCrypt() {
		when(provisionTenantSchema.exists("acme")).thenReturn(false);
		when(passwordHasher.hash(any())).thenThrow(new IllegalArgumentException("password exceeds 72 bytes"));

		assertThatThrownBy(() -> service.login("acme", "ana@acme", "a".repeat(100)))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void shouldRejectWhenEmailDoesNotExistButStillHashToKeepTimingConsistent() {
		when(provisionTenantSchema.exists("acme")).thenReturn(true);
		when(userRepository.findByEmail("nobody@acme")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.login("acme", "nobody@acme", "raw-password"))
				.isInstanceOf(InvalidCredentialsException.class);

		verify(passwordHasher).hash("raw-password");
		verifyNoInteractions(accessTokenIssuer);
	}

	@Test
	void shouldRejectWhenPasswordDoesNotMatch() {
		User user = registeredUser(false);
		when(provisionTenantSchema.exists("acme")).thenReturn(true);
		when(userRepository.findByEmail("ana@acme")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("wrong-password", "hashed-password")).thenReturn(false);

		assertThatThrownBy(() -> service.login("acme", "ana@acme", "wrong-password"))
				.isInstanceOf(InvalidCredentialsException.class);

		verifyNoInteractions(accessTokenIssuer);
	}
}
