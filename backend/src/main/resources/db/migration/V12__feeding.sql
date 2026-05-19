CREATE TABLE feed_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  code VARCHAR(60) NOT NULL,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  category ENUM('FORAGE','SILAGE','CONCENTRATE','MINERAL','BYPRODUCT','OTHER') NOT NULL,
  dry_matter_pct DECIMAL(5,2) NULL,
  protein_pct DECIMAL(5,2) NULL,
  energy_mcal_kg DECIMAL(5,2) NULL,
  unit_cost DECIMAL(10,4) NULL,
  currency VARCHAR(3) NULL DEFAULT 'MXN',
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_feed_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT uq_feed_account_code UNIQUE (account_id, code),
  INDEX ix_feed_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  category ENUM('DAIRY_LACTATION','DAIRY_DRY','BEEF_GROWING','BEEF_FINISHING','CALF','OTHER') NOT NULL,
  description VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fp_account FOREIGN KEY (account_id) REFERENCES account(id),
  INDEX ix_fp_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_plan_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  feeding_plan_id BIGINT NOT NULL,
  feed_item_id BIGINT NOT NULL,
  kg_per_head_day DECIMAL(6,2) NOT NULL,
  notes VARCHAR(200) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fpi_plan FOREIGN KEY (feeding_plan_id) REFERENCES feeding_plan(id) ON DELETE CASCADE,
  CONSTRAINT fk_fpi_feed FOREIGN KEY (feed_item_id) REFERENCES feed_item(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lot_feeding_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  lot_id BIGINT NOT NULL,
  feeding_plan_id BIGINT NOT NULL,
  assigned_at DATE NOT NULL,
  unassigned_at DATE NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_lfp_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_lfp_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_lfp_plan FOREIGN KEY (feeding_plan_id) REFERENCES feeding_plan(id),
  INDEX ix_lfp_acct_lot_date (account_id, lot_id, assigned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE feeding_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  lot_id BIGINT NOT NULL,
  feed_item_id BIGINT NOT NULL,
  consumed_at DATE NOT NULL,
  total_kg DECIMAL(10,2) NOT NULL,
  cost DECIMAL(10,2) NULL,
  recorded_by_user_id BIGINT NULL,
  notes VARCHAR(300) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_fr_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_fr_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  CONSTRAINT fk_fr_feed FOREIGN KEY (feed_item_id) REFERENCES feed_item(id),
  CONSTRAINT fk_fr_user FOREIGN KEY (recorded_by_user_id) REFERENCES app_user(id),
  INDEX ix_fr_acct_lot_date (account_id, lot_id, consumed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO feed_item (account_id, code, name_es, name_en, category, dry_matter_pct, protein_pct, energy_mcal_kg, unit_cost, currency) VALUES
  (NULL, 'ALFALFA_HENO', 'Heno de alfalfa', 'Alfalfa hay', 'FORAGE', 90.0, 18.0, 2.2, 5.00, 'MXN'),
  (NULL, 'SORGO_GRANO', 'Grano de sorgo', 'Sorghum grain', 'CONCENTRATE', 88.0, 9.5, 3.2, 6.50, 'MXN'),
  (NULL, 'MAIZ_MOLIDO', 'Maiz molido', 'Ground corn', 'CONCENTRATE', 88.0, 9.0, 3.3, 7.20, 'MXN'),
  (NULL, 'SOYA_PASTA', 'Pasta de soya', 'Soybean meal', 'CONCENTRATE', 90.0, 47.0, 3.4, 14.50, 'MXN'),
  (NULL, 'SAL_MINERAL', 'Sal mineral', 'Mineral salt', 'MINERAL', 99.0, 0, 0, 12.00, 'MXN'),
  (NULL, 'ENSILAJE_MAIZ', 'Ensilaje de maiz', 'Corn silage', 'SILAGE', 32.0, 8.0, 2.6, 2.20, 'MXN')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
