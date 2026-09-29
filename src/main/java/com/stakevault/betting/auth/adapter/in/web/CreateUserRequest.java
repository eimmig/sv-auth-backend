package com.stakevault.betting.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
		@NotBlank String name,
		@NotBlank @Pattern(regexp = "^[a-zA-Z0-9._-]{1,64}$") String username,
		@NotBlank String password) {
}
