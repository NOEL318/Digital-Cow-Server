CREATE TABLE expense_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  code VARCHAR(60) NOT NULL,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  kind ENUM('FEED','HEALTH','LABOR','INFRASTRUCTURE','TRANSPORT','REPRODUCTION','OTHER') NOT NULL,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_exc_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT uq_exc_account_code UNIQUE (account_id, code),
  INDEX ix_exc_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE income_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  code VARCHAR(60) NOT NULL,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  kind ENUM('ANIMAL_SALE','MILK_SALE','BYPRODUCT','SERVICE','OTHER') NOT NULL,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inc_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT uq_inc_account_code UNIQUE (account_id, code),
  INDEX ix_inc_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expense (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  expense_category_id BIGINT NOT NULL,
  incurred_at DATE NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
  ranch_id BIGINT NULL,
  lot_id BIGINT NULL,
  animal_id BIGINT NULL,
  description VARCHAR(400) NULL,
  vendor VARCHAR(160) NULL,
  invoice_number VARCHAR(80) NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_exp_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_exp_cat FOREIGN KEY (expense_category_id) REFERENCES expense_category(id),
  CONSTRAINT fk_exp_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_exp_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_exp_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_exp_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_exp_acct_date (account_id, incurred_at),
  INDEX ix_exp_acct_ranch (account_id, ranch_id),
  INDEX ix_exp_acct_animal (account_id, animal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE income (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  income_category_id BIGINT NOT NULL,
  received_at DATE NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
  ranch_id BIGINT NULL,
  lot_id BIGINT NULL,
  animal_id BIGINT NULL,
  description VARCHAR(400) NULL,
  payer VARCHAR(160) NULL,
  invoice_number VARCHAR(80) NULL,
  source_type ENUM('MANUAL','ANIMAL_SALE','MILK_SALE','BULK_TANK','OTHER') NOT NULL DEFAULT 'MANUAL',
  source_id BIGINT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inc_acct FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_inc_cat FOREIGN KEY (income_category_id) REFERENCES income_category(id),
  CONSTRAINT fk_inc_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_inc_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_inc_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_inc_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_inc_acct_date (account_id, received_at),
  INDEX ix_inc_acct_source (account_id, source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE animal_sale (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  sold_at DATE NOT NULL,
  live_weight_kg DECIMAL(7,2) NULL,
  price_per_kg DECIMAL(10,4) NULL,
  total_price DECIMAL(12,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
  buyer VARCHAR(160) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_as_acct FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_as_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_as_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  CONSTRAINT uq_as_account_animal UNIQUE (account_id, animal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE milk_sale (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  sale_date DATE NOT NULL,
  total_liters DECIMAL(10,2) NOT NULL,
  price_per_liter DECIMAL(10,4) NOT NULL,
  total_price DECIMAL(12,2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
  buyer VARCHAR(160) NULL,
  bulk_tank_delivery_id BIGINT NULL,
  ranch_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ms_acct FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ms_btd FOREIGN KEY (bulk_tank_delivery_id) REFERENCES bulk_tank_delivery(id),
  CONSTRAINT fk_ms_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_ms_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_ms_acct_date (account_id, sale_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO expense_category (account_id, code, name_es, name_en, kind) VALUES
  (NULL, 'FEED_GENERAL', 'Alimentacion general', 'General feed', 'FEED'),
  (NULL, 'FEED_CONCENTRATE', 'Concentrados', 'Concentrates', 'FEED'),
  (NULL, 'FEED_FORAGE', 'Forrajes', 'Forages', 'FEED'),
  (NULL, 'HEALTH_VET', 'Veterinaria', 'Veterinary', 'HEALTH'),
  (NULL, 'HEALTH_VACCINES', 'Vacunas', 'Vaccines', 'HEALTH'),
  (NULL, 'HEALTH_MEDICATIONS', 'Medicamentos', 'Medications', 'HEALTH'),
  (NULL, 'LABOR_SALARIES', 'Salarios', 'Salaries', 'LABOR'),
  (NULL, 'LABOR_CONTRACTORS', 'Personal eventual', 'Contractors', 'LABOR'),
  (NULL, 'INFRASTRUCTURE_REPAIR', 'Infraestructura - reparacion', 'Infrastructure repair', 'INFRASTRUCTURE'),
  (NULL, 'INFRASTRUCTURE_NEW', 'Infraestructura - nueva', 'Infrastructure new', 'INFRASTRUCTURE'),
  (NULL, 'TRANSPORT_FUEL', 'Combustible', 'Fuel', 'TRANSPORT'),
  (NULL, 'TRANSPORT_FREIGHT', 'Fletes', 'Freight', 'TRANSPORT'),
  (NULL, 'REPRODUCTION_SEMEN', 'Semen', 'Semen', 'REPRODUCTION'),
  (NULL, 'REPRODUCTION_VET', 'Veterinaria reproductiva', 'Reproductive vet', 'REPRODUCTION'),
  (NULL, 'ENERGY', 'Energia', 'Energy', 'OTHER'),
  (NULL, 'WATER', 'Agua', 'Water', 'OTHER'),
  (NULL, 'OTHER', 'Otros', 'Other', 'OTHER')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);

INSERT INTO income_category (account_id, code, name_es, name_en, kind) VALUES
  (NULL, 'ANIMAL_SALE_BEEF', 'Venta animal carne', 'Beef animal sale', 'ANIMAL_SALE'),
  (NULL, 'ANIMAL_SALE_DAIRY', 'Venta animal lechero', 'Dairy animal sale', 'ANIMAL_SALE'),
  (NULL, 'ANIMAL_SALE_CULL', 'Venta de descarte', 'Cull sale', 'ANIMAL_SALE'),
  (NULL, 'MILK_SALE_BULK', 'Venta leche a granel', 'Bulk milk sale', 'MILK_SALE'),
  (NULL, 'MILK_SALE_DIRECT', 'Venta leche directa', 'Direct milk sale', 'MILK_SALE'),
  (NULL, 'BYPRODUCT_MANURE', 'Estiercol', 'Manure', 'BYPRODUCT'),
  (NULL, 'BYPRODUCT_OTHER', 'Otros subproductos', 'Other byproducts', 'BYPRODUCT'),
  (NULL, 'SERVICE_BULL_RENTAL', 'Renta de toro', 'Bull rental', 'SERVICE'),
  (NULL, 'OTHER', 'Otros', 'Other', 'OTHER')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
