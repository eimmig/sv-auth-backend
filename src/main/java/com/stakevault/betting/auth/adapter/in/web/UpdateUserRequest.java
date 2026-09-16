package com.stakevault.betting.auth.adapter.in.web;

import com.stakevault.betting.auth.domain.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
		@NotBlank String name,
		@NotNull Role role) {
}
