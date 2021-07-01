PRAGMA foreign_keys= OFF;

DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS rawMaterials;
DROP TABLE IF EXISTS cookiesToBake;
DROP TABLE IF EXISTS pallets;
DROP TABLE IF EXISTS recipeRows;
DROP TABLE IF EXISTS cookies;

PRAGMA foreign_keys= ON;

CREATE TABLE customers
(
    company_id TEXT,
    address    TEXT,
    PRIMARY KEY (company_id)
);

CREATE TABLE cookies(
    recipe_id TEXT ,
    PRIMARY KEY (recipe_id)
);


CREATE TABLE orders
(
    order_id        TEXT NOT NULL,
    delivery_reg    INT,
    company_id TEXT,
    PRIMARY KEY (order_id),
    FOREIGN KEY (company_id) REFERENCES customers(company_id)
);

CREATE TABLE rawMaterials(
    material_id TEXT NOT NULL,
    amountStorage INT CHECK (amountStorage > 0),
    amountDelivery INT,
    lastDelivery date,
    unit TEXT,
    PRIMARY KEY (material_id)
);

CREATE TABLE pallets
(
    pallet_id DEFAULT (lower(hex(randomblob(16)))),
    pallet_state TEXT,
    isBlocked BOOLEAN DEFAULT FALSE ,
    productionDate DATE,
    order_id TEXT ,
    recipe_id TEXT NOT NULL,
    PRIMARY KEY (pallet_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (recipe_id) REFERENCES cookies(recipe_id)
);

CREATE TABLE cookiesToBake(
    amountCookies INT CHECK (amountCookies > 0),
    order_id TEXT NOT NULL,
    recipe_id TEXT NOT NULL,
    PRIMARY KEY (amountCookies),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (recipe_id) REFERENCES cookies(recipe_id)
);

CREATE TABLE recipeRows
(
    amount DOUBLE check (amount > 0),
    recipe_id TEXT NOT NULL,
    material_id TEXT NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES cookies(recipe_id),
    FOREIGN KEY (material_id) REFERENCES rawMaterials(material_id),
    PRIMARY KEY (material_id, recipe_id)
);

CREATE TRIGGER validate_cookies
   BEFORE INSERT ON pallets
BEGIN
   SELECT
      CASE
      WHEN NEW.recipe_id
      NOT IN (
         SELECT recipe_id
         FROM   cookies
      ) THEN
         RAISE (ABORT,'Invalid cookie name')
       END;
END;


CREATE TRIGGER validate_ingredients
   BEFORE UPDATE ON rawMaterials
BEGIN
   SELECT
      CASE
      WHEN (OLD.amountStorage - NEW.amountStorage < 0)
      THEN
         RAISE (ABORT,'Invalid cookie name')
       END;
END;

INSERT INTO customers(company_id, address)
VALUES ('Finkakor AB', 'Helsingborg'),
       ('Småbröd AB', 'Malmö'),
       ('Kaffebröd AB', 'Landskrona'),
       ('Bjudkakor AB', 'Ystad'),
       ('Kalaskakor AB', 'Trelleborg'),
       ('Partykakor AB', 'Kristianstad'),
       ('Gästkakor AB', 'Hässleholm'),
       ('Skånekakor AB', 'Perstorp');

INSERT INTO cookies(recipe_id)
VALUES ('Nut ring'),
       ('Nut cookie'),
       ('Amneris'),
       ('Tango'),
       ('Almond delight'),
       ('Berliner');


INSERT INTO rawMaterials(material_id, amountStorage, unit)
VALUES ('Flour', 100000, 'g'),
       ('Butter', 100000, 'g'),
       ('Icing sugar', 100000, 'g'),
       ('Roasted, chopped nuts', 100000, 'g'),
       ('Fine-ground nuts', 100000, 'g'),
       ('Ground, roasted nuts', 100000, 'g'),
       ('Bread crumbs', 100000, 'g'),
       ('Sugar', 100000, 'g'),
       ('Egg whites', 100000, 'g'),
       ('Chocolate', 100000, 'g'),
       ('Marzipan', 100000, 'g'),
       ('Egg', 100000, 'g'),
       ('Potato starch', 100000, 'g'),
       ('Wheat flour', 100000, 'g'),
       ('Sodium bicarbonate', 100000, 'g'),
       ('Vanilla', 100000, 'g'),
       ('Cinnamon', 100000, 'g'),
       ('Chopped almonds', 100000, 'g'),
       ('Vanilla sugar', 100000, 'g');

INSERT INTO recipeRows(recipe_id, material_id, amount)
VALUES ('Nut ring', 'Flour', 450.0),
       ('Nut ring', 'Butter', 450.0),
       ('Nut ring', 'Icing sugar', 190.0),
       ('Nut ring', 'Roasted, chopped nuts', 225.0),

       ('Nut cookie', 'Fine-ground nuts', 750.0),
       ('Nut cookie', 'Ground, roasted nuts', 625.0),
       ('Nut cookie', 'Bread crumbs', 125.0),
       ('Nut cookie', 'Sugar', 375.0),
       ('Nut cookie', 'Egg whites', 350.0),
       ('Nut cookie', 'Chocolate', 50.0),

       ('Amneris', 'Marzipan', 750.0),
       ('Amneris', 'Butter', 250.0),
       ('Amneris', 'Egg', 250.0),
       ('Amneris', 'Potato starch', 25.0),
       ('Amneris', 'Wheat flour', 25.0),

       ('Tango', 'Butter', 200.0),
       ('Tango', 'Sugar', 250.0),
       ('Tango', 'Flour', 300.0),
       ('Tango', 'Sodium bicarbonate', 4.0),
       ('Tango', 'Vanilla', 2.0),

       ('Almond delight', 'Butter', 400.0),
       ('Almond delight', 'Sugar', 270.0),
       ('Almond delight', 'Chopped almonds', 279.0),
       ('Almond delight', 'Flour', 400.0),
       ('Almond delight', 'Cinnamon', 10.0),

       ('Berliner', 'Flour', 350.0),
       ('Berliner', 'Butter', 250.0),
       ('Berliner', 'Icing sugar', 100.0),
       ('Berliner', 'Egg', 50.0),
       ('Berliner', 'Vanilla sugar', 5.0),
       ('Berliner', 'Chocolate', 50.0);


