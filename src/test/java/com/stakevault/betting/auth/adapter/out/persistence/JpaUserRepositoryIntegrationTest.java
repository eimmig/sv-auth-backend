package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JpaUserRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	@Autowired
	private UserRepository userRepository;

	@Test
	void salvaEBuscaPorId() {
		// truncatedTo(MICROS): timestamptz do Postgres guarda microssegundos, Instant.now() tem nanossegundos - sem isso o round-trip quebra o equals() do record.
		User user = new User(UUID.randomUUID(), "Ana", "ana@acme.com", "hash", Role.ADMIN, false, Instant.now().truncatedTo(ChronoUnit.MICROS));

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
	void buscaPorEmail_encontraQuandoExiste() {
		User user = new User(UUID.randomUUID(), "Bruno", "bruno@acme.com", "hash", Role.MEMBER, true, Instant.now().truncatedTo(ChronoUnit.MICROS));

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
	void buscaPorEmail_vazioQuandoNaoExiste() {
		Optional<User> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = userRepository.findByEmail("ninguem@acme.com");
		}

		assertThat(found).isEmpty();
	}
}
