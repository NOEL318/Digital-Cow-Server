-- V8: Planes sanitarios. health_plan, health_plan_step, animal_health_plan (asignacion).
-- account_id en health_plan es NULLABLE: NULL = plan global del sistema.

CREATE TABLE health_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NULL,
  name VARCHAR(160) NOT NULL,
  description VARCHAR(500) NULL,
  applies_to_purpose ENUM('BEEF','DAIRY','DUAL','ANY') NOT NULL DEFAULT 'ANY',
  applies_to_sex ENUM('FEMALE','MALE','ANY') NOT NULL DEFAULT 'ANY',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_health_plan_account FOREIGN KEY (account_id) REFERENCES account(id),
  INDEX ix_health_plan_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE health_plan_step (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  health_plan_id BIGINT NOT NULL,
  step_order SMALLINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  vaccine_id BIGINT NULL,
  age_months_min SMALLINT NULL,
  recurrence_months SMALLINT NULL,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_step_plan FOREIGN KEY (health_plan_id) REFERENCES health_plan(id) ON DELETE CASCADE,
  CONSTRAINT fk_step_vaccine FOREIGN KEY (vaccine_id) REFERENCES vaccine(id),
  INDEX ix_step_plan_order (health_plan_id, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE animal_health_plan (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  health_plan_id BIGINT NOT NULL,
  animal_id BIGINT NULL,
  lot_id BIGINT NULL,
  assigned_at DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ahp_account FOREIGN KEY (account_id) REFERENCES account(id),
  CONSTRAINT fk_ahp_plan FOREIGN KEY (health_plan_id) REFERENCES health_plan(id),
  CONSTRAINT fk_ahp_animal FOREIGN KEY (animal_id) REFERENCES animal(id),
  CONSTRAINT fk_ahp_lot FOREIGN KEY (lot_id) REFERENCES lot(id),
  INDEX ix_ahp_animal (account_id, animal_id),
  INDEX ix_ahp_lot (account_id, lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed planes globales
INSERT INTO health_plan (id, account_id, name, description, applies_to_purpose, applies_to_sex) VALUES
  (1, NULL, 'Plan Estandar Lecheria', 'Calendario estandar para dairy', 'DAIRY', 'ANY'),
  (2, NULL, 'Plan Estandar Engorda', 'Calendario estandar para beef', 'BEEF', 'ANY'),
  (3, NULL, 'Plan Tropical Cebuino', 'Beef + Anthrax + garrapata periodica', 'ANY', 'ANY')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO health_plan_step (health_plan_id, step_order, name, vaccine_id, age_months_min, recurrence_months) VALUES
  (1, 1, 'Brucella RB51 a 4 meses', (SELECT id FROM vaccine WHERE code='BRUCELLA_RB51'), 4, NULL),
  (1, 2, 'IBR/BVD/PI3/BRSV a 6 meses', (SELECT id FROM vaccine WHERE code='IBR_BVD_PI3_BRSV'), 6, 12),
  (1, 3, 'Leptospira a 6 meses', (SELECT id FROM vaccine WHERE code='LEPTOSPIRA_PENTAVALENTE'), 6, 6),
  (1, 4, 'Carbon sintomatico a 4 meses', (SELECT id FROM vaccine WHERE code='CARBON_SINTOMATICO'), 4, 12),
  (1, 5, 'Mastitis J5 preparto', (SELECT id FROM vaccine WHERE code='MASTITIS_J5'), 24, 6),
  (2, 1, 'IBR/BVD a 6 meses', (SELECT id FROM vaccine WHERE code='IBR_BVD_PI3_BRSV'), 6, 12),
  (2, 2, 'Carbon sintomatico a 4 meses', (SELECT id FROM vaccine WHERE code='CARBON_SINTOMATICO'), 4, 12),
  (2, 3, 'Pasteurella a 6 meses', (SELECT id FROM vaccine WHERE code='PASTEURELLA'), 6, 12),
  (3, 1, 'IBR/BVD a 6 meses', (SELECT id FROM vaccine WHERE code='IBR_BVD_PI3_BRSV'), 6, 12),
  (3, 2, 'Carbon sintomatico a 4 meses', (SELECT id FROM vaccine WHERE code='CARBON_SINTOMATICO'), 4, 12),
  (3, 3, 'Anthrax anual', (SELECT id FROM vaccine WHERE code='ANTHRAX'), 6, 12)
ON DUPLICATE KEY UPDATE name=VALUES(name);
