-- V6: Catalogos sanitarios globales (vacunas, enfermedades, medicamentos, plagas).
-- Sin account_id: son datos seed compartidos por todos los tenants.

CREATE TABLE vaccine (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  target_diseases VARCHAR(400) NULL,
  default_dose_ml DECIMAL(5,2) NULL,
  route ENUM('IM','SC','ORAL','INTRANASAL','TOPICAL') NULL,
  recommended_age_months SMALLINT NULL,
  recommended_frequency_months SMALLINT NULL,
  species VARCHAR(20) NOT NULL DEFAULT 'BOVINE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disease (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  category ENUM('BACTERIAL','VIRAL','PARASITIC','METABOLIC','NUTRITIONAL','MECHANICAL','OTHER') NOT NULL,
  zoonotic BOOLEAN NOT NULL DEFAULT FALSE,
  severity ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  default_symptoms VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medication (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  active_ingredient VARCHAR(200) NULL,
  default_dose VARCHAR(120) NULL,
  default_route ENUM('IM','SC','IV','ORAL','TOPICAL','INTRAMAMMARY') NULL,
  withdrawal_milk_days SMALLINT NOT NULL DEFAULT 0,
  withdrawal_meat_days SMALLINT NOT NULL DEFAULT 0,
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pest (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  name_es VARCHAR(160) NOT NULL,
  name_en VARCHAR(160) NOT NULL,
  scientific_name VARCHAR(160) NULL,
  type ENUM('TICK','FLY','WORM','LICE','MITE','OTHER') NOT NULL,
  region ENUM('TROPICAL','TEMPERATE','ANY') NOT NULL DEFAULT 'ANY',
  notes VARCHAR(400) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed vaccines
INSERT INTO vaccine (code, name_es, name_en, route, recommended_frequency_months) VALUES
  ('BRUCELLA_RB51', 'Brucella RB51', 'Brucella RB51', 'SC', NULL),
  ('IBR_BVD_PI3_BRSV', 'IBR/BVD/PI3/BRSV (Bovi-Shield)', 'IBR/BVD/PI3/BRSV (Bovi-Shield)', 'IM', 12),
  ('LEPTOSPIRA_PENTAVALENTE', 'Leptospira Pentavalente', 'Leptospira Pentavalent', 'IM', 6),
  ('CARBON_SINTOMATICO', 'Carbon sintomatico (Clostridiosis)', 'Blackleg (Clostridiosis)', 'SC', 12),
  ('RABIA_BOVINA', 'Rabia bovina', 'Bovine rabies', 'IM', 12),
  ('PASTEURELLA', 'Pasteurella multocida', 'Pasteurella multocida', 'SC', 12),
  ('FIEBRE_AFTOSA', 'Fiebre aftosa', 'Foot-and-mouth disease', 'IM', 6),
  ('ANTHRAX', 'Antrax', 'Anthrax', 'SC', 12),
  ('MASTITIS_J5', 'Mastitis Coliforme J5', 'Coliform Mastitis J5', 'IM', 6)
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);

-- Seed diseases
INSERT INTO disease (code, name_es, name_en, category, zoonotic, severity) VALUES
  ('MASTITIS', 'Mastitis', 'Mastitis', 'BACTERIAL', FALSE, 'MEDIUM'),
  ('BRD', 'Complejo respiratorio bovino', 'Bovine Respiratory Disease', 'BACTERIAL', FALSE, 'HIGH'),
  ('DIARREA_NEONATAL', 'Diarrea neonatal', 'Neonatal diarrhea', 'BACTERIAL', FALSE, 'HIGH'),
  ('COJERA', 'Cojera', 'Lameness', 'MECHANICAL', FALSE, 'MEDIUM'),
  ('BRUCELOSIS', 'Brucelosis', 'Brucellosis', 'BACTERIAL', TRUE, 'HIGH'),
  ('TUBERCULOSIS', 'Tuberculosis bovina', 'Bovine tuberculosis', 'BACTERIAL', TRUE, 'HIGH'),
  ('LEPTOSPIROSIS', 'Leptospirosis', 'Leptospirosis', 'BACTERIAL', TRUE, 'HIGH'),
  ('FIEBRE_AFTOSA_DIS', 'Fiebre aftosa', 'Foot-and-mouth disease', 'VIRAL', FALSE, 'HIGH'),
  ('IBR', 'Rinotraqueitis infecciosa bovina', 'Infectious bovine rhinotracheitis', 'VIRAL', FALSE, 'MEDIUM'),
  ('BVD', 'Diarrea viral bovina', 'Bovine viral diarrhea', 'VIRAL', FALSE, 'HIGH'),
  ('ANAPLASMOSIS', 'Anaplasmosis', 'Anaplasmosis', 'PARASITIC', FALSE, 'HIGH'),
  ('BABESIOSIS', 'Piroplasmosis (Babesiosis)', 'Babesiosis', 'PARASITIC', FALSE, 'HIGH'),
  ('ACETOSIS', 'Acetonemia', 'Ketosis', 'METABOLIC', FALSE, 'MEDIUM'),
  ('HIPOCALCEMIA', 'Hipocalcemia (Fiebre de leche)', 'Milk fever', 'METABOLIC', FALSE, 'HIGH'),
  ('METRITIS', 'Metritis', 'Metritis', 'BACTERIAL', FALSE, 'MEDIUM'),
  ('RETENCION_PLACENTA', 'Retencion placentaria', 'Retained placenta', 'OTHER', FALSE, 'MEDIUM')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en), severity=VALUES(severity);

-- Seed medications
INSERT INTO medication (code, name_es, name_en, active_ingredient, withdrawal_milk_days, withdrawal_meat_days) VALUES
  ('OXITETRACICLINA_LA', 'Oxitetraciclina LA', 'Long-acting Oxytetracycline', 'Oxitetraciclina', 21, 28),
  ('PENICILINA', 'Penicilina G procainica', 'Penicillin G procaine', 'Penicillin G', 3, 14),
  ('ENROFLOXACINA', 'Enrofloxacina', 'Enrofloxacin', 'Enrofloxacin', 4, 14),
  ('IVERMECTINA', 'Ivermectina 1%', 'Ivermectin 1%', 'Ivermectin', 28, 35),
  ('FLUNIXIN', 'Flunixin meglumine', 'Flunixin meglumine', 'Flunixin meglumine', 2, 4),
  ('CEFTIOFUR', 'Ceftiofur', 'Ceftiofur', 'Ceftiofur', 0, 4),
  ('AMOXICILINA', 'Amoxicilina', 'Amoxicillin', 'Amoxicillin', 4, 25),
  ('DEXAMETASONA', 'Dexametasona', 'Dexamethasone', 'Dexamethasone', 3, 14),
  ('TIAMULINA', 'Tiamulina', 'Tiamulin', 'Tiamulin', 1, 5),
  ('CLORSULON', 'Clorsulon (fasciolicida)', 'Clorsulon (flukicide)', 'Clorsulon', 30, 8)
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en), withdrawal_milk_days=VALUES(withdrawal_milk_days), withdrawal_meat_days=VALUES(withdrawal_meat_days);

