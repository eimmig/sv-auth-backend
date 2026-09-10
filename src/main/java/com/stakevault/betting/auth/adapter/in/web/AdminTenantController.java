package com.stakevault.betting.auth.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.CreatedTenantAdmin;
import com.stakevault.betting.auth.domain.port.in.CreateTenantUseCase;

@RestController
@RequestMapping("/api/v1/admin/tenants")
public class AdminTenantController {

	private final CreateTenantUseCase createTenant;

	public AdminTenantController(CreateTenantUseCase createTenant) {
		this.createTenant = createTenant;
	}

	@PostMapping
	public ResponseEntity<CreateTenantResponse> create(@RequestBody CreateTenantRequest request) {
		CreatedTenantAdmin admin = createTenant.createTenant(request.slug(), request.tenantName());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new CreateTenantResponse(admin.userId(), admin.email(), admin.temporaryPassword()));
	}
}
