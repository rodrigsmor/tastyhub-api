CREATE TYPE ingredient_unit_enum AS ENUM (
    'GRAM',
    'KILOGRAM',
    'MILLILITER',
    'LITER',
    'CUP',
    'TABLESPOON',
    'TEASPOON',
    'DESSERTSPOON',
    'UNIT',
    'HALF',
    'SLICE',
    'PINCH',
    'DASH',
    'TO_TASTE',
    'CLOVE',
    'BUNCH'
);

CREATE TABLE ingredients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE currencies (
    id SMALLSERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10)
);

CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    cook_time_min INTEGER NOT NULL,
    cook_time_max INTEGER,
    estimated_cost NUMERIC(10,2),
    currency_id SMALLINT REFERENCES currencies(id),
    user_id BIGINT NOT NULL,
    cover_url TEXT,
    cover_alt VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE preparation_steps (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    step_number INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    CONSTRAINT fk_recipe_steps FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
    CONSTRAINT uk_recipe_step_number UNIQUE (recipe_id, step_number)
);

CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    cover_url TEXT,
    cover_alt VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity DECIMAL(10,2),
    unit ingredient_unit_enum NOT NULL,

    CONSTRAINT fk_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
    CONSTRAINT fk_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE CASCADE
);

CREATE TABLE recipe_tags (
    recipe_id BIGINT NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (recipe_id, tag_id),
    CONSTRAINT fk_recipe_tags_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

CREATE TABLE recipe_media (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    media_url TEXT NOT NULL,
    media_type VARCHAR(20),
    alt_text VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipe_media FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipe_id BIGINT,
    article_id BIGINT,
    rating NUMERIC(2,1) NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_only_one_target CHECK (
        (recipe_id IS NOT NULL AND article_id IS NULL) OR
        (recipe_id IS NULL AND article_id IS NOT NULL)
    )
);