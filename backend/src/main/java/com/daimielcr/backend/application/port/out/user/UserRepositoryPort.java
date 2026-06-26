package com.daimielcr.backend.application.port.out.user;

import java.util.Optional;

import com.daimielcr.backend.domain.model.user.User;
import com.daimielcr.backend.domain.model.user.UserId;

public interface UserRepositoryPort {

    Optional<User> findById(UserId userId);
}
