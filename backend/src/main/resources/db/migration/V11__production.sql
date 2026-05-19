CREATE TABLE weighing (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  weighed_at DATE NOT NULL,
  weight_kg DECIMAL(7,2) NOT NULL,
  method ENUM('SCALE','TAPE','VISUAL_ESTIMATE') NULL,
  body_condition_score DECIMAL(3,1) NULL,
  weighed_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_weigh_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_weigh_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_weigh_user FOREIGN KEY (weighed_by_user_id) REFERENCES app_user(id),
  INDEX ix_weigh_acct_animal_date (account_id, animal_id, weighed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE milking (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  milking_date DATE NOT NULL,
  session ENUM('TOTAL','AM','PM') NOT NULL DEFAULT 'TOTAL',
  liters DECIMAL(6,2) NOT NULL,
  recorded_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_milk_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_milk_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_milk_user FOREIGN KEY (recorded_by_user_id) REFERENCES app_user(id),
  CONSTRAINT uq_milk_acct_animal_date_session UNIQUE (account_id, animal_id, milking_date, session),
  INDEX ix_milk_acct_date (account_id, milking_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE milk_sample (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  sampled_at DATE NOT NULL,
  scc_cells_per_ml INT NULL,
  fat_pct DECIMAL(4,2) NULL,
  protein_pct DECIMAL(4,2) NULL,
  lactose_pct DECIMAL(4,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ms_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ms_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  INDEX ix_ms_acct_animal_date (account_id, animal_id, sampled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bulk_tank_delivery (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NOT NULL,
  delivery_date DATE NOT NULL,
  total_liters DECIMAL(10,2) NOT NULL,
  buyer VARCHAR(160) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_btd_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_btd_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id),
  CONSTRAINT fk_btd_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_btd_acct_ranch_date (account_id, ranch_id, delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE slaughter_result (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  slaughtered_at DATE NOT NULL,
  live_weight_kg DECIMAL(7,2) NULL,
  carcass_weight_kg DECIMAL(7,2) NULL,
  yield_pct DECIMAL(5,2) NULL,
  grade VARCHAR(40) NULL,
  buyer VARCHAR(160) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_slaughter_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_slaughter_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_slaughter_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_slaughter_acct_animal_date (account_id, animal_id, slaughtered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
