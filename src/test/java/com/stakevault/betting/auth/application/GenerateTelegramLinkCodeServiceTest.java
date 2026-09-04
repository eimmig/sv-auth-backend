package com.stakevault.betting.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.config.TenantContextScope;
import com.stakevault.betting.auth.domain.model.CallerNotFoundException;
import com.stakevault.betting.auth.domain.model.GeneratedTelegramLinkCode;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.PendingTelegramLink;
import com.stakevault.betting.auth.domain.model.Role;
import com.stakevault.betting.auth.domain.model.TenantSchemaName;
import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.PendingTelegramLinkRepository;
import com.stakevault.betting.auth.domain.port.out.TelegramLinkCodeGenerator;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

class GenerateTelegramLinkCodeServiceTest {

	private static final UUID CALLER_ID = UUID.randomUUID();

	private final UserRepository userRepository = mock(UserRepository.class);
	private final TelegramLinkCodeGenerator codeGenerator = mock(TelegramLinkCodeGenerator.class);
	private final PendingTelegramLinkRepository pendingTelegramLinkRepository = mock(PendingTelegramLinkRepository.class);
	private final GenerateTelegramLinkCodeService service = new GenerateTelegramLinkCodeService(userRepository,
			codeGenerator, pendingTelegramLinkRepository, 15);

	@Test
	void shouldGenerateCodeAndSavePendingLinkWhenCallerExists() {
		try (var _ = TenantContextScope.open(TenantSchemaName.fromSlug("acme"))) {
			when(userRepository.findById(CALLER_ID))
					.thenReturn(Optional.of(new User(CALLER_ID, "Ana", "ana@acme", "hash", Role.MEMBER, false, Instant.now())));
			when(codeGenerator.generate()).thenReturn("ABC12345");

			GeneratedTelegramLinkCode result = service.generateCode(CALLER_ID);

			assertThat(result.code()).isEqualTo("ABC12345");
			verify(pendingTelegramLinkRepository).save(argThatMatches(pending -> pending.code().equals("ABC12345")
					&& pending.tenantSlug().equals("acme") && pending.userId().equals(CALLER_ID)));
		}
	}

	@Test
	void shouldRejectWhenNoTenantContextIsOpen() {
		assertThatThrownBy(() -> service.generateCode(CALLER_ID)).isInstanceOf(MissingTenantContextException.class);

		verifyNoInteractions(userRepository, codeGenerator, pendingTelegramLinkRepository);
	}

	@Test
	void shouldRejectWhenCallerDoesNotExist() {
		try (var _ = TenantContextScope.open(TenantSchemaName.fromSlug("acme"))) {
			when(userRepository.findById(CALLER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.generateCode(CALLER_ID)).isInstanceOf(CallerNotFoundException.class);

			verifyNoInteractions(codeGenerator, pendingTelegramLinkRepository);
		}
	}

	private PendingTelegramLink argThatMatches(Predicate<PendingTelegramLink> predicate) {
		return argThat(predicate::test);
	}
}
