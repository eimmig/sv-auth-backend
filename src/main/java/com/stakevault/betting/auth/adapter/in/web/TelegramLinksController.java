package com.stakevault.betting.auth.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.GeneratedTelegramLinkCode;
import com.stakevault.betting.auth.domain.model.MissingCallerContextException;
import com.stakevault.betting.auth.domain.model.TelegramLinkStatus;
import com.stakevault.betting.auth.domain.port.in.GenerateTelegramLinkCodeUseCase;
import com.stakevault.betting.auth.domain.port.in.GetTelegramLinkStatusUseCase;

@RestController
@RequestMapping("/api/v1/telegram-links")
public class TelegramLinksController {

	public static final String CALLER_HEADER = "X-User-Id";

	private final GenerateTelegramLinkCodeUseCase generateTelegramLinkCode;
	private final GetTelegramLinkStatusUseCase getTelegramLinkStatus;

	public TelegramLinksController(GenerateTelegramLinkCodeUseCase generateTelegramLinkCode,
			GetTelegramLinkStatusUseCase getTelegramLinkStatus) {
		this.generateTelegramLinkCode = generateTelegramLinkCode;
		this.getTelegramLinkStatus = getTelegramLinkStatus;
	}

	@PostMapping
	public ResponseEntity<GenerateTelegramLinkResponse> create(
			@RequestHeader(value = CALLER_HEADER, required = false) String callerIdHeader) {
		UUID callerId = parseCallerId(callerIdHeader);
		GeneratedTelegramLinkCode generated = generateTelegramLinkCode.generateCode(callerId);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new GenerateTelegramLinkResponse(generated.code(), generated.expiresAt()));
	}

	@GetMapping("/me")
	public TelegramLinkStatusResponse me(
			@RequestHeader(value = CALLER_HEADER, required = false) String callerIdHeader) {
		TelegramLinkStatus status = getTelegramLinkStatus.statusOf(parseCallerId(callerIdHeader));
		return new TelegramLinkStatusResponse(status.linked(), status.linkedAt());
	}

	private UUID parseCallerId(String callerIdHeader) {
		if (callerIdHeader == null || callerIdHeader.isBlank()) {
			throw new MissingCallerContextException();
		}
		try {
			return UUID.fromString(callerIdHeader);
		} catch (IllegalArgumentException _) {
			throw new MissingCallerContextException();
		}
	}
}
