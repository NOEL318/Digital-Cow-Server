-- V18 (Fase 6.2): Polígono del lote para dibujar potreros en el mapa.
-- Almacena la lista de coordenadas como JSON: [[lat,lng],[lat,lng],...]

ALTER TABLE lot
  ADD COLUMN polygon JSON NULL AFTER notes,
  ADD COLUMN center_lat DECIMAL(9,6) NULL,
  ADD COLUMN center_lng DECIMAL(9,6) NULL;
