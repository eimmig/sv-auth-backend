package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JpaTelegramLinkRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final TelegramLinkRepository telegramLinkRepository;

	JpaTelegramLinkRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema,
			JdbcTemplate jdbcTemplate, TelegramLinkRepository telegramLinkRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.telegramLinkRepository = telegramLinkRepository;
	}

	@Test
	void shouldUpsertAndFindByTelegramUserIdEvenWithATenantContextOpen() {
		UUID userId = UUID.randomUUID();

		try (var _ = TenantContextScope.open(schema)) {
			telegramLinkRepository.upsert(new TelegramLink("111", tenantSlug, userId));
		}

		Optional<TelegramLink> found = telegramLinkRepository.findByTelegramUserId("111");

		assertThat(found).contains(new TelegramLink("111", tenantSlug, userId));
		Integer countInPublicSchema = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM public.telegram_link WHERE telegram_user_id = ?", Integer.class, "111");
		assertThat(countInPublicSchema).isEqualTo(1);
	}

	@Test
	void shouldOverwritePreviousLinkWhenUpsertingSameTelegramUserId() {
		UUID firstUserId = UUID.randomUUID();
		UUID secondUserId = UUID.randomUUID();
		telegramLinkRepository.upsert(new TelegramLink("222", "acme", firstUserId));

		telegramLinkRepository.upsert(new TelegramLink("222", "other-tenant", secondUserId));

		Optional<TelegramLink> found = telegramLinkRepository.findByTelegramUserId("222");
		assertThat(found).contains(new TelegramLink("222", "other-tenant", secondUserId));
	}

	@Test
	void shouldReturnEmptyWhenNotFound() {
		assertThat(telegramLinkRepository.findByTelegramUserId("does-not-exist")).isEmpty();
	}
}
