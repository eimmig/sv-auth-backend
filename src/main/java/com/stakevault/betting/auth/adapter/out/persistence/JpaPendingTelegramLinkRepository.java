package com.stakevault.betting.auth.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;

@Repository
public class JpaPendingTelegramLinkRepository implements PendingTelegramLinkRepository {

	private final PendingTelegramLinkJpaRepository jpaRepository;

	public JpaPendingTelegramLinkRepository(PendingTelegramLinkJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void save(PendingTelegramLink pendingTelegramLink) {
		jpaRepository.save(toEntity(pendingTelegramLink));
	}

	@Override
	public Optional<PendingTelegramLink> findByCode(String code) {
		return jpaRepository.findById(code).map(JpaPendingTelegramLinkRepository::toDomain);
	}

	@Override
	public void deleteByCode(String code) {
		jpaRepository.deleteById(code);
	}

	private static PendingTelegramLinkJpaEntity toEntity(PendingTelegramLink pendingTelegramLink) {
		return new PendingTelegramLinkJpaEntity(pendingTelegramLink.code(), pendingTelegramLink.tenantSlug(),
				pendingTelegramLink.userId(), pendingTelegramLink.expiresAt());
	}

	private static PendingTelegramLink toDomain(PendingTelegramLinkJpaEntity entity) {
		return new PendingTelegramLink(entity.getCode(), entity.getTenantId(), entity.getUserId(), entity.getExpiresAt());
	}
}
