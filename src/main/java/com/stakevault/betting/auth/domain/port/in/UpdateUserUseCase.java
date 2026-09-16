package com.stakevault.betting.auth.domain.port.in;

import java.util.UUID;

import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.User;

public interface UpdateUserUseCase {

	User updateUser(UUID callerId, UUID targetUserId, String name, Role role);
}
