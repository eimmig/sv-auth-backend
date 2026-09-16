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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.LastAdminCannotBeDemotedException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.model.UserNotFoundException;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class UpdateUserServiceTest {

	private static final UUID ADMIN_ID = UUID.randomUUID();
	private static final UUID TARGET_ID = UUID.randomUUID();

	private final UserRepository userRepository = mock(UserRepository.class);
	private final UpdateUserService service = new UpdateUserService(userRepository);

	private User adminCaller() {
		return new User(ADMIN_ID, "Admin", "admin@acme", "hash", Role.ADMIN, false, Instant.now());
	}

	private User targetMember() {
		return new User(TARGET_ID, "Member", "member@acme", "hash", Role.MEMBER, false, Instant.now());
	}

	@Test
	void shouldUpdateNameAndRoleWhenCallerIsAdmin() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminCaller()));
			when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(targetMember()));
			when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

			User result = service.updateUser(ADMIN_ID, TARGET_ID, "Renamed", Role.ADMIN);

			assertThat(result.name()).isEqualTo("Renamed");
			assertThat(result.role()).isEqualTo(Role.ADMIN);
			assertThat(result.id()).isEqualTo(TARGET_ID);
			assertThat(result.email()).isEqualTo("member@acme");
		}
	}

	@Test
	void shouldRejectWhenNoTenantContextIsOpen() {
		assertThatThrownBy(() -> service.updateUser(ADMIN_ID, TARGET_ID, "Renamed", Role.MEMBER))
				.isInstanceOf(MissingTenantContextException.class);

		verifyNoInteractions(userRepository);
	}

	@Test
	void shouldRejectWhenCallerDoesNotExist() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.updateUser(ADMIN_ID, TARGET_ID, "Renamed", Role.MEMBER))
					.isInstanceOf(AdminRoleRequiredException.class);

			verify(userRepository, never()).update(any());
		}
	}

	@Test
	void shouldRejectWhenCallerIsNotAdmin() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			User member = new User(ADMIN_ID, "Member", "member@acme", "hash", Role.MEMBER, false, Instant.now());
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(member));

			assertThatThrownBy(() -> service.updateUser(ADMIN_ID, TARGET_ID, "Renamed", Role.MEMBER))
					.isInstanceOf(AdminRoleRequiredException.class);

			verify(userRepository, never()).update(any());
		}
	}

	@Test
	void shouldRejectWhenTargetUserDoesNotExist() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminCaller()));
			when(userRepository.findById(TARGET_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.updateUser(ADMIN_ID, TARGET_ID, "Renamed", Role.MEMBER))
					.isInstanceOf(UserNotFoundException.class);

			verify(userRepository, never()).update(any());
		}
	}

	@Test
	void shouldRejectDemotingTheLastAdminOfTheTenant() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			User onlyAdmin = adminCaller();
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(onlyAdmin));
			when(userRepository.findAllOrderByName()).thenReturn(List.of(onlyAdmin));

			assertThatThrownBy(() -> service.updateUser(ADMIN_ID, ADMIN_ID, "Renamed", Role.MEMBER))
					.isInstanceOf(LastAdminCannotBeDemotedException.class);

			verify(userRepository, never()).update(any());
		}
	}

	@Test
	void shouldAllowDemotingAnAdminWhenAnotherAdminRemains() {
		try (var _ = TenantContextScope.open(new TenantSchemaName("tenant_acme"))) {
			User otherAdmin = new User(UUID.randomUUID(), "Other Admin", "other@acme", "hash", Role.ADMIN, false,
					Instant.now());
			User targetAdmin = new User(TARGET_ID, "Target Admin", "target@acme", "hash", Role.ADMIN, false,
					Instant.now());
			when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminCaller()));
			when(userRepository.findById(TARGET_ID)).thenReturn(Optional.of(targetAdmin));
			when(userRepository.findAllOrderByName()).thenReturn(List.of(adminCaller(), otherAdmin, targetAdmin));
			when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

			User result = service.updateUser(ADMIN_ID, TARGET_ID, "Target Renamed", Role.MEMBER);

			assertThat(result.role()).isEqualTo(Role.MEMBER);
		}
	}
}
