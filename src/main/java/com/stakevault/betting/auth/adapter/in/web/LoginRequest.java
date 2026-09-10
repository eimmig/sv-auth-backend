package com.stakevault.betting.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank String slug,
		@NotBlank String email,
		@NotBlank String password) {
}
