package com.stakevault.betting.auth.config;

import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Locale resolvido pelo header Accept-Language, restrito aos 3 idiomas suportados -
 * ver docs/CONVENTIONS.md "Internacionalizacao (i18n)". Sem supportedLocales,
 * AcceptHeaderLocaleResolver aceita qualquer locale do header verbatim (ex.: fr-FR),
 * o que quebraria o fallback deterministico para pt-BR exigido pela convencao.
 */
@Configuration
public class LocaleConfig {

	private static final List<Locale> SUPPORTED_LOCALES = List.of(
			Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("en-US"), Locale.forLanguageTag("es"));
	private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("pt-BR");

	@Bean
	LocaleResolver localeResolver() {
		AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
		resolver.setSupportedLocales(SUPPORTED_LOCALES);
		resolver.setDefaultLocale(DEFAULT_LOCALE);
		return resolver;
	}
}
