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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.EmailAlreadyRegisteredException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class CreateUserServiceTest {

	private static final UUID ADMIN_ID = UUID.randomUUID();

	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
	private final CreateUserService service = new CreateUserService(userRepository, passwordHasher);

	private User adminCaller() {
		return new User(ADMIN_ID, "Admin", "admin@acme", "hash", Role.ADMIN, false, Instant.now());
	}

	@Test
	void shouldCreateMemberUserWithHashedPasswordWhenCallerIsAdmin() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminCaller()));
			when(passwordHasher.hash("raw-password")).thenReturn("hashed-password");
			when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

			User result = service.createUser(ADMIN_ID, "Member", "member@acme", "raw-password");

			assertThat(result.name()).isEqualTo("Member");
			assertThat(result.email()).isEqualTo("member@acme");
			assertThat(result.role()).isEqualTo(Role.MEMBER);
			assertThat(result.mustChangePassword()).isFalse();
			verify(userRepository).save(argThatSavedUserMatches(user -> user.passwordHash().equals("hashed-password")
					&& user.role() == Role.MEMBER && !user.mustChangePassword()));
		}
	}

	@Test
	void shouldRejectWhenNoTenantContextIsOpen() {
		assertThatThrownBy(() -> service.createUser(ADMIN_ID, "Member", "member@acme", "raw-password"))
				.isInstanceOf(MissingTenantContextException.class);

		verifyNoInteractions(userRepository, passwordHasher);
	}

	@Test
	void shouldRejectWhenCallerDoesNotExist() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.createUser(ADMIN_ID, "Member", "member@acme", "raw-password"))
					.isInstanceOf(AdminRoleRequiredException.class);

			verifyNoInteractions(passwordHasher);
			verify(userRepository, never()).save(any());
		}
	}

	@Test
	void shouldRejectWhenCallerIsNotAdmin() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			User member = new User(ADMIN_ID, "Member", "member@acme", "hash", Role.MEMBER, false, Instant.now());
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(member));

			assertThatThrownBy(() -> service.createUser(ADMIN_ID, "Other", "other@acme", "raw-password"))
					.isInstanceOf(AdminRoleRequiredException.class);

			verifyNoInteractions(passwordHasher);
			verify(userRepository, never()).save(any());
		}
	}

	@Test
	void shouldMapDuplicateEmailInsertToEmailAlreadyRegisteredException() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminCaller()));
			when(passwordHasher.hash("raw-password")).thenReturn("hashed-password");
			when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate email"));

			assertThatThrownBy(() -> service.createUser(ADMIN_ID, "Member", "member@acme", "raw-password"))
					.isInstanceOf(EmailAlreadyRegisteredException.class);
		}
	}

	private User argThatSavedUserMatches(Predicate<User> predicate) {
		return argThat(predicate::test);
	}
}
