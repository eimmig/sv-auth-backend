package com.stakevault.betting.auth.domain.model;

/**
 * Um servico abaixo nao confirmou a exclusao da organizacao. Nada foi apagado no auth-service, e o
 * pedido pode ser repetido (as rotas de exclusao dos servicos sao idempotentes).
 */
public class OrganizationDeletionFailedException extends LocalizedRuntimeException {

	public OrganizationDeletionFailedException(String serviceName, Throwable cause) {
		super("organization deletion failed in " + serviceName, cause);
	}

	@Override
	public String messageKey() {
		return "error.organization-deletion-failed";
	}

	@Override
	public int httpStatusCode() {
		return 502;
	}
}
