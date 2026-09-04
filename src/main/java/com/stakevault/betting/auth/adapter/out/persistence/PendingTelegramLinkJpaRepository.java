package com.stakevault.betting.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PendingTelegramLinkJpaRepository extends JpaRepository<PendingTelegramLinkJpaEntity, String> {
}
