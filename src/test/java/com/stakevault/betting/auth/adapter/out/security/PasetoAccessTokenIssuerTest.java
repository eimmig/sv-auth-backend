package com.stakevault.betting.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HexFormat;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.paseto4j.commons.SecretKey;
import org.paseto4j.commons.Version;
import org.paseto4j.version4.Paseto;

import tools.jackson.databind.ObjectMapper;
import com.stakevault.betting.auth.domain.model.Role;

class PasetoAccessTokenIssuerTest {

	private static final String KEY_HEX = "93f681c1304df4c64f73a0df5c58c7eb097927246ad5b850247a97ef14774bc7";

	private final PasetoAccessTokenIssuer issuer = new PasetoAccessTokenIssuer(KEY_HEX, 480, new ObjectMapper());

	@Test
	void shouldIssueTokenStartingWithV4LocalHeader() {
		String token = issuer.issue(UUID.randomUUID(), "acme", Role.MEMBER);

		assertThat(token).startsWith("v4.local.");
	}

	@Test
	void shouldIssueTokenDecryptableWithSameKeyCarryingUserIdTenantIdAndRole() {
		UUID userId = UUID.randomUUID();

		String token = issuer.issue(userId, "acme", Role.ADMIN);

		SecretKey key = new SecretKey(HexFormat.of().parseHex(KEY_HEX), Version.V4);
		String claimsJson = Paseto.decrypt(key, token, "");

		assertThat(claimsJson)
				.contains("\"userId\":\"" + userId + "\"")
				.contains("\"tenantId\":\"acme\"")
				.contains("\"role\":\"ADMIN\"")
				.contains("\"iat\":")
				.contains("\"exp\":");
	}

	@Test
	void shouldIssueDifferentTokensForSameInputDueToRandomNonce() {
		UUID userId = UUID.randomUUID();

		assertThat(issuer.issue(userId, "acme", Role.MEMBER)).isNotEqualTo(issuer.issue(userId, "acme", Role.MEMBER));
	}
}
