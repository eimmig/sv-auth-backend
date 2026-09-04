package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.support.TenantSchemaIntegrationSupport;

class JpaPendingTelegramLinkRepositoryIntegrationTest extends TenantSchemaIntegrationSupport {

	private final PendingTelegramLinkRepository pendingTelegramLinkRepository;

	JpaPendingTelegramLinkRepositoryIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema,
			JdbcTemplate jdbcTemplate, PendingTelegramLinkRepository pendingTelegramLinkRepository) {
		super(provisionTenantSchema, jdbcTemplate);
		this.pendingTelegramLinkRepository = pendingTelegramLinkRepository;
	}

	@Test
	void shouldSaveAndFindByCode() {
		UUID userId = UUID.randomUUID();
		Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES).truncatedTo(DB_PRECISION);
		PendingTelegramLink pending = new PendingTelegramLink("ABC12345", tenantSlug, userId, expiresAt);

		pendingTelegramLinkRepository.save(pending);

		assertThat(pendingTelegramLinkRepository.findByCode("ABC12345")).contains(pending);
	}

	@Test
	void shouldReturnEmptyWhenCodeNotFound() {
		assertThat(pendingTelegramLinkRepository.findByCode("MISSING1")).isEmpty();
	}

	@Test
	void shouldDeleteByCode() {
		PendingTelegramLink pending = new PendingTelegramLink("DEL12345", tenantSlug, UUID.randomUUID(),
				Instant.now().plusSeconds(60));
		pendingTelegramLinkRepository.save(pending);

		pendingTelegramLinkRepository.deleteByCode("DEL12345");

		assertThat(pendingTelegramLinkRepository.findByCode("DEL12345")).isEmpty();
	}

	@Test
	void shouldNotFailWhenDeletingCodeThatDoesNotExist() {
		Optional<PendingTelegramLink> before = pendingTelegramLinkRepository.findByCode("GHOST123");
		assertThat(before).isEmpty();

		pendingTelegramLinkRepository.deleteByCode("GHOST123");
	}
}
