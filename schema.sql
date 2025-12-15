-- Database Schema for AI Search Service
-- MySQL 8.0+

-- ============================================
-- User Domain
-- ============================================

-- 사용자 기본 정보
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, INACTIVE, SUSPENDED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 기본 정보';

-- 사용자 상세 정보
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100),
    phone_number VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    birth_date DATE,
    gender VARCHAR(10) COMMENT 'MALE, FEMALE, OTHER',
    profile_image_url VARCHAR(500),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_country (country)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 상세 정보';

-- ============================================
-- Product Domain
-- ============================================

-- 상품 기본 정보
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100),
    sku VARCHAR(100) UNIQUE,
    price DECIMAL(15, 2) NOT NULL,
    discount_price DECIMAL(15, 2),
    stock INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, OUT_OF_STOCK, DISCONTINUED',
    rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_brand (brand),
    INDEX idx_sku (sku),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_rating (rating),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품 기본 정보';

-- 상품 상세 정보
CREATE TABLE IF NOT EXISTS product_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    description TEXT,
    long_description LONGTEXT,
    specifications JSON COMMENT '제품 사양 (JSON 형태)',
    features JSON COMMENT '주요 특징 (JSON 배열)',
    dimensions VARCHAR(100) COMMENT '크기: width x height x depth',
    weight DECIMAL(10, 2) COMMENT '무게 (kg)',
    manufacturer VARCHAR(255),
    origin_country VARCHAR(100),
    warranty_period INT COMMENT '보증 기간 (개월)',
    tags JSON COMMENT '태그 (JSON 배열)',
    meta_title VARCHAR(255),
    meta_description TEXT,
    meta_keywords VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_id (product_id),
    FULLTEXT INDEX ft_description (description, long_description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품 상세 정보';

-- ============================================
-- Supporting Tables
-- ============================================

-- 상품 이미지
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_type VARCHAR(20) DEFAULT 'PRODUCT' COMMENT 'THUMBNAIL, PRODUCT, DETAIL',
    display_order INT DEFAULT 0,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상품 이미지';

-- 사용자 검색 로그
CREATE TABLE IF NOT EXISTS search_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    query VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    result_count INT,
    selected_product_id BIGINT,
    clicked BOOLEAN DEFAULT FALSE,
    session_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (selected_product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_query (query(255)),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 검색 로그';

-- ============================================
-- Sample Data (Optional)
-- ============================================

-- 샘플 사용자
INSERT INTO users (email, username, password, status) VALUES
('user1@example.com', 'user1', '$2a$10$dummyHashedPassword1', 'ACTIVE'),
('user2@example.com', 'user2', '$2a$10$dummyHashedPassword2', 'ACTIVE');

-- 샘플 사용자 프로필
INSERT INTO user_profiles (user_id, full_name, phone_number, city, country, gender) VALUES
(1, 'John Doe', '010-1234-5678', 'Seoul', 'South Korea', 'MALE'),
(2, 'Jane Smith', '010-8765-4321', 'Busan', 'South Korea', 'FEMALE');

-- 샘플 상품
INSERT INTO products (name, category, brand, sku, price, discount_price, stock, status, rating, review_count) VALUES
('iPhone 15 Pro', 'Electronics', 'Apple', 'APL-IP15P-128', 1200.00, 1150.00, 50, 'AVAILABLE', 4.5, 120),
('Galaxy S24 Ultra', 'Electronics', 'Samsung', 'SAM-GS24U-256', 1100.00, NULL, 30, 'AVAILABLE', 4.7, 95),
('MacBook Pro 16"', 'Electronics', 'Apple', 'APL-MBP16-512', 2500.00, 2400.00, 20, 'AVAILABLE', 4.8, 150),
('Wireless Mouse', 'Accessories', 'Logitech', 'LOG-MX-MASTER3', 99.99, 89.99, 100, 'AVAILABLE', 4.3, 340);

-- 샘플 상품 상세 정보
INSERT INTO product_details (product_id, description, long_description, specifications, features, manufacturer, origin_country, warranty_period) VALUES
(1, 'Latest Apple flagship smartphone with A17 Pro chip', 'The iPhone 15 Pro features the powerful A17 Pro chip, titanium design, and advanced camera system.',
 '{"display": "6.1 inch OLED", "chip": "A17 Pro", "storage": "128GB", "camera": "48MP Main"}',
 '["Dynamic Island", "Always-On display", "Action button", "USB-C"]', 'Apple Inc.', 'China', 12),
(2, 'Samsung flagship with S Pen and 200MP camera', 'Galaxy S24 Ultra offers the best of Samsung innovation with integrated S Pen and AI features.',
 '{"display": "6.8 inch Dynamic AMOLED", "chip": "Snapdragon 8 Gen 3", "storage": "256GB", "camera": "200MP Main"}',
 '["S Pen integrated", "AI Photo editing", "5000mAh battery", "45W fast charging"]', 'Samsung Electronics', 'South Korea', 24),
(3, 'Professional laptop for creators', 'MacBook Pro 16-inch with M3 Max chip delivers exceptional performance for professionals.',
 '{"display": "16.2 inch Liquid Retina XDR", "chip": "M3 Max", "memory": "36GB", "storage": "512GB"}',
 '["M3 Max chip", "Up to 22 hours battery", "6 Thunderbolt 4 ports", "1080p camera"]', 'Apple Inc.', 'China', 12),
(4, 'Ergonomic wireless mouse for productivity', 'Logitech MX Master 3 offers precision and comfort with customizable buttons.',
 '{"connectivity": "Bluetooth + USB-C", "sensor": "Darkfield 4000 DPI", "battery": "70 days"}',
 '["MagSpeed scrolling", "Ergonomic design", "Multi-device", "Customizable buttons"]', 'Logitech', 'China', 6);

-- 샘플 상품 이미지
INSERT INTO product_images (product_id, image_url, image_type, display_order) VALUES
(1, 'https://example.com/images/iphone15pro-thumb.jpg', 'THUMBNAIL', 1),
(1, 'https://example.com/images/iphone15pro-1.jpg', 'PRODUCT', 2),
(2, 'https://example.com/images/galaxys24ultra-thumb.jpg', 'THUMBNAIL', 1),
(2, 'https://example.com/images/galaxys24ultra-1.jpg', 'PRODUCT', 2);
