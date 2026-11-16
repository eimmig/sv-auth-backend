package com.stakevault.betting.auth.domain.port.in;

import java.util.UUID;

public interface DeleteOrganizationUseCase {

	/** Apaga todos os dados da organizacao do chamador, que precisa ser admin dela (epic-065). */
	void deleteOrganization(UUID callerId);
}
