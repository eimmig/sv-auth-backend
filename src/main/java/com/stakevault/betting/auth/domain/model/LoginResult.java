package com.stakevault.betting.auth.domain.model;

public record LoginResult(String token, boolean mustChangePassword) {
}
