-- V14 (Fase 6): enriquece el catalogo global de medicamentos con
-- presentacion comercial, fabricante y codigo de barras, y permite
-- entradas propias por tenant (account_id NULL = seed global).

-- Sin clausulas AFTER: TiDB no permite posicionar una columna nueva despues de otra
-- columna agregada en el mismo ALTER. La posicion fisica no afecta al ORM.
ALTER TABLE medication
  ADD COLUMN account_id BIGINT NULL,
  ADD COLUMN manufacturer VARCHAR(160) NULL,
  ADD COLUMN presentation VARCHAR(160) NULL,
  ADD COLUMN barcode VARCHAR(40) NULL,
  ADD COLUMN category ENUM('VACCINE','ANTIBIOTIC','ANTIPARASITIC','HORMONE','VITAMIN','ANTIINFLAMMATORY','OTHER')
    NOT NULL DEFAULT 'OTHER';

CREATE INDEX idx_medication_account ON medication(account_id);
CREATE INDEX idx_medication_barcode ON medication(barcode);
-- Unicidad de codigo: relajada para permitir mismas SKUs por tenant.
-- El UNIQUE original sobre code se conserva solo para entradas globales (account_id IS NULL).
-- Para evitar duplicados por tenant usamos unique compuesto (account_id, code).
ALTER TABLE medication
  DROP INDEX code,
  ADD UNIQUE KEY uk_medication_account_code (account_id, code);

-- Categorizar los seeds existentes
UPDATE medication SET category = 'ANTIBIOTIC' WHERE code IN ('OXITETRACICLINA_LA','PENICILINA','ENROFLOXACINA','CEFTIOFUR','AMOXICILINA','TIAMULINA');
UPDATE medication SET category = 'ANTIPARASITIC' WHERE code IN ('IVERMECTINA','CLORSULON');
UPDATE medication SET category = 'ANTIINFLAMMATORY' WHERE code IN ('FLUNIXIN','DEXAMETASONA');
