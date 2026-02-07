ALTER TABLE users
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN profile_picture_url TEXT,
    ADD COLUMN profile_picture_alt VARCHAR(255),
    ADD COLUMN cover_photo_url TEXT,
    ADD COLUMN cover_photo_alt VARCHAR(255),
    ADD COLUMN on_boarding_started_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN on_boarding_completed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN on_boarding_status VARCHAR(50) DEFAULT 'PENDING_VERIFICATION',
    ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING';