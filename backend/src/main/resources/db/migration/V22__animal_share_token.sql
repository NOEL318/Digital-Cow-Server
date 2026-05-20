ALTER TABLE animal
  ADD COLUMN share_token VARCHAR(64) NULL,
  ADD CONSTRAINT uq_animal_share_token UNIQUE (share_token);
