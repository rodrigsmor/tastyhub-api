CREATE TABLE user_collections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    description TEXT,
    cover_url VARCHAR(255),
    cover_alt VARCHAR(100),
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_fixed BOOLEAN NOT NULL DEFAULT FALSE,
    is_deletable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_collection_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE recipe_collections (
    collection_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, recipe_id),
    CONSTRAINT fk_recipe_coll_collection FOREIGN KEY (collection_id) REFERENCES user_collections (id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_coll_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
);

CREATE TABLE article_collections (
    collection_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, article_id),
    CONSTRAINT fk_article_coll_collection FOREIGN KEY (collection_id) REFERENCES user_collections (id) ON DELETE CASCADE,
    CONSTRAINT fk_article_coll_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);