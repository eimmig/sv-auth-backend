package com.stakevault.betting.auth.adapter.in.web;

import java.util.UUID;

import com.stakevault.betting.auth.domain.model.Role;

public record LoginResponse(String token, boolean mustChangePassword, UUID userId, Role role) {
}
