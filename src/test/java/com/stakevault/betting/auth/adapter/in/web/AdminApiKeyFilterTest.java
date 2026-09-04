package com.stakevault.betting.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import tools.jackson.databind.ObjectMapper;

class AdminApiKeyFilterTest {

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
	private final LocaleResolver localeResolver = mock(LocaleResolver.class);
	private final AdminApiKeyFilter filter = new AdminApiKeyFilter(
			"correct-key", messageSource, localeResolver, new ObjectMapper());

	AdminApiKeyFilterTest() {
		messageSource.setBasename("messages");
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setDefaultEncoding("UTF-8");
	}

	@Test
	void shouldPassThroughNonAdminPathsWithoutCheckingHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/telegram-accounts/123");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void shouldEnforceKeyCheckOnPercentEncodedAdminPath() throws Exception {
		when(localeResolver.resolveLocale(any())).thenReturn(Locale.forLanguageTag("pt-BR"));
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/adm%69n/tenants");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(chain.getRequest()).isNull();
	}

	@Test
	void shouldAllowRequestWithMatchingAdminApiKey() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin/tenants");
		request.addHeader(AdminApiKeyFilter.ADMIN_API_KEY_HEADER, "correct-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void shouldReturn401WhenHeaderMissing() throws Exception {
		when(localeResolver.resolveLocale(any())).thenReturn(Locale.forLanguageTag("pt-BR"));
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin/tenants");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		assertThat(response.getContentAsString()).contains("\"type\":\"https://docs/errors/invalid-admin-api-key\"");
		assertThat(chain.getRequest()).isNull();
	}

	@Test
	void shouldReturn401WhenHeaderDoesNotMatch() throws Exception {
		when(localeResolver.resolveLocale(any())).thenReturn(Locale.forLanguageTag("en-US"));
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin/tenants");
		request.addHeader(AdminApiKeyFilter.ADMIN_API_KEY_HEADER, "wrong-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		assertThat(response.getContentAsString()).contains("Invalid admin key");
		assertThat(chain.getRequest()).isNull();
	}

	@Test
	void shouldLocalizeErrorBodyPerAcceptLanguage() throws Exception {
		when(localeResolver.resolveLocale(any())).thenReturn(Locale.forLanguageTag("es"));
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/admin/tenants");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertThat(response.getContentAsString()).contains("Clave de administrador inválida");
	}
}
