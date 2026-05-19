CREATE TABLE breed (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(40) NOT NULL,
  name_es VARCHAR(120) NOT NULL,
  name_en VARCHAR(120) NOT NULL,
  species ENUM('BOVINE') NOT NULL DEFAULT 'BOVINE',
  category ENUM('DAIRY','BEEF','DUAL') NOT NULL,
  bos ENUM('TAURUS','INDICUS','CROSS') NOT NULL,
  CONSTRAINT uq_breed_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO breed (code, name_es, name_en, species, category, bos) VALUES
  ('HOLSTEIN','Holstein','Holstein','BOVINE','DAIRY','TAURUS'),
  ('JERSEY','Jersey','Jersey','BOVINE','DAIRY','TAURUS'),
  ('PARDO_SUIZO','Pardo Suizo','Brown Swiss','BOVINE','DAIRY','TAURUS'),
  ('GYR','Gyr','Gyr','BOVINE','DAIRY','INDICUS'),
  ('GIROLANDO','Girolando','Girolando','BOVINE','DAIRY','CROSS'),
  ('ANGUS','Angus','Angus','BOVINE','BEEF','TAURUS'),
  ('HEREFORD','Hereford','Hereford','BOVINE','BEEF','TAURUS'),
  ('CHAROLAIS','Charolais','Charolais','BOVINE','BEEF','TAURUS'),
  ('BRAHMAN','Brahman','Brahman','BOVINE','BEEF','INDICUS'),
  ('BRANGUS','Brangus','Brangus','BOVINE','BEEF','CROSS'),
  ('BEEFMASTER','Beefmaster','Beefmaster','BOVINE','BEEF','CROSS'),
  ('SIMMENTAL','Simmental','Simmental','BOVINE','DUAL','TAURUS'),
  ('LIMOUSIN','Limousin','Limousin','BOVINE','BEEF','TAURUS'),
  ('NELORE','Nelore','Nelore','BOVINE','BEEF','INDICUS'),
  ('SENEPOL','Senepol','Senepol','BOVINE','BEEF','TAURUS'),
  ('SANTA_GERTRUDIS','Santa Gertrudis','Santa Gertrudis','BOVINE','BEEF','CROSS'),
  ('SIMBRAH','Simbrah','Simbrah','BOVINE','DUAL','CROSS')
ON DUPLICATE KEY UPDATE name_es=VALUES(name_es), name_en=VALUES(name_en);
