-- Maisto pristatymo ir valdymo sistema
-- DB Schema v1
-- Target: MySQL 8.0.16+

CREATE DATABASE IF NOT EXISTS food_delivery
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE food_delivery;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_type VARCHAR(31) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone_number VARCHAR(30) NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    delivery_address VARCHAR(500) NULL,
    bonus_points INT NULL,
    vehicle_type VARCHAR(30) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_type CHECK (
        user_type IN ('ADMIN', 'CLIENT', 'DRIVER', 'RESTAURANT_OWNER')
    ),
    CONSTRAINT chk_users_role CHECK (
        role IN ('ADMIN', 'CLIENT', 'DRIVER', 'RESTAURANT_OWNER')
    ),
    CONSTRAINT chk_users_type_matches_role CHECK (user_type = role),
    CONSTRAINT chk_users_bonus_points CHECK (
        (user_type = 'CLIENT' AND bonus_points IS NOT NULL AND bonus_points >= 0)
        OR (user_type <> 'CLIENT' AND bonus_points IS NULL)
    ),
    CONSTRAINT chk_users_vehicle_type CHECK (
        (user_type = 'DRIVER' AND vehicle_type IN
            ('BICYCLE', 'SCOOTER', 'MOTORCYCLE', 'CAR', 'OTHER'))
        OR (user_type <> 'DRIVER' AND vehicle_type IS NULL)
    )
) ENGINE = InnoDB;

CREATE TABLE restaurants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    address VARCHAR(500) NOT NULL,
    type VARCHAR(30) NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_restaurants_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_restaurants_type CHECK (
        type IN ('ITALIAN', 'ASIAN', 'SUSHI', 'PIZZA', 'BURGERS',
                 'FAST_FOOD', 'VEGETARIAN', 'OTHER')
    ),
    INDEX idx_restaurants_owner (owner_id),
    INDEX idx_restaurants_type (type),
    INDEX idx_restaurants_name (name)
) ENGINE = InnoDB;

CREATE TABLE menu_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_menu_items_price CHECK (price > 0),
    CONSTRAINT uq_menu_items_restaurant_name UNIQUE (restaurant_id, name),
    INDEX idx_menu_items_restaurant_available (restaurant_id, available)
) ENGINE = InnoDB;

CREATE TABLE food_orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    driver_id BIGINT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at DATETIME(6) NULL,
    subtotal DECIMAL(12, 2) NOT NULL,
    price_multiplier DECIMAL(6, 3) NOT NULL DEFAULT 1.000,
    total_price DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_food_orders_client
        FOREIGN KEY (client_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_food_orders_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_food_orders_driver
        FOREIGN KEY (driver_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_food_orders_status CHECK (
        status IN ('CREATED', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP',
                   'PICKED_UP', 'DELIVERING', 'COMPLETED', 'CANCELLED')
    ),
    CONSTRAINT chk_food_orders_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_food_orders_multiplier CHECK (price_multiplier > 0),
    CONSTRAINT chk_food_orders_total CHECK (total_price >= 0),
    CONSTRAINT chk_food_orders_completion CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
        OR (status <> 'COMPLETED' AND completed_at IS NULL)
    ),
    INDEX idx_food_orders_client_created (client_id, created_at),
    INDEX idx_food_orders_restaurant_status (restaurant_id, status),
    INDEX idx_food_orders_driver_status (driver_id, status),
    INDEX idx_food_orders_status_created (status, created_at)
) ENGINE = InnoDB;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES food_orders (id)
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_price CHECK (price_at_purchase > 0),
    CONSTRAINT uq_order_items_order_menu_item UNIQUE (order_id, menu_item_id),
    INDEX idx_order_items_menu_item (menu_item_id)
) ENGINE = InnoDB;

CREATE TABLE order_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    sent_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_messages_order
        FOREIGN KEY (order_id) REFERENCES food_orders (id)
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_order_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    INDEX idx_order_messages_order_sent (order_id, sent_at),
    INDEX idx_order_messages_sender (sender_id)
) ENGINE = InnoDB;

CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    review_type VARCHAR(30) NOT NULL,
    restaurant_id BIGINT NULL,
    reviewed_user_id BIGINT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(2000) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_reviews_order
        FOREIGN KEY (order_id) REFERENCES food_orders (id)
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT fk_reviews_author
        FOREIGN KEY (author_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_reviewed_user
        FOREIGN KEY (reviewed_user_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_reviews_type CHECK (
        review_type IN ('RESTAURANT', 'DRIVER', 'CLIENT')
    ),
    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_reviews_target CHECK (
        (review_type = 'RESTAURANT' AND restaurant_id IS NOT NULL
            AND reviewed_user_id IS NULL)
        OR
        (review_type IN ('DRIVER', 'CLIENT') AND restaurant_id IS NULL
            AND reviewed_user_id IS NOT NULL)
    ),
    CONSTRAINT uq_reviews_order_author_type UNIQUE
        (order_id, author_id, review_type),
    INDEX idx_reviews_restaurant (restaurant_id, review_type),
    INDEX idx_reviews_reviewed_user (reviewed_user_id, review_type),
    INDEX idx_reviews_order (order_id)
) ENGINE = InnoDB;
