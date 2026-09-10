package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.CreatedTenantAdmin;
import com.stakevault.betting.auth.domain.model.InvalidTenantSlugException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantAlreadyProvisionedException;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.TemporaryPasswordGenerator;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class CreateTenantServiceTest {

	private final ProvisionTenantSchemaUseCase provisionTenantSchema = mock(ProvisionTenantSchemaUseCase.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private final TemporaryPasswordGenerator passwordGenerator = mock(TemporaryPasswordGenerator.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final CreateTenantService service = new CreateTenantService(
			provisionTenantSchema, passwordHasher, passwordGenerator, userRepository);

	@Test
	void shouldCreateAdminUserWithGeneratedPasswordWhenSlugIsNew() {
		when(provisionTenantSchema.exists("acme")).thenReturn(false);
		when(passwordGenerator.generate()).thenReturn("raw-temporary-password");
		when(passwordHasher.hash("raw-temporary-password")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			assertThat(TenantContextHolder.current()).isEqualTo(new TenantSchemaName("tenant_acme"));
			return invocation.getArgument(0);
		});

		CreatedTenantAdmin result = service.createTenant("acme", "Acme Corp");

		verify(provisionTenantSchema).ensureSchemaExists("acme");
		assertThat(result.email()).isEqualTo("admin@acme");
		assertThat(result.temporaryPassword()).isEqualTo("raw-temporary-password");
		assertThat(TenantContextHolder.current()).isNull();
	}

	@Test
	void shouldSaveAdminUserWithHashedPasswordAndMustChangePassword() {
		when(provisionTenantSchema.exists("acme")).thenReturn(false);
		when(passwordGenerator.generate()).thenReturn("raw-temporary-password");
		when(passwordHasher.hash("raw-temporary-password")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.createTenant("acme", "Acme Corp");

		verify(userRepository).save(argThatSavedUserMatches(user -> user.name().equals("Acme Corp")
				&& user.email().equals("admin@acme")
				&& user.passwordHash().equals("hashed-password")
				&& user.role() == Role.ADMIN
				&& user.mustChangePassword()));
	}

	@Test
	void shouldUseDefaultAdminNameWhenTenantNameIsBlank() {
		when(provisionTenantSchema.exists("acme")).thenReturn(false);
		when(passwordGenerator.generate()).thenReturn("raw-temporary-password");
		when(passwordHasher.hash("raw-temporary-password")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.createTenant("acme", " ");

		verify(userRepository).save(argThatSavedUserMatches(user -> user.name().equals("Administrator")));
	}

	@Test
	void shouldRejectInvalidSlugWithoutTouchingAnyPort() {
		assertThatThrownBy(() -> service.createTenant("1acme", "Acme Corp"))
				.isInstanceOf(InvalidTenantSlugException.class);

		verifyNoInteractions(provisionTenantSchema, passwordHasher, passwordGenerator, userRepository);
	}

	@Test
	void shouldRejectAlreadyProvisionedSlugWithoutCreatingSchemaOrUser() {
		when(provisionTenantSchema.exists("acme")).thenReturn(true);

		assertThatThrownBy(() -> service.createTenant("acme", "Acme Corp"))
				.isInstanceOf(TenantAlreadyProvisionedException.class);

		verify(provisionTenantSchema, never()).ensureSchemaExists(any());
		verifyNoInteractions(passwordHasher, passwordGenerator, userRepository);
	}

	@Test
	void shouldMapConcurrentDuplicateInsertToTenantAlreadyProvisionedException() {
		when(provisionTenantSchema.exists("acme")).thenReturn(false);
		when(passwordGenerator.generate()).thenReturn("raw-temporary-password");
		when(passwordHasher.hash("raw-temporary-password")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate email"));

		assertThatThrownBy(() -> service.createTenant("acme", "Acme Corp"))
				.isInstanceOf(TenantAlreadyProvisionedException.class);
		assertThat(TenantContextHolder.current()).isNull();
	}

	private User argThatSavedUserMatches(Predicate<User> predicate) {
		return argThat(predicate::test);
	}
}
