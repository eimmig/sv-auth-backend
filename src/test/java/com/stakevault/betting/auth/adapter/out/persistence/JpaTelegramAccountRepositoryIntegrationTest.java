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
import com.stakevault.betting.auth.domain.model.TelegramAccount;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JpaTelegramAccountRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	@Autowired
	private TelegramAccountRepository telegramAccountRepository;

	@Autowired
	private UserRepository userRepository;

	private UUID persistUser() {
		User user = new User(UUID.randomUUID(), "Ana", "ana-" + UUID.randomUUID() + "@acme.com", "hash", Role.MEMBER,
				false, Instant.now().truncatedTo(ChronoUnit.MICROS));
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			userRepository.save(user);
		}
		return user.id();
	}

	@Test
	void salvaEBuscaPorUserId() {
		UUID userId = persistUser();
		TelegramAccount account = new TelegramAccount(UUID.randomUUID(), userId, "123456",
				Instant.now().truncatedTo(ChronoUnit.MICROS));

		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			telegramAccountRepository.save(account);
		}

		Optional<TelegramAccount> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = telegramAccountRepository.findByUserId(account.userId());
		}

		assertThat(found).contains(account);
	}

	@Test
	void buscaPorTelegramUserId_encontraQuandoExiste() {
		UUID userId = persistUser();
		TelegramAccount account = new TelegramAccount(UUID.randomUUID(), userId, "654321",
				Instant.now().truncatedTo(ChronoUnit.MICROS));

		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			telegramAccountRepository.save(account);
		}

		Optional<TelegramAccount> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = telegramAccountRepository.findByTelegramUserId("654321");
		}

		assertThat(found).contains(account);
	}

	@Test
	void buscaPorUserId_vazioQuandoNaoExiste() {
		Optional<TelegramAccount> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = telegramAccountRepository.findByUserId(UUID.randomUUID());
		}

		assertThat(found).isEmpty();
	}
}
