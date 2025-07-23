INSERT INTO products (name, price, image_url)
VALUES
    ('초콜릿', 1000, 'http://localhost:8080/image/chocolate.webp'),
    ('새우깡', 1500, 'http://localhost:8080/image/shrimpCrackers.webp');

INSERT INTO approved_products (name) VALUES ('카카오 프렌즈 필통');
INSERT INTO approved_products (name) VALUES ('카카오 프렌즈 인형');

INSERT INTO members (email, password, is_admin) VALUES ('5seonjae@gmail.com', '5seonjae', true);
INSERT INTO members (email, password, is_admin) VALUES ('6seonjae@gmail.com', '6seonjae', false);