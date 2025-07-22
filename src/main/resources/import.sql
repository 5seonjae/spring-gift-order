INSERT INTO products (name, price, image_url) VALUES ('초콜릿', 1000, 'http://localhost:8080/image/chocolate.webp');
INSERT INTO products (name, price, image_url) VALUES ('새우깡', 1500, 'http://localhost:8080/image/shrimpCrackers.webp');
INSERT INTO products (name, price, image_url) VALUES ('커피', 1500, 'http://localhost:8080/image/coffee.png');
INSERT INTO products (name, price, image_url) VALUES ('김밥', 2000, 'http://localhost:8080/image/kimbap.png');
INSERT INTO products (name, price, image_url) VALUES ('라면', 1000, 'http://localhost:8080/image/ramen.png');
INSERT INTO products (name, price, image_url) VALUES ('젤리', 1000, 'http://localhost:8080/image/jelly.png');

INSERT INTO approved_products (name) VALUES ('카카오 프렌즈 필통');
INSERT INTO approved_products (name) VALUES ('카카오 프렌즈 인형');

INSERT INTO members (email, password, is_admin) VALUES ('5seonjae@gmail.com', '5seonjae', true);
INSERT INTO members (email, password, is_admin) VALUES ('6seonjae@gmail.com', '6seonjae', false);

INSERT INTO options (product_id, option_name, option_quantity) VALUES (1, '다크 초콜릿', 5);
INSERT INTO options (product_id, option_name, option_quantity) VALUES (1, '화이트 초콜릿', 4);
INSERT INTO options (product_id, option_name, option_quantity) VALUES (1, '아몬드 초콜릿', 3);
INSERT INTO options (product_id, option_name, option_quantity) VALUES (1, '두바이 초콜릿', 5);
INSERT INTO options (product_id, option_name, option_quantity) VALUES (1, '스위스 초콜릿', 4);
INSERT INTO options (product_id, option_name, option_quantity) VALUES (1, '누텔라 초콜릿', 3);