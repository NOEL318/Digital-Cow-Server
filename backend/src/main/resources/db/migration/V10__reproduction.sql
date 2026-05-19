CREATE TABLE bull (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  internal_code VARCHAR(60) NOT NULL,
  name VARCHAR(160) NOT NULL,
  breed_id BIGINT NULL,
  source ENUM('OWN','EXTERNAL') NOT NULL,
  animal_id BIGINT NULL,
  registry_number VARCHAR(80) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_bull_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_bull_breed FOREIGN KEY (breed_id) REFERENCES breed(id),
  CONSTRAINT fk_bull_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT uq_bull_account_code UNIQUE (account_id, internal_code),
  INDEX ix_bull_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE semen_straw (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  bull_id BIGINT NOT NULL,
  provider VARCHAR(160) NULL,
  batch_number VARCHAR(80) NULL,
  total_quantity INT NOT NULL DEFAULT 0,
  available_quantity INT NOT NULL DEFAULT 0,
  received_at DATE NULL,
  expires_at DATE NULL,
  cost_per_straw DECIMAL(10,2) NULL,
  storage_location VARCHAR(120) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_straw_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_straw_bull FOREIGN KEY (bull_id) REFERENCES bull(id),
  INDEX ix_straw_account_bull_exp (account_id, bull_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE heat (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  detected_at TIMESTAMP NOT NULL,
  detection_method ENUM('VISUAL','PEDOMETER','HEAT_PATCH','CAMERA','OTHER') NULL,
  intensity ENUM('WEAK','MODERATE','STRONG') NULL,
  notes TEXT NULL,
  detected_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_heat_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_heat_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_heat_user FOREIGN KEY (detected_by_user_id) REFERENCES app_user(id),
  INDEX ix_heat_acct_animal_date (account_id, animal_id, detected_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE service_event (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  service_type ENUM('AI','NATURAL','EMBRYO_TRANSFER') NOT NULL,
  service_date DATE NOT NULL,
  bull_id BIGINT NULL,
  semen_straw_id BIGINT NULL,
  technician_name VARCHAR(160) NULL,
  heat_id BIGINT NULL,
  cost DECIMAL(10,2) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_serv_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_serv_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_serv_bull FOREIGN KEY (bull_id) REFERENCES bull(id),
  CONSTRAINT fk_serv_straw FOREIGN KEY (semen_straw_id) REFERENCES semen_straw(id),
  CONSTRAINT fk_serv_heat FOREIGN KEY (heat_id) REFERENCES heat(id),
  CONSTRAINT fk_serv_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_serv_acct_animal_date (account_id, animal_id, service_date),
  INDEX ix_serv_acct_date (account_id, service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pregnancy_check (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  service_id BIGINT NULL,
  checked_at DATE NOT NULL,
  method ENUM('PALPATION','ULTRASOUND','BLOOD_TEST','MILK_TEST') NULL,
  result ENUM('POSITIVE','NEGATIVE','DOUBTFUL') NOT NULL,
  estimated_gestation_days SMALLINT NULL,
  estimated_calving_date DATE NULL,
  vet_visit_id BIGINT NULL,
  checked_by_user_id BIGINT NULL,
  notes TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_preg_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_preg_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_preg_service FOREIGN KEY (service_id) REFERENCES service_event(id),
  CONSTRAINT fk_preg_visit FOREIGN KEY (vet_visit_id) REFERENCES vet_visit(id),
  CONSTRAINT fk_preg_user FOREIGN KEY (checked_by_user_id) REFERENCES app_user(id),
  INDEX ix_preg_acct_animal_date (account_id, animal_id, checked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE calving (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  calved_at DATE NOT NULL,
  ease ENUM('FREE','EASY','ASSISTED','DIFFICULT','SURGERY') NOT NULL DEFAULT 'FREE',
  outcome ENUM('LIVE','STILLBORN','TWIN_LIVE','TWIN_MIXED','TWIN_STILLBORN') NOT NULL DEFAULT 'LIVE',
  calf_animal_id BIGINT NULL,
  calf_sex ENUM('FEMALE','MALE') NULL,
  calf_birth_weight_kg DECIMAL(5,2) NULL,
  pregnancy_check_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_calv_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_calv_dam FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_calv_calf FOREIGN KEY (calf_animal_id) REFERENCES animal(id),
  CONSTRAINT fk_calv_pc FOREIGN KEY (pregnancy_check_id) REFERENCES pregnancy_check(id),
  CONSTRAINT fk_calv_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_calv_acct_animal_date (account_id, animal_id, calved_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE abortion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  aborted_at DATE NOT NULL,
  estimated_gestation_days SMALLINT NULL,
  cause VARCHAR(300) NULL,
  pregnancy_check_id BIGINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ab_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ab_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_ab_pc FOREIGN KEY (pregnancy_check_id) REFERENCES pregnancy_check(id),
  CONSTRAINT fk_ab_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_ab_acct_animal_date (account_id, animal_id, aborted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE weaning (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  weaned_at DATE NOT NULL,
  weight_kg DECIMAL(6,2) NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_wean_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_wean_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_wean_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_wean_acct_animal_date (account_id, animal_id, weaned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dry_off (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  animal_id BIGINT NOT NULL,
  dried_off_at DATE NOT NULL,
  lactation_days SMALLINT NULL,
  notes TEXT NULL,
  created_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_do_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_do_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_do_user FOREIGN KEY (created_by_user_id) REFERENCES app_user(id),
  INDEX ix_do_acct_animal_date (account_id, animal_id, dried_off_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
