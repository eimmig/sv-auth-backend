package com.stakevault.betting.auth.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.stakevault.betting.auth.domain.model.User;
import com.stakevault.betting.auth.domain.port.out.UserRepository;

@Repository
public class JpaUserRepository implements UserRepository {

	private final UserJpaRepository jpaRepository;

	public JpaUserRepository(UserJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public User save(User user) {
		UserJpaEntity saved = jpaRepository.save(toEntity(user));
		return toDomain(saved);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return jpaRepository.findById(id).map(JpaUserRepository::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpaRepository.findByEmail(email).map(JpaUserRepository::toDomain);
	}

	private static UserJpaEntity toEntity(User user) {
		return new UserJpaEntity(user.id(), user.name(), user.email(), user.passwordHash(), user.role(),
				user.mustChangePassword(), user.createdAt());
	}

	private static User toDomain(UserJpaEntity entity) {
		return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getPasswordHash(),
				entity.getRole(), entity.isMustChangePassword(), entity.getCreatedAt());
	}
}
