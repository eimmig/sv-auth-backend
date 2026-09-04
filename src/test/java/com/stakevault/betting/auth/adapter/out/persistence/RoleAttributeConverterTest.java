package com.stakevault.betting.auth.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.stakevault.betting.auth.domain.model.Role;

class RoleAttributeConverterTest {

	private final RoleAttributeConverter converter = new RoleAttributeConverter();

	@Test
	void convertToDatabaseColumn_gravaMinusculo() {
		assertThat(converter.convertToDatabaseColumn(Role.ADMIN)).isEqualTo("admin");
		assertThat(converter.convertToDatabaseColumn(Role.MEMBER)).isEqualTo("member");
	}

	@Test
	void convertToEntityAttribute_leMinusculo() {
		assertThat(converter.convertToEntityAttribute("admin")).isEqualTo(Role.ADMIN);
		assertThat(converter.convertToEntityAttribute("member")).isEqualTo(Role.MEMBER);
	}

	@Test
	void nuloEmAmbasDirecoes() {
		assertThat(converter.convertToDatabaseColumn(null)).isNull();
		assertThat(converter.convertToEntityAttribute(null)).isNull();
	}
}
