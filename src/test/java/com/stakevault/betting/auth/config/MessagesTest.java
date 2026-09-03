package com.stakevault.betting.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

/** Resolve a mesma chave nos tres locales de mensagem. */
class MessagesTest {

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	MessagesTest() {
		messageSource.setBasename("messages");
		messageSource.setFallbackToSystemLocale(false);
	}

	@Test
	void resolveSmokeKey_ptBR() {
		assertThat(messageSource.getMessage("smoke.test", null, Locale.forLanguageTag("pt-BR")))
				.isEqualTo("Mensagem de teste do MessageSource");
	}

	@Test
	void resolveSmokeKey_enUS() {
		assertThat(messageSource.getMessage("smoke.test", null, Locale.forLanguageTag("en-US")))
				.isEqualTo("MessageSource test message");
	}

	@Test
	void resolveSmokeKey_es() {
		assertThat(messageSource.getMessage("smoke.test", null, Locale.forLanguageTag("es")))
				.isEqualTo("Mensaje de prueba de MessageSource");
	}
}
