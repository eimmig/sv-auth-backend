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

/** Id ja vem preenchido do dominio - Persistable.isNew() evita que save() vire merge()+SELECT para toda insercao. */
@Entity
@Table(name = "telegram_accounts")
@Getter
@Setter
@NoArgsConstructor
public class TelegramAccountJpaEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "telegram_user_id")
	private String telegramUserId;

	@Column(name = "linked_at")
	private Instant linkedAt;

	@Transient
	private boolean isNew = true;

	public TelegramAccountJpaEntity(UUID id, UUID userId, String telegramUserId, Instant linkedAt) {
		this.id = id;
		this.userId = userId;
		this.telegramUserId = telegramUserId;
		this.linkedAt = linkedAt;
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
