package com.stakevault.betting.auth.adapter.in.web;

import java.time.Instant;

public record TelegramLinkStatusResponse(boolean linked, Instant linkedAt) {
}
