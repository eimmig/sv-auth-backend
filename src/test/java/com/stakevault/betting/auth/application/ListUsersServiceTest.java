package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class ListUsersServiceTest {

	private static final UUID ADMIN_ID = UUID.randomUUID();

	private final UserRepository userRepository = mock(UserRepository.class);
	private final ListUsersService service = new ListUsersService(userRepository);

	private User adminCaller() {
		return new User(ADMIN_ID, "Admin", "admin@acme", "hash", Role.ADMIN, false, Instant.now());
	}

	@Test
	void shouldReturnUsersOrderedByNameWhenCallerIsAdmin() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			List<User> orderedUsers = List.of(adminCaller(),
					new User(UUID.randomUUID(), "Zed Member", "zed@acme", "hash", Role.MEMBER, false, Instant.now()));
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminCaller()));
			when(userRepository.findAllOrderByName()).thenReturn(orderedUsers);

			List<User> result = service.listUsers(ADMIN_ID);

			assertThat(result).isEqualTo(orderedUsers);
		}
	}

	@Test
	void shouldRejectWhenNoTenantContextIsOpen() {
		assertThatThrownBy(() -> service.listUsers(ADMIN_ID)).isInstanceOf(MissingTenantContextException.class);

		verifyNoInteractions(userRepository);
	}

	@Test
	void shouldRejectWhenCallerDoesNotExist() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.listUsers(ADMIN_ID)).isInstanceOf(AdminRoleRequiredException.class);
		}
	}

	@Test
	void shouldRejectWhenCallerIsNotAdmin() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			User member = new User(ADMIN_ID, "Member", "member@acme", "hash", Role.MEMBER, false, Instant.now());
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(member));

			assertThatThrownBy(() -> service.listUsers(ADMIN_ID)).isInstanceOf(AdminRoleRequiredException.class);
		}
	}
}
