package com.stakevault.betting.auth.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import com.stakevault.betting.auth.domain.model.Role;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserJpaEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	private String name;

	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	private Role role;

	@Column(name = "must_change_password")
	private boolean mustChangePassword;

	@Column(name = "created_at")
	private Instant createdAt;

	@Transient
	private boolean isNew = true;

	public UserJpaEntity(UUID id, String name, String email, String passwordHash, Role role,
			boolean mustChangePassword, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.mustChangePassword = mustChangePassword;
		this.createdAt = createdAt;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	@PostLoad
	void markNotNew() {
		this.isNew = false;
	}
}
