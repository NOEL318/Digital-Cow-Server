ALTER TABLE animal
  ADD COLUMN sire_id BIGINT NULL AFTER cover_photo_id,
  ADD COLUMN external_sire_name VARCHAR(160) NULL AFTER sire_id,
  ADD COLUMN dam_id BIGINT NULL AFTER external_sire_name,
  ADD COLUMN birth_weight_kg DECIMAL(5,2) NULL AFTER dam_id,
  ADD CONSTRAINT fk_animal_sire FOREIGN KEY (sire_id) REFERENCES animal(id),
  ADD CONSTRAINT fk_animal_dam FOREIGN KEY (dam_id) REFERENCES animal(id),
  ADD INDEX ix_animal_sire (account_id, sire_id),
  ADD INDEX ix_animal_dam (account_id, dam_id);
