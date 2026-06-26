package com.daimielcr.backend.adapter.out.persistence.user;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.daimielcr.backend.application.port.out.user.UserRepositoryPort;
import com.daimielcr.backend.domain.model.user.User;
import com.daimielcr.backend.domain.model.user.UserId;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository userRepository;

    public UserPersistenceAdapter(SpringDataUserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "El repositorio JPA de usuarios es obligatorio"
        );
    }

    @Override
    public Optional<User> findById(UserId userId) {
        Objects.requireNonNull(userId, "El id de usuario es obligatorio");

        return userRepository.findById(userId.value())
                .map(UserPersistenceMapper::toDomain);
    }
}