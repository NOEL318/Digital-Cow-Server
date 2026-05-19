CREATE TABLE ranch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  location VARCHAR(200) NULL,
  latitude DECIMAL(9,6) NULL,
  longitude DECIMAL(9,6) NULL,
  area_hectares DECIMAL(10,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_ranch_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  INDEX ix_ranch_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id BIGINT NOT NULL,
  ranch_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  area_hectares DECIMAL(10,2) NULL,
  notes TEXT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_lot_account FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE RESTRICT,
  CONSTRAINT fk_lot_ranch FOREIGN KEY (ranch_id) REFERENCES ranch(id) ON DELETE RESTRICT,
  INDEX ix_lot_account_ranch (account_id, ranch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
