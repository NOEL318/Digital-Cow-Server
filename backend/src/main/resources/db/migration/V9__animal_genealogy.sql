-- V9: genealogia del animal (padre/madre internos o externos y peso al nacer).
-- TiDB no admite referenciar una columna recien agregada dentro del MISMO ALTER TABLE,
-- ni en la clausula AFTER ni en indices o llaves foraneas (da "Unknown column"). Por eso:
-- (1) se agregan las columnas sin AFTER (la posicion fisica es irrelevante para el ORM) y
-- (2) los indices y llaves foraneas van en una sentencia separada. Valido tambien en MySQL.
ALTER TABLE animal
  ADD COLUMN sire_id BIGINT NULL,
  ADD COLUMN external_sire_name VARCHAR(160) NULL,
  ADD COLUMN dam_id BIGINT NULL,
  ADD COLUMN birth_weight_kg DECIMAL(5,2) NULL;

ALTER TABLE animal
  ADD CONSTRAINT fk_animal_sire FOREIGN KEY (sire_id) REFERENCES animal(id),
  ADD CONSTRAINT fk_animal_dam FOREIGN KEY (dam_id) REFERENCES animal(id),
  ADD INDEX ix_animal_sire (account_id, sire_id),
  ADD INDEX ix_animal_dam (account_id, dam_id);
