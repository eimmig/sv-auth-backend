package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.adapter.out.persistence.JpaPendingTelegramLinkRepository;
import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

@Import(TelegramLinkConfirmationTransactionRollbackIntegrationTest.FailingDeleteConfig.class)
class TelegramLinkConfirmationTransactionRollbackIntegrationTest extends TenantSchemaIntegrationSupport {

	@TestConfiguration
	static class FailingDeleteConfig {

		@Bean
		@Primary
		PendingTelegramLinkRepository failingPendingTelegramLinkRepository(JpaPendingTelegramLinkRepository real) {
			return new PendingTelegramLinkRepository() {

				@Override
				public void save(PendingTelegramLink pendingTelegramLink) {
					real.save(pendingTelegramLink);
				}

				@Override
				public Optional<PendingTelegramLink> findByCode(String code) {
					return real.findByCode(code);
				}

				@Override
				public void deleteByCode(String code) {
					throw new IllegalStateException("forced failure for rollback test");
				}
			};
		}
	}

	private final TelegramLinkConfirmationTransaction transaction;
	private final TelegramAccountRepository telegramAccountRepository;
	private final TelegramLinkRepository telegramLinkRepository;

	TelegramLinkConfirmationTransactionRollbackIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema,
			JdbcTemplate jdbcTemplate, TelegramLinkConfirmationTransaction transaction,
			TelegramAccountRepository telegramAccountRepository, TelegramLinkRepository telegramLinkRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.transaction = transaction;
		this.telegramAccountRepository = telegramAccountRepository;
		this.telegramLinkRepository = telegramLinkRepository;
	}

	@Test
	void shouldRollBackTenantSchemaWriteWhenPublicSchemaWriteFailsAfterIt() {
		UUID userId = UUID.randomUUID();
		PendingTelegramLink pending = new PendingTelegramLink("CODE1234", tenantSlug, userId,
				Instant.now().plusSeconds(60));

		try (var _ = TenantContextScope.open(schema)) {
			assertThatThrownBy(() -> transaction.execute(pending, "999")).isInstanceOf(IllegalStateException.class);
		}

		try (var _ = TenantContextScope.open(schema)) {
			assertThat(telegramAccountRepository.findByTelegramUserId("999")).isEmpty();
		}
		assertThat(telegramLinkRepository.findByTelegramUserId("999")).isEmpty();
	}
}
