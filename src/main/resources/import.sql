INSERT INTO products (name, price, image_url) VALUES ('초콜릿', 1000, 'https://i.imgur.com/ZU9CL58.png');
INSERT INTO products (name, price, image_url) VALUES ('새우깡', 1500, 'https://i.imgur.com/YxTseJH.png');
INSERT INTO products (name, price, image_url) VALUES ('커피', 1500, 'https://i.imgur.com/J55LFRc.png');
INSERT INTO products (name, price, image_url) VALUES ('김밥', 2000, 'https://i.imgur.com/EA8US8c.png');
INSERT INTO products (name, price, image_url) VALUES ('라면', 1000, 'https://i.imgur.com/NMp72cw.png');
INSERT INTO products (name, price, image_url) VALUES ('젤리', 1000, 'https://i.imgur.com/sFFjwua.png');

INSERT INTO approved_products (name) VALUES ('카카오 프렌즈 필통');
INSERT INTO approved_products (name) VALUES ('카카오 프렌즈 인형');

INSERT INTO members (email, password, is_admin) VALUES ('5seonjae@gmail.com', '5seonjae', true);
INSERT INTO members (email, password, is_admin) VALUES ('6seonjae@gmail.com', '6seonjae', false);

INSERT INTO options (product_id, name, quantity) VALUES (1, '다크 초콜릿', 5);
INSERT INTO options (product_id, name, quantity) VALUES (1, '화이트 초콜릿', 4);
INSERT INTO options (product_id, name, quantity) VALUES (1, '아몬드 초콜릿', 3);
INSERT INTO options (product_id, name, quantity) VALUES (1, '두바이 초콜릿', 5);
INSERT INTO options (product_id, name, quantity) VALUES (1, '스위스 초콜릿', 4);
INSERT INTO options (product_id, name, quantity) VALUES (1, '누텔라 초콜릿', 3);