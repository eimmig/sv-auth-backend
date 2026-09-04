package com.stakevault.betting.auth.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.LoginResult;
import com.stakevault.betting.auth.domain.port.in.LoginUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final LoginUseCase login;

	public AuthController(LoginUseCase login) {
		this.login = login;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = login.login(request.slug(), request.email(), request.password());
		return ResponseEntity.ok(new LoginResponse(result.token(), result.mustChangePassword()));
	}
}