-- Seed pests
INSERT INTO pest (code, name_es, name_en, scientific_name, type, region) VALUES
  ('GARRAPATA_COMUN', 'Garrapata comun', 'Common cattle tick', 'Rhipicephalus microplus', 'TICK', 'TROPICAL'),
  ('MOSCA_CUERNO', 'Mosca del cuerno', 'Horn fly', 'Haematobia irritans', 'FLY', 'ANY'),
  ('MOSCA_BRAVA', 'Mosca brava', 'Stable fly', 'Stomoxys calcitrans', 'FLY', 'ANY'),
  ('GUSANO_BARRENADOR', 'Gusano barrenador', 'Screwworm', 'Cochliomyia hominivorax', 'WORM', 'TROPICAL'),
  ('PIOJO_BOVINO', 'Piojo bovino', 'Cattle louse', 'Haematopinus eurysternus', 'LICE', 'TEMPERATE'),
  ('ACARO_SARCOPTICO', 'Acaro sarcoptico', 'Sarcoptic mite', 'Sarcoptes scabiei', 'MITE', 'ANY'),
  ('GASTERINTESTINALES', 'Parasitos gastrointestinales', 'Gastrointestinal parasites', 'varios', 'WORM', 'ANY'),
  ('FASCIOLA', 'Fasciola hepatica', 'Liver fluke', 'Fasciola hepatica', 'WORM', 'TEMPERATE')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
