CREATE DATABASE IF NOT EXISTS mydatabase;
USE mydatabase;

DROP TABLE IF EXISTS recipe_photos;
DROP TABLE IF EXISTS recipe_ingredients;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE recipes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    cuisine_type VARCHAR(60) NOT NULL,
    country_of_origin VARCHAR(60) NOT NULL,
    difficulty VARCHAR(30) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    instructions VARCHAR(4000) NOT NULL,
    cook_time_minutes INT NOT NULL,
    popularity_score INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE recipe_ingredients (
    recipe_id BIGINT NOT NULL,
    ingredient VARCHAR(120) NOT NULL,
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE recipe_photos (
    recipe_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_recipe_photos_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

INSERT INTO users (username, email, password, role) VALUES
('chefana', 'chefana@duoc.cl', '$2y$10$2Helkx6rWOboHf8euDjVCOdb5Gkew3jBr165uHElOgVk69JCSO5c6', 'ROLE_USER'),
('martinchef', 'martinchef@duoc.cl', '$2y$10$byCQCfKn4Nqa94kJNGoFNOMMBTDaRfauGipbeLBDTVtZm47UHqMoW', 'ROLE_USER'),
('adminrecetas', 'admin@duoc.cl', '$2y$10$BopQ4aGHiEOsufYNM3jc9eH7UbC/4BxhY0A5d0xGGbkZdnUf2QaMe', 'ROLE_ADMIN');

INSERT INTO recipes (name, cuisine_type, country_of_origin, difficulty, summary, instructions, cook_time_minutes, popularity_score, created_at) VALUES
('Pastel de choclo', 'Chilena', 'Chile', 'Media', 'Receta tradicional chilena con pino, pollo y cobertura de choclo.', 'Prepara el pino. Cocina el pollo. Procesa el choclo. Arma la fuente y hornea hasta dorar.', 55, 96, '2026-03-20 12:00:00'),
('Ceviche peruano', 'Peruana', 'Perú', 'Fácil', 'Preparación fresca de pescado marinado con limón y cebolla.', 'Corta el pescado. Mezcla con limón, cebolla morada, cilantro y ají. Sirve de inmediato.', 20, 92, '2026-03-19 12:00:00'),
('Tacos de pollo', 'Mexicana', 'México', 'Fácil', 'Tacos rápidos con verduras frescas y salsa cremosa.', 'Cocina el pollo, calienta tortillas, rellena y termina con salsa de yogur.', 30, 90, '2026-03-18 12:00:00'),
('Paella mediterránea', 'Española', 'España', 'Difícil', 'Arroz especiado con mariscos y verduras.', 'Sofríe verduras, incorpora arroz y azafrán, añade caldo y termina con mariscos.', 70, 88, '2026-03-17 12:00:00');

INSERT INTO recipe_ingredients (recipe_id, ingredient) VALUES
(1, 'choclo'), (1, 'carne molida'), (1, 'pollo'), (1, 'cebolla'), (1, 'aceitunas'), (1, 'huevo'),
(2, 'pescado blanco'), (2, 'limón'), (2, 'cebolla morada'), (2, 'cilantro'), (2, 'ají'),
(3, 'tortillas'), (3, 'pollo'), (3, 'palta'), (3, 'tomate'), (3, 'lechuga'), (3, 'yogur'),
(4, 'arroz'), (4, 'azafrán'), (4, 'caldo'), (4, 'camarones'), (4, 'choritos'), (4, 'pimentón');

INSERT INTO recipe_photos (recipe_id, photo_url) VALUES
(1, 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80'),
(2, 'https://images.unsplash.com/photo-1619894991209-9f9694be045a?auto=format&fit=crop&w=900&q=80'),
(3, 'https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?auto=format&fit=crop&w=900&q=80'),
(4, 'https://images.unsplash.com/photo-1515443961218-a51367888e4b?auto=format&fit=crop&w=900&q=80');
