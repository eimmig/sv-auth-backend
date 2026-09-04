package com.stakevault.betting.auth.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.CreatedTenantAdmin;
import com.stakevault.betting.auth.domain.model.InvalidTenantSlugException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantAlreadyProvisionedException;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.CreateTenantUseCase;
import com.stakevault.betting.auth.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.auth.domain.port.out.PasswordHasher;
import com.stakevault.betting.auth.domain.port.out.TemporaryPasswordGenerator;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Service
public class CreateTenantService implements CreateTenantUseCase {

	private static final String DEFAULT_ADMIN_NAME = "Administrator";

	private final ProvisionTenantSchemaUseCase provisionTenantSchema;
	private final PasswordHasher passwordHasher;
	private final TemporaryPasswordGenerator passwordGenerator;
	private final UserRepository userRepository;

	public CreateTenantService(ProvisionTenantSchemaUseCase provisionTenantSchema, PasswordHasher passwordHasher,
			TemporaryPasswordGenerator passwordGenerator, UserRepository userRepository) {
		this.provisionTenantSchema = provisionTenantSchema;
		this.passwordHasher = passwordHasher;
		this.passwordGenerator = passwordGenerator;
		this.userRepository = userRepository;
	}

	@Override
	public CreatedTenantAdmin createTenant(String slug, String tenantName) {
		TenantSchemaName schema;
		try {
			schema = TenantSchemaName.fromSlug(slug);
		} catch (IllegalArgumentException cause) {
			throw new InvalidTenantSlugException(slug, cause);
		}

		if (provisionTenantSchema.exists(slug)) {
			throw new TenantAlreadyProvisionedException(slug);
		}
		provisionTenantSchema.ensureSchemaExists(slug);

		String rawPassword = passwordGenerator.generate();
		String passwordHash = passwordHasher.hash(rawPassword);
		String adminName = (tenantName == null || tenantName.isBlank()) ? DEFAULT_ADMIN_NAME : tenantName;
		String email = "admin@" + slug;

		User admin;
		try (var _ = TenantContextScope.open(schema)) {
			admin = userRepository.save(
					new User(UUID.randomUUID(), adminName, email, passwordHash, Role.ADMIN, true, Instant.now()));
		} catch (DataIntegrityViolationException concurrentProvisioning) {
			throw new TenantAlreadyProvisionedException(slug);
		}

		return new CreatedTenantAdmin(admin.id(), admin.email(), rawPassword);
	}
}
