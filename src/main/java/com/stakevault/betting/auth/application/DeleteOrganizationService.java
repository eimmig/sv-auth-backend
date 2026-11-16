package com.stakevault.betting.auth.application;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stakevault.betting.auth.config.TenantContextHolder;
import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.DownstreamProvisioningException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.OrganizationDeletionFailedException;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.port.in.DeleteOrganizationUseCase;
import com.stakevault.betting.auth.domain.port.out.DownstreamTenantProvisioner;
import com.stakevault.betting.auth.domain.port.out.TenantSchemaGateway;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

/**
 * Exclusao da organizacao a pedido do admin (epic-065, LGPD art. 18, VI). Os servicos abaixo sao
 * apagados primeiro: se um deles falhar, o auth-service mantem os usuarios, o admin continua podendo
 * entrar e repetir o pedido. So depois sao apagados os vinculos do Telegram e o schema do auth.
 */
@Service
public class DeleteOrganizationService implements DeleteOrganizationUseCase {

	private static final Logger log = LoggerFactory.getLogger(DeleteOrganizationService.class);

	private final UserRepository userRepository;
	private final DownstreamTenantProvisioner downstream;
	private final TenantSchemaGateway schemaGateway;

	public DeleteOrganizationService(UserRepository userRepository, DownstreamTenantProvisioner downstream,
			TenantSchemaGateway schemaGateway) {
		this.userRepository = userRepository;
		this.downstream = downstream;
		this.schemaGateway = schemaGateway;
	}

	@Override
	public void deleteOrganization(UUID callerId) {
		TenantSchemaName schema = TenantContextHolder.current();
		if (schema == null) {
			throw new MissingTenantContextException();
		}
		boolean callerIsAdmin = userRepository.findById(callerId)
				.map(caller -> caller.role() == Role.ADMIN)
				.orElse(false);
		if (!callerIsAdmin) {
			throw new AdminRoleRequiredException();
		}

		String slug = schema.slug();
		try {
			downstream.deleteFromBetsService(slug);
			downstream.deleteFromStatsService(slug);
		}
		catch (DownstreamProvisioningException e) {
			log.warn("Organization '{}' not deleted: {}", slug, e.getMessage());
			throw new OrganizationDeletionFailedException(e.serviceName(), e);
		}
		schemaGateway.drop(schema);
		log.info("Organization '{}' deleted by {}", slug, callerId);
	}
}
