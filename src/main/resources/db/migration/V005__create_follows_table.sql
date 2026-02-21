CREATE TABLE follows (
    follower_user_id INTEGER NOT NULL,
    following_user_id INTEGER NOT NULL,
    followed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (follower_user_id, following_user_id),
    CONSTRAINT fk_follower FOREIGN KEY (follower_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_following FOREIGN KEY (following_user_id) REFERENCES users (id) ON DELETE CASCADE
);