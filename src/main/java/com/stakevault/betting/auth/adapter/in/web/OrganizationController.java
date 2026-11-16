package com.stakevault.betting.auth.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.MissingCallerContextException;
import com.stakevault.betting.auth.domain.port.in.DeleteOrganizationUseCase;

@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {

	private final DeleteOrganizationUseCase deleteOrganization;

	public OrganizationController(DeleteOrganizationUseCase deleteOrganization) {
		this.deleteOrganization = deleteOrganization;
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(
			@RequestHeader(value = UsersController.CALLER_HEADER, required = false) String callerIdHeader) {
		deleteOrganization.deleteOrganization(parseCallerId(callerIdHeader));
		return ResponseEntity.noContent().build();
	}

	private static UUID parseCallerId(String callerIdHeader) {
		if (callerIdHeader == null || callerIdHeader.isBlank()) {
			throw new MissingCallerContextException();
		}
		try {
			return UUID.fromString(callerIdHeader);
		} catch (IllegalArgumentException _) {
			throw new MissingCallerContextException();
		}
	}
}
