package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JpaUserRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final UserRepository userRepository;

	JpaUserRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			UserRepository userRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.userRepository = userRepository;
	}

	@Test
	void shouldSaveAndFindById() {
		User user = new User(UUID.randomUUID(), "Ana", "ana@acme.com", "hash", Role.ADMIN, false,
				Instant.now().truncatedTo(DB_PRECISION));

		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			userRepository.save(user);
		}

		Optional<User> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = userRepository.findById(user.id());
		}

		assertThat(found).contains(user);
	}

	@Test
	void shouldFindByEmailWhenPresent() {
		User user = new User(UUID.randomUUID(), "Bruno", "bruno@acme.com", "hash", Role.MEMBER, true,
				Instant.now().truncatedTo(DB_PRECISION));

		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			userRepository.save(user);
		}

		Optional<User> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = userRepository.findByEmail("bruno@acme.com");
		}

		assertThat(found).contains(user);
	}

	@Test
	void shouldReturnEmptyWhenEmailNotFound() {
		Optional<User> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = userRepository.findByEmail("nobody@acme.com");
		}

		assertThat(found).isEmpty();
	}

	@Test
	void shouldRejectDuplicateEmailWithinSameSchema() {
		Instant now = Instant.now().truncatedTo(DB_PRECISION);
		User first = new User(UUID.randomUUID(), "Carla", "carla@acme.com", "hash", Role.MEMBER, false, now);
		User duplicate = new User(UUID.randomUUID(), "Carla 2", "carla@acme.com", "hash2", Role.MEMBER, false, now);

		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			userRepository.save(first);
			assertThatThrownBy(() -> userRepository.save(duplicate)).isInstanceOf(DataIntegrityViolationException.class);
		}
	}

	@Test
	void shouldAllowSameEmailAcrossDifferentTenantSchemas() {
		String otherSlug = "test-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		TenantSchemaName otherSchema = TenantSchemaName.fromSlug(otherSlug);
		provisionTenantSchema.ensureSchemaExists(otherSlug);

		try {
			Instant now = Instant.now().truncatedTo(DB_PRECISION);
			User inFirstTenant = new User(UUID.randomUUID(), "Dani", "dani@acme.com", "hash", Role.MEMBER, false, now);
			User inOtherTenant = new User(UUID.randomUUID(), "Dani", "dani@acme.com", "hash", Role.MEMBER, false, now);

			try (TenantContextScope scope = TenantContextScope.open(schema)) {
				userRepository.save(inFirstTenant);
			}
			try (TenantContextScope scope = TenantContextScope.open(otherSchema)) {
				userRepository.save(inOtherTenant);
			}

			Optional<User> foundInFirst;
			Optional<User> foundInOther;
			try (TenantContextScope scope = TenantContextScope.open(schema)) {
				foundInFirst = userRepository.findByEmail("dani@acme.com");
			}
			try (TenantContextScope scope = TenantContextScope.open(otherSchema)) {
				foundInOther = userRepository.findByEmail("dani@acme.com");
			}

			assertThat(foundInFirst).contains(inFirstTenant);
			assertThat(foundInOther).contains(inOtherTenant);
		} finally {
			jdbcTemplate.execute("DROP SCHEMA IF EXISTS \"" + otherSchema.value() + "\" CASCADE");
		}
	}
}
