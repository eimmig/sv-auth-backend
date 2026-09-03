package com.stakevault.betting.auth.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Regra de dominio de docs/CONVENTIONS.md secao "Migrations": nome do schema e sempre
 * `tenant_<slug>`, e o slug segue um formato restrito (Postgres nao aceita identificador
 * sem aspas com hifen - ver JdbcFlywayTenantSchemaGateway.create, que aspeia o valor).
 * So indiretamente coberto ate aqui (via um unico caso invalido em TenantSchemaFilterTest)
 * - teste dedicado fecha a lacuna apontada pelo Test Suite Auditor.
 */
class TenantSchemaNameTest {

	@Test
	void fromSlug_derivaPrefixoTenant() {
		assertThat(TenantSchemaName.fromSlug("acme").value()).isEqualTo("tenant_acme");
	}

	@Test
	void fromSlug_aceitaHifenNoMeio() {
		assertThat(TenantSchemaName.fromSlug("acme-corp").value()).isEqualTo("tenant_acme-corp");
	}

	@Test
	void fromSlug_rejeitaComecarComDigito() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("1acme")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void fromSlug_rejeitaMaiuscula() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("ACME")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void fromSlug_rejeitaCaractereInvalido() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("acme!")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void fromSlug_rejeitaSlugDeUmCaractereSo() {
		// Regex exige pelo menos 2 caracteres (1 letra + 1 a mais) - um slug de 1
		// caractere e um limite deliberado, nao um acidente do regex.
		assertThatThrownBy(() -> TenantSchemaName.fromSlug("a")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void fromSlug_rejeitaNulo() {
		assertThatThrownBy(() -> TenantSchemaName.fromSlug(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void construtor_rejeitaValorSemPrefixoTenant() {
		assertThatThrownBy(() -> new TenantSchemaName("acme")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void construtor_rejeitaValorNulo() {
		assertThatThrownBy(() -> new TenantSchemaName(null)).isInstanceOf(IllegalArgumentException.class);
	}
}
