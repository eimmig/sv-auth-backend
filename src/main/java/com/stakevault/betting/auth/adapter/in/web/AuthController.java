package com.stakevault.betting.auth.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.LoginResult;
import com.stakevault.betting.auth.domain.model.MissingCallerContextException;
import com.stakevault.betting.auth.domain.port.in.ChangePasswordUseCase;
import com.stakevault.betting.auth.domain.port.in.LoginUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	public static final String CALLER_HEADER = "X-User-Id";

	private final LoginUseCase login;
	private final ChangePasswordUseCase changePassword;

	public AuthController(LoginUseCase login, ChangePasswordUseCase changePassword) {
		this.login = login;
		this.changePassword = changePassword;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = login.login(request.slug(), request.email(), request.password());
		return ResponseEntity.ok(
				new LoginResponse(result.token(), result.mustChangePassword(), result.userId(), result.role()));
	}

	@PostMapping("/change-password")
	public ResponseEntity<Void> changePassword(
			@RequestHeader(value = CALLER_HEADER, required = false) String callerIdHeader,
			@Valid @RequestBody ChangePasswordRequest request) {
		UUID callerId = parseCallerId(callerIdHeader);
		changePassword.changePassword(callerId, request.currentPassword(), request.newPassword());
		return ResponseEntity.noContent().build();
	}

	private UUID parseCallerId(String callerIdHeader) {
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
