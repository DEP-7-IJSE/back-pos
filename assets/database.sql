CREATE TABLE customer
(
    id      VARCHAR(8) PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL
);

CREATE TABLE item
(
    code        VARCHAR(10) PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    unit_price  DECIMAL      NOT NULL,
    qty_on_hand INT          NOT NULL
);

CREATE TABLE `order`
(
    id          VARCHAR(10) PRIMARY KEY,
    date        DATE       NOT NULL,
    customer_id VARCHAR(8) NOT NULL,
    CONSTRAINT FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE order_detail
(
    order_id   VARCHAR(10) NOT NULL,
    item_code  VARCHAR(10) NOT NULL,
    unit_price DECIMAL     NOT NULL,
    qty        INT         NOT NULL,
    CONSTRAINT PRIMARY KEY (order_id, item_code),
    CONSTRAINT FOREIGN KEY (order_id) REFERENCES `order` (id),
    CONSTRAINT FOREIGN KEY (item_code) REFERENCES item (code)
);

SELECT o.id, o.date, o.customer_id, c.name, SUM(od.unit_price * od.qty) AS total
FROM `order` o
         LEFT OUTER JOIN customer c ON o.customer_id = c.id
         LEFT OUTER JOIN order_detail od ON o.id = od.order_id
GROUP BY o.id;
