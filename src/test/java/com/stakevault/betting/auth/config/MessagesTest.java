package com.stakevault.betting.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Prova que os tres arquivos de mensagem (messages_pt_BR/en_US/es.properties) resolvem
 * a mesma chave nos tres locales - ver docs/CONVENTIONS.md "Internacionalizacao (i18n)".
 * Teste puro (sem subir contexto Spring): o mesmo mecanismo (ResourceBundleMessageSource
 * com basename "messages") que o Spring Boot autoconfigura a partir dessas propriedades.
 */
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
