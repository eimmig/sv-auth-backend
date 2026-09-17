package com.stakevault.betting.auth.domain.port.in;

import java.util.UUID;

public interface ChangePasswordUseCase {

	void changePassword(UUID callerId, String currentPassword, String newPassword);
}
