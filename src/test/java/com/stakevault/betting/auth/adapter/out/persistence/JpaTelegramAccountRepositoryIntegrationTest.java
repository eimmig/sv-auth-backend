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
import com.stakevault.betting.auth.domain.model.TelegramAccount;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;
import com.stakevault.betting.auth.domain.port.out.UserRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JpaTelegramAccountRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final TelegramAccountRepository telegramAccountRepository;
	private final UserRepository userRepository;

	JpaTelegramAccountRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			TelegramAccountRepository telegramAccountRepository, UserRepository userRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.telegramAccountRepository = telegramAccountRepository;
		this.userRepository = userRepository;
	}

	private UUID persistUser() {
		User user = new User(UUID.randomUUID(), "Ana", "ana-" + UUID.randomUUID() + "@acme.com", "hash", Role.MEMBER,
				false, Instant.now().truncatedTo(DB_PRECISION));
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			userRepository.save(user);
		}
		return user.id();
	}

	@Test
	void shouldSaveAndFindByUserId() {
		UUID userId = persistUser();
		TelegramAccount account = new TelegramAccount(UUID.randomUUID(), userId, "123456",
				Instant.now().truncatedTo(DB_PRECISION));

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
	void shouldFindByTelegramUserIdWhenPresent() {
		UUID userId = persistUser();
		TelegramAccount account = new TelegramAccount(UUID.randomUUID(), userId, "654321",
				Instant.now().truncatedTo(DB_PRECISION));

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
	void shouldReturnEmptyWhenUserIdNotFound() {
		Optional<TelegramAccount> found;
		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			found = telegramAccountRepository.findByUserId(UUID.randomUUID());
		}

		assertThat(found).isEmpty();
	}

	@Test
	void shouldRejectSecondLinkForSameUserId() {
		UUID userId = persistUser();
		TelegramAccount first = new TelegramAccount(UUID.randomUUID(), userId, "111111",
				Instant.now().truncatedTo(DB_PRECISION));
		TelegramAccount second = new TelegramAccount(UUID.randomUUID(), userId, "222222",
				Instant.now().truncatedTo(DB_PRECISION));

		try (TenantContextScope scope = TenantContextScope.open(schema)) {
			telegramAccountRepository.save(first);
			assertThatThrownBy(() -> telegramAccountRepository.save(second))
					.isInstanceOf(DataIntegrityViolationException.class);
		}
	}
}
