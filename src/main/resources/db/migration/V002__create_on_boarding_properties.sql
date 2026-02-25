CREATE TYPE onboarding_status_enum AS ENUM (
    'PENDING_VERIFICATION',
    'STEP_1',
    'STEP_2',
    'STEP_3',
    'COMPLETED'
);

CREATE TYPE user_status_enum AS ENUM (
    'PENDING',
    'ACTIVE',
    'DISABLED'
);

ALTER TABLE users
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN profile_picture_url TEXT,
    ADD COLUMN profile_picture_alt VARCHAR(500),
    ADD COLUMN cover_photo_url TEXT,
    ADD COLUMN cover_photo_alt VARCHAR(500),
    ADD COLUMN onboarding_started_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN onboarding_completed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN onboarding_status onboarding_status_enum DEFAULT 'PENDING_VERIFICATION',
    ADD COLUMN status user_status_enum DEFAULT 'PENDING';
