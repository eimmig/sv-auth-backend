package com.stakevault.betting.auth.application;

import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.InvalidCredentialsException;
import com.stakevault.betting.auth.domain.model.LoginResult;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.LoginUseCase;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.AccessTokenIssuer;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class LoginService implements LoginUseCase {

	private final ProvisionTenantSchemaUseCase provisionTenantSchema;
	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;
	private final AccessTokenIssuer accessTokenIssuer;

	public LoginService(ProvisionTenantSchemaUseCase provisionTenantSchema, UserRepository userRepository,
			PasswordHasher passwordHasher, AccessTokenIssuer accessTokenIssuer) {
		this.provisionTenantSchema = provisionTenantSchema;
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
		this.accessTokenIssuer = accessTokenIssuer;
	}

	@Override
	public LoginResult login(String tenantSlug, String email, String rawPassword) {
		TenantSchemaName schema;
		try {
			schema = TenantSchemaName.fromSlug(tenantSlug);
		} catch (IllegalArgumentException _) {
			throw new InvalidCredentialsException();
		}

		if (!provisionTenantSchema.exists(tenantSlug)) {
			burnHashingCost(rawPassword);
			throw new InvalidCredentialsException();
		}
		provisionTenantSchema.migrateIfPending(tenantSlug);

		User user;
		try (var _ = TenantContextScope.open(schema)) {
			user = userRepository.findByEmail(email).orElse(null);
		}

		if (user == null) {
			burnHashingCost(rawPassword);
			throw new InvalidCredentialsException();
		}
		if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
			throw new InvalidCredentialsException();
		}

		String token = accessTokenIssuer.issue(user.id(), tenantSlug);
		return new LoginResult(token, user.mustChangePassword());
	}

	private void burnHashingCost(String rawPassword) {
		try {
			passwordHasher.hash(rawPassword);
		} catch (IllegalArgumentException _) {
			throw new InvalidCredentialsException();
		}
	}
}
