-- V17 (Fase 6.1): Condiciones del corral / lote.
-- Permite registrar cosas como: nivel de lodo, lluvia fuerte, plagas
-- de moscas, calor extremo, suministro de agua interrumpido, etc.
-- El tipo es un enum extensible mas un texto libre para personalizar.

CREATE TABLE lot_condition (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id BIGINT NOT NULL,
  lot_id BIGINT NOT NULL,
  observed_at DATE NOT NULL,
  kind ENUM(
    'MUD_LOW','MUD_MEDIUM','MUD_HIGH',
    'RAIN_LIGHT','RAIN_HEAVY','DROUGHT',
    'HEAT_WAVE','COLD',
    'FLIES','TICKS','OTHER_PEST',
    'WATER_OK','WATER_LOW','WATER_OUT',
    'PASTURE_GOOD','PASTURE_LOW',
    'INFRA_DAMAGE',
    'CUSTOM'
  ) NOT NULL,
  -- Severidad opcional para tipos que admiten escala (1 a 5).
  severity TINYINT NULL,
  -- Etiqueta libre cuando kind = CUSTOM, o anotacion extra para los predefinidos.
  custom_label VARCHAR(80) NULL,
  notes VARCHAR(400) NULL,
  recorded_by_user_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_lot_condition_account (account_id),
  INDEX idx_lot_condition_lot_date (lot_id, observed_at DESC),
  INDEX idx_lot_condition_kind (account_id, kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
