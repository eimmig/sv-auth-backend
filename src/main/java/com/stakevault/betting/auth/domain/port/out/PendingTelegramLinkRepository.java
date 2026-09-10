package com.stakevault.betting.auth.domain.port.out;

import java.util.Optional;

import com.stakevault.betting.auth.domain.model.PendingTelegramLink;

public interface PendingTelegramLinkRepository {

	void save(PendingTelegramLink pendingTelegramLink);

	Optional<PendingTelegramLink> findByCode(String code);

	void deleteByCode(String code);
}
