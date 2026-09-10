package com.stakevault.betting.auth.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.CallerNotFoundException;
import com.stakevault.betting.auth.domain.model.GeneratedTelegramLinkCode;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.port.in.GenerateTelegramLinkCodeUseCase;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkCodeGenerator;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class GenerateTelegramLinkCodeService implements GenerateTelegramLinkCodeUseCase {

	private final UserRepository userRepository;
	private final TelegramLinkCodeGenerator codeGenerator;
	private final PendingTelegramLinkRepository pendingTelegramLinkRepository;
	private final long ttlMinutes;

	public GenerateTelegramLinkCodeService(UserRepository userRepository, TelegramLinkCodeGenerator codeGenerator,
			PendingTelegramLinkRepository pendingTelegramLinkRepository,
			@Value("${telegram.link-code-ttl-minutes}") long ttlMinutes) {
		this.userRepository = userRepository;
		this.codeGenerator = codeGenerator;
		this.pendingTelegramLinkRepository = pendingTelegramLinkRepository;
		this.ttlMinutes = ttlMinutes;
	}

	@Override
	public GeneratedTelegramLinkCode generateCode(UUID callerId) {
		var schema = TenantContextHolder.current();
		if (schema == null) {
			throw new MissingTenantContextException();
		}
		if (userRepository.findById(callerId).isEmpty()) {
			throw new CallerNotFoundException();
		}

		String code = codeGenerator.generate();
		Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);
		pendingTelegramLinkRepository.save(new PendingTelegramLink(code, schema.slug(), callerId, expiresAt));
		return new GeneratedTelegramLinkCode(code, expiresAt);
	}
}
