CREATE TYPE difficulty_level_enum AS ENUM (
    'BEGINNER',
    'INTERMEDIATE',
    'ADVANCED',
    'EXPERT'
);

ALTER TABLE recipes
ADD COLUMN difficulty_level difficulty_level_enum NOT NULL DEFAULT 'BEGINNER';

ALTER TABLE recipes
ADD COLUMN language VARCHAR(5) NOT NULL DEFAULT 'en-US';

ALTER TABLE articles
ADD COLUMN language VARCHAR(5) NOT NULL DEFAULT 'en-US';
