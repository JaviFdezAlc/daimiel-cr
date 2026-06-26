package com.daimielcr.backend.adapter.out.persistence.user;

import java.util.Objects;

import com.daimielcr.backend.domain.model.user.DisplayName;
import com.daimielcr.backend.domain.model.user.PhoneNumber;
import com.daimielcr.backend.domain.model.user.User;
import com.daimielcr.backend.domain.model.user.UserId;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        Objects.requireNonNull(entity, "La entidad de usuario es obligatoria");

        return User.restore(
                new UserId(entity.getId()),
                new PhoneNumber(entity.getPhoneNumber()),
                new DisplayName(entity.getDisplayName()),
                entity.getProfileImageUrl(),
                entity.getPhoneVerificationStatus(),
                entity.getRole(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static UserJpaEntity toEntity(User user) {
        Objects.requireNonNull(user, "El usuario es obligatorio");

        return new UserJpaEntity(
                user.id().value(),
                user.phoneNumber().value(),
                user.displayName().value(),
                user.profileImageUrl(),
                user.phoneVerificationStatus(),
                user.role(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}