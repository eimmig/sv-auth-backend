package com.stakevault.betting.auth.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.TelegramLink;
import com.stakevault.betting.auth.domain.port.in.ConfirmTelegramLinkUseCase;
import com.stakevault.betting.auth.domain.port.in.LookupTelegramAccountUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/telegram-accounts")
public class TelegramAccountsController {

	private final ConfirmTelegramLinkUseCase confirmTelegramLink;
	private final LookupTelegramAccountUseCase lookupTelegramAccount;

	public TelegramAccountsController(ConfirmTelegramLinkUseCase confirmTelegramLink,
			LookupTelegramAccountUseCase lookupTelegramAccount) {
		this.confirmTelegramLink = confirmTelegramLink;
		this.lookupTelegramAccount = lookupTelegramAccount;
	}

	@PostMapping
	public ResponseEntity<Void> confirm(@Valid @RequestBody ConfirmTelegramLinkRequest request) {
		confirmTelegramLink.confirm(request.telegramUserId(), request.code());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/{telegramUserId}")
	public ResponseEntity<TelegramAccountLookupResponse> lookup(@PathVariable String telegramUserId) {
		TelegramLink link = lookupTelegramAccount.lookup(telegramUserId);
		return ResponseEntity.ok(new TelegramAccountLookupResponse(link.userId(), link.tenantSlug()));
	}
}
