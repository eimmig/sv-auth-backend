package com.stakevault.betting.auth.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

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
@Table(name = "pending_telegram_link", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class PendingTelegramLinkJpaEntity implements Persistable<String> {

	@Id
	private String code;

	@Column(name = "tenant_id")
	private String tenantId;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Transient
	private boolean isNew = true;

	public PendingTelegramLinkJpaEntity(String code, String tenantId, UUID userId, Instant expiresAt) {
		this.code = code;
		this.tenantId = tenantId;
		this.userId = userId;
		this.expiresAt = expiresAt;
	}

	@Override
	public String getId() {
		return code;
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
