package com.stakevault.betting.auth.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stakevault.betting.auth.domain.model.MissingCallerContextException;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.in.CreateUserUseCase;
import com.stakevault.betting.auth.domain.port.in.ListUsersUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

	public static final String CALLER_HEADER = "X-User-Id";

	private final CreateUserUseCase createUser;
	private final ListUsersUseCase listUsers;

	public UsersController(CreateUserUseCase createUser, ListUsersUseCase listUsers) {
		this.createUser = createUser;
		this.listUsers = listUsers;
	}

	@PostMapping
	public ResponseEntity<CreateUserResponse> create(
			@RequestHeader(value = CALLER_HEADER, required = false) String callerIdHeader,
			@Valid @RequestBody CreateUserRequest request) {
		UUID callerId = parseCallerId(callerIdHeader);
		User created = createUser.createUser(callerId, request.name(), request.email(), request.password());
		return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(created.id(), created.name(),
				created.email(), created.role(), created.mustChangePassword(), created.createdAt()));
	}

	@GetMapping
	public ResponseEntity<List<UserSummaryResponse>> list(
			@RequestHeader(value = CALLER_HEADER, required = false) String callerIdHeader) {
		UUID callerId = parseCallerId(callerIdHeader);
		List<UserSummaryResponse> users = listUsers.listUsers(callerId).stream()
				.map(user -> new UserSummaryResponse(user.id(), user.name(), user.email(), user.role(),
						user.mustChangePassword(), user.createdAt()))
				.toList();
		return ResponseEntity.ok(users);
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
