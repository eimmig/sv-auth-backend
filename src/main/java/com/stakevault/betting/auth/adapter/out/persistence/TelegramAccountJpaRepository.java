package com.stakevault.betting.auth.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface TelegramAccountJpaRepository extends JpaRepository<TelegramAccountJpaEntity, UUID> {

	Optional<TelegramAccountJpaEntity> findByUserId(UUID userId);

	Optional<TelegramAccountJpaEntity> findByTelegramUserId(String telegramUserId);
}
