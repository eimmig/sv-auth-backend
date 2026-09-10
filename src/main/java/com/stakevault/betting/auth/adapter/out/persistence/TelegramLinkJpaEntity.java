package com.stakevault.betting.auth.adapter.out.persistence;

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
@Table(name = "telegram_link", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class TelegramLinkJpaEntity implements Persistable<String> {

	@Id
	@Column(name = "telegram_user_id")
	private String telegramUserId;

	@Column(name = "tenant_id")
	private String tenantId;

	@Column(name = "user_id")
	private UUID userId;

	@Transient
	private boolean isNew = true;

	public TelegramLinkJpaEntity(String telegramUserId, String tenantId, UUID userId) {
		this.telegramUserId = telegramUserId;
		this.tenantId = tenantId;
		this.userId = userId;
	}

	@Override
	public String getId() {
		return telegramUserId;
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
