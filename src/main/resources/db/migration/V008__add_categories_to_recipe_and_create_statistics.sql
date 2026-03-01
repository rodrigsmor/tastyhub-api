CREATE TYPE recipe_category_enum AS ENUM (
    'VEGETARIAN', 'VEGAN', 'SNACK', 'SAUCE', 'PIES',
    'FISH', 'SOUP', 'PASTA', 'FITNESS', 'CANDY',
    'DESSERT', 'MEAL', 'LUNCH', 'DRINK'
);

ALTER TABLE recipes
ADD COLUMN category recipe_category_enum NOT NULL;

CREATE TABLE recipe_statistics (
    recipe_id BIGINT PRIMARY KEY,
    favorites_count INTEGER DEFAULT 0 NOT NULL,
    reviews_count INTEGER DEFAULT 0 NOT NULL,
    total_rating_sum INTEGER DEFAULT 0 NOT NULL,
    average_rating DECIMAL(3, 2) DEFAULT 0.00 NOT NULL,
    CONSTRAINT fk_recipe_stats_recipe FOREIGN KEY (recipe_id) REFERENCES recipes (id) ON DELETE CASCADE
);

CREATE TABLE article_statistics (
    article_id BIGINT PRIMARY KEY,
    comments_count INTEGER DEFAULT 0 NOT NULL,
    favorites_count INTEGER DEFAULT 0 NOT NULL,
    likes_count INTEGER DEFAULT 0 NOT NULL,
    CONSTRAINT fk_article_stats_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);


