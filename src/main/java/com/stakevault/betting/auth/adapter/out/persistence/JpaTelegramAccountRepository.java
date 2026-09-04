package com.stakevault.betting.auth.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.stakevault.betting.auth.domain.model.TelegramAccount;
import com.stakevault.betting.auth.domain.port.out.TelegramAccountRepository;

@Component
public class JpaTelegramAccountRepository implements TelegramAccountRepository {

	private final TelegramAccountJpaRepository jpaRepository;

	public JpaTelegramAccountRepository(TelegramAccountJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public TelegramAccount save(TelegramAccount telegramAccount) {
		TelegramAccountJpaEntity saved = jpaRepository.save(toEntity(telegramAccount));
		return toDomain(saved);
	}

	@Override
	public Optional<TelegramAccount> findByUserId(UUID userId) {
		return jpaRepository.findByUserId(userId).map(JpaTelegramAccountRepository::toDomain);
	}

	@Override
	public Optional<TelegramAccount> findByTelegramUserId(String telegramUserId) {
		return jpaRepository.findByTelegramUserId(telegramUserId).map(JpaTelegramAccountRepository::toDomain);
	}

	private static TelegramAccountJpaEntity toEntity(TelegramAccount account) {
		return new TelegramAccountJpaEntity(account.id(), account.userId(), account.telegramUserId(), account.linkedAt());
	}

	private static TelegramAccount toDomain(TelegramAccountJpaEntity entity) {
		return new TelegramAccount(entity.getId(), entity.getUserId(), entity.getTelegramUserId(), entity.getLinkedAt());
	}
}
