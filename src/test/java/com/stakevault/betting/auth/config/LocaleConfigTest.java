package com.stakevault.betting.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

class LocaleConfigTest {

	private final LocaleResolver resolver = new LocaleConfig().localeResolver();

	@Test
	void semAcceptLanguage_resolvePtBR() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.forLanguageTag("pt-BR"));
	}

	@Test
	void acceptLanguageSuportado_resolveOMesmoLocale() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Accept-Language", "en-US");

		assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.forLanguageTag("en-US"));
	}

	@Test
	void acceptLanguageNaoSuportado_caiNoDefaultPtBR() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Accept-Language", "fr-FR");

		assertThat(resolver.resolveLocale(request)).isEqualTo(Locale.forLanguageTag("pt-BR"));
	}
}
