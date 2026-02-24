CREATE TYPE theme_enum AS ENUM ('LIGHT', 'DARK', 'SYSTEM');

CREATE TYPE visibility_enum AS ENUM ('PUBLIC', 'PRIVATE');

CREATE TYPE notification_channel_enum AS ENUM ('EMAIL', 'PUSH', 'BOTH', 'NONE');

CREATE TYPE font_size_enum AS ENUM ('NORMAL', 'LARGE', 'EXTRA_LARGE');

CREATE TYPE password_rotation_enum AS ENUM (
    'DISABLED',
    'EVERY_3_MONTHS',
    'EVERY_6_MONTHS',
    'YEARLY',
    'EVERY_2_YEARS'
);

CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    default_theme theme_enum DEFAULT 'SYSTEM',
    language_preference VARCHAR(5) DEFAULT 'en-US',
    font_size font_size_enum DEFAULT 'NORMAL',
    high_contrast_mode BOOLEAN DEFAULT FALSE,

    profile_visibility visibility_enum DEFAULT 'PUBLIC',
    recipe_visibility_default visibility_enum DEFAULT 'PUBLIC',
    show_birthday BOOLEAN DEFAULT FALSE,
    share_activity_feed BOOLEAN DEFAULT TRUE,
    allow_recipe_sharing BOOLEAN DEFAULT TRUE,
    follow_requests_manual BOOLEAN DEFAULT FALSE,

    accept_direct_messages BOOLEAN DEFAULT TRUE,
    allow_recipe_messages BOOLEAN DEFAULT TRUE,

    notification_channel notification_channel_enum DEFAULT 'BOTH',
    notify_on_new_comment BOOLEAN DEFAULT TRUE,
    notify_on_new_follower BOOLEAN DEFAULT TRUE,
    notify_on_recipe_like BOOLEAN DEFAULT TRUE,
    notify_on_direct_message BOOLEAN DEFAULT TRUE,

    two_factor_auth BOOLEAN DEFAULT FALSE,
    login_alerts BOOLEAN DEFAULT TRUE,
    session_timeout_milliseconds INTEGER DEFAULT 600000,
    password_change_reminder_interval password_rotation_enum DEFAULT 'DISABLED',
    password_change_reminder_last_update TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE user_settings
    ADD CONSTRAINT fk_user_settings_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE;