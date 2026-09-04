package com.stakevault.betting.auth.adapter.out.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import com.stakevault.betting.auth.domain.port.out.AccessTokenIssuer;

@Component
public class PasetoAccessTokenIssuer implements AccessTokenIssuer {

	private final SecretKey localKey;
	private final long accessTokenTtlMinutes;
	private final ObjectMapper objectMapper;

	public PasetoAccessTokenIssuer(@Value("${paseto.local-key}") String localKeyHex,
			@Value("${paseto.access-token-ttl-minutes}") long accessTokenTtlMinutes, ObjectMapper objectMapper) {
		this.localKey = new SecretKey(HexFormat.of().parseHex(localKeyHex), Version.V4);
		this.accessTokenTtlMinutes = accessTokenTtlMinutes;
		this.objectMapper = objectMapper;
	}

	@Override
	public String issue(UUID userId, String tenantSlug) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(accessTokenTtlMinutes, ChronoUnit.MINUTES);
		PasetoClaims claims = new PasetoClaims(userId.toString(), tenantSlug, now.getEpochSecond(),
				expiresAt.getEpochSecond());
		String payload = objectMapper.writeValueAsString(claims);
		return Paseto.encrypt(localKey, payload, "");
	}

	record PasetoClaims(String userId, String tenantId, long iat, long exp) {
	}
}
