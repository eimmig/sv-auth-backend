package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.CallerNotFoundException;
import com.stakevault.betting.auth.domain.model.CurrentPasswordMismatchException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class ChangePasswordServiceTest {

	private static final UUID CALLER_ID = UUID.randomUUID();

	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private final ChangePasswordService service = new ChangePasswordService(userRepository, passwordHasher);

	private User caller(boolean mustChangePassword) {
		return new User(CALLER_ID, "Admin", "admin@acme", "old-hash", Role.ADMIN, mustChangePassword, Instant.now());
	}

	@Test
	void shouldHashAndPersistNewPasswordAndClearMustChangePasswordFlag() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			User existing = caller(true);
			when(userRepository.findById(CALLER_ID)).thenReturn(Optional.of(existing));
			when(passwordHasher.matches("current", "old-hash")).thenReturn(true);
			when(passwordHasher.hash("new-password")).thenReturn("new-hash");

			service.changePassword(CALLER_ID, "current", "new-password");

			verify(userRepository).update(new User(CALLER_ID, "Admin", "admin@acme", "new-hash", Role.ADMIN, false,
					existing.createdAt()));
		}
	}

	@Test
	void shouldRejectWhenNoTenantContextIsOpen() {
		assertThatThrownBy(() -> service.changePassword(CALLER_ID, "current", "new-password"))
				.isInstanceOf(MissingTenantContextException.class);

		verifyNoInteractions(userRepository, passwordHasher);
	}

	@Test
	void shouldRejectWhenCallerDoesNotExist() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(CALLER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.changePassword(CALLER_ID, "current", "new-password"))
					.isInstanceOf(CallerNotFoundException.class);

			verify(userRepository, never()).update(any());
			verifyNoInteractions(passwordHasher);
		}
	}

	@Test
	void shouldRejectWhenCurrentPasswordDoesNotMatch() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(CALLER_ID)).thenReturn(Optional.of(caller(false)));
			when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

			assertThatThrownBy(() -> service.changePassword(CALLER_ID, "wrong", "new-password"))
					.isInstanceOf(CurrentPasswordMismatchException.class);

			verify(userRepository, never()).update(any());
			verify(passwordHasher, never()).hash(any());
		}
	}
}
