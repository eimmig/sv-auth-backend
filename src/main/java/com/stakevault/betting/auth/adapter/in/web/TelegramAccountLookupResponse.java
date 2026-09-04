package com.stakevault.betting.auth.adapter.in.web;

import java.util.UUID;

public record TelegramAccountLookupResponse(UUID userId, String tenantId) {
}
