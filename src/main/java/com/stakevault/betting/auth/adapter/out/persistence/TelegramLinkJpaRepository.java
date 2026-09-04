package com.stakevault.betting.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface TelegramLinkJpaRepository extends JpaRepository<TelegramLinkJpaEntity, String> {
}
