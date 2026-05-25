-- V22: token publico para compartir un animal en solo lectura.
-- Se separa el ADD COLUMN del UNIQUE porque TiDB no admite referenciar una columna
-- recien agregada en una constraint dentro del mismo ALTER TABLE.
ALTER TABLE animal
  ADD COLUMN share_token VARCHAR(64) NULL;

ALTER TABLE animal
  ADD CONSTRAINT uq_animal_share_token UNIQUE (share_token);
