CREATE TABLE products
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(15) NOT NULL,
    price INT NOT NULL,
    image_url VARCHAR(255) NOT NULL
);

-- "카카오" 문구가 포함된 상품명을 등록할 수 있는 승인 리스트 테이블
CREATE TABLE approved_products
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE members
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE wish_items
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT uk_member_product UNIQUE (member_id, product_id),
    CONSTRAINT fk_wish_member FOREIGN KEY (member_id) REFERENCES members(id),
    CONSTRAINT fk_wish_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE options
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    quantity INT NOT NULL,
    CONSTRAINT uk_product UNIQUE (product_id),
    CONSTRAINT fk_option_product FOREIGN KEY (product_id) REFERENCES products(id)
);