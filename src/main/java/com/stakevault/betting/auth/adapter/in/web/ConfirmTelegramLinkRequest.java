package com.stakevault.betting.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record ConfirmTelegramLinkRequest(
		@NotBlank String telegramUserId,
		@NotBlank String code) {
}
