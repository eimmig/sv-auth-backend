package com.stakevault.betting.auth.adapter.out.persistence;

import com.stakevault.betting.auth.domain.model.Role;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleAttributeConverter implements AttributeConverter<Role, String> {

	@Override
	public String convertToDatabaseColumn(Role role) {
		return role == null ? null : role.name().toLowerCase();
	}

	@Override
	public Role convertToEntityAttribute(String dbValue) {
		return dbValue == null ? null : Role.valueOf(dbValue.toUpperCase());
	}
}
