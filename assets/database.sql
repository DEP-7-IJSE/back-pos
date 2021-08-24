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

SELECT o.*, c.name, SUM(od.unit_price * od.qty) AS total
FROM `order` o
         INNER JOIN customer c ON o.customer_id = c.id
         INNER JOIN order_detail od ON o.id = od.order_id
GROUP BY o.id;

SELECT o.*, c.name, order_total.total
FROM `order` o
         INNER JOIN customer c on o.customer_id = c.id
         INNER JOIN
     (SELECT order_id, SUM(qty * unit_price) AS total FROM order_detail od GROUP BY order_id) AS order_total
     ON o.id = order_total.order_id
WHERE (order_id LIKE '%OD001%'
    OR date LIKE '%2021-08-%'
    OR customer_id LIKE '%C001%'
    OR name LIKE '%Pethum%')
  AND (order_id LIKE '%OD001%'
    OR date LIKE '%2021-08-%'
    OR customer_id LIKE '%C001%'
    OR name LIKE '%Pethum%')
ORDER BY id;

