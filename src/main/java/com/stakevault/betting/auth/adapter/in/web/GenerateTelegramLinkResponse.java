package com.stakevault.betting.auth.adapter.in.web;

import java.time.Instant;

public record GenerateTelegramLinkResponse(String code, Instant expiresAt) {
}
