package com.stakevault.betting.auth.domain.port.in;

import java.util.List;
import java.util.UUID;

import com.stakevault.betting.auth.domain.model.User;

public interface ListUsersUseCase {

	List<User> listUsers(UUID callerId);
}
