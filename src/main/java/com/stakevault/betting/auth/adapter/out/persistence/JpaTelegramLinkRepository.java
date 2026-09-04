package com.stakevault.betting.auth.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkRepository;

@Repository
public class JpaTelegramLinkRepository implements TelegramLinkRepository {

	private final TelegramLinkJpaRepository jpaRepository;

	public JpaTelegramLinkRepository(TelegramLinkJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void upsert(TelegramLink telegramLink) {
		TelegramLinkJpaEntity entity = jpaRepository.findById(telegramLink.telegramUserId())
				.orElseGet(() -> new TelegramLinkJpaEntity(telegramLink.telegramUserId(), null, null));
		entity.setTenantId(telegramLink.tenantSlug());
		entity.setUserId(telegramLink.userId());
		jpaRepository.save(entity);
	}

	@Override
	public Optional<TelegramLink> findByTelegramUserId(String telegramUserId) {
		return jpaRepository.findById(telegramUserId).map(JpaTelegramLinkRepository::toDomain);
	}

	private static TelegramLink toDomain(TelegramLinkJpaEntity entity) {
		return new TelegramLink(entity.getTelegramUserId(), entity.getTenantId(), entity.getUserId());
	}
}
