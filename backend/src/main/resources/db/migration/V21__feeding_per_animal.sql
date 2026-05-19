-- V21: permitir alimentar a un solo animal, no solo por lote.
-- lot_id pasa a NULL; agregamos animal_id NULL; el backend exige
-- que al menos uno de los dos venga lleno.
ALTER TABLE feeding_record
  MODIFY COLUMN lot_id BIGINT NULL,
  ADD COLUMN animal_id BIGINT NULL AFTER lot_id;

CREATE INDEX idx_feeding_record_animal ON feeding_record(animal_id);
