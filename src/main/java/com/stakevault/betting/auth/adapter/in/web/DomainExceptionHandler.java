package com.stakevault.betting.auth.adapter.in.web;

import java.net.URI;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.stakevault.betting.auth.domain.model.AdminRoleRequiredException;
import com.stakevault.betting.auth.domain.model.CallerNotFoundException;
import com.stakevault.betting.auth.domain.model.EmailAlreadyRegisteredException;
import com.stakevault.betting.auth.domain.model.InvalidCredentialsException;
import com.stakevault.betting.auth.domain.model.InvalidTenantSlugException;
import com.stakevault.betting.auth.domain.model.LocalizedDomainException;
import com.stakevault.betting.auth.domain.model.MissingCallerContextException;
import com.stakevault.betting.auth.domain.model.MissingTenantContextException;
import com.stakevault.betting.auth.domain.model.TelegramAccountAlreadyLinkedException;
import com.stakevault.betting.auth.domain.model.TelegramAccountNotFoundException;
import com.stakevault.betting.auth.domain.model.TelegramLinkCodeExpiredException;
import com.stakevault.betting.auth.domain.model.TelegramLinkCodeNotFoundException;
import com.stakevault.betting.auth.domain.model.TenantAlreadyProvisionedException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class DomainExceptionHandler {

	private final MessageSource messageSource;

	public DomainExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	// InvalidAdminApiKeyException fires from AdminApiKeyFilter, before DispatcherServlet - not listed here on purpose.
	@ExceptionHandler({ TenantAlreadyProvisionedException.class, InvalidTenantSlugException.class,
			MissingCallerContextException.class, MissingTenantContextException.class,
			AdminRoleRequiredException.class, EmailAlreadyRegisteredException.class,
			InvalidCredentialsException.class, CallerNotFoundException.class,
			TelegramLinkCodeNotFoundException.class, TelegramLinkCodeExpiredException.class,
			TelegramAccountAlreadyLinkedException.class, TelegramAccountNotFoundException.class })
	public ProblemDetail handle(LocalizedDomainException exception, Locale locale, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.valueOf(exception.httpStatusCode()),
				ProblemDetailMessages.detail(exception, locale, messageSource));
		problem.setTitle(ProblemDetailMessages.title(exception, locale, messageSource));
		problem.setType(URI.create("https://docs/errors/" + ProblemDetailMessages.typeSlug(exception)));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception, Locale locale,
			HttpServletRequest request) {
		String fields = exception.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getField)
				.distinct()
				.collect(Collectors.joining(", "));
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				messageSource.getMessage("error.validation-failed.detail", new Object[] { fields }, locale));
		problem.setTitle(messageSource.getMessage("error.validation-failed.title", null, locale));
		problem.setType(URI.create("https://docs/errors/validation-failed"));
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}
}
