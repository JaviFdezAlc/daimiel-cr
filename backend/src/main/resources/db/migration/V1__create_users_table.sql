CREATE TABLE users (
    id UUID PRIMARY KEY,
    phone_number VARCHAR(16) NOT NULL,
    display_name VARCHAR(60) NOT NULL,
    profile_image_url VARCHAR(2048),
    phone_verification_status VARCHAR(16) NOT NULL,
    role VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_phone_number
        UNIQUE (phone_number),

    CONSTRAINT chk_users_phone_number_e164
        CHECK (phone_number ~ '^\+[1-9][0-9]{7,14}$'),

    CONSTRAINT chk_users_display_name_not_blank
        CHECK (length(btrim(display_name)) >= 2),

    CONSTRAINT chk_users_phone_verification_status
        CHECK (phone_verification_status IN ('PENDING', 'VERIFIED')),

    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN'))
);