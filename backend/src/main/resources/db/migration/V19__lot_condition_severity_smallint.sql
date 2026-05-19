-- V19 (fix): la entity LotCondition mapea severity como Short, que en
-- Hibernate equivale a SMALLINT. En V17 quedo como TINYINT y la
-- validacion de schema falla al iniciar el backend.

ALTER TABLE lot_condition
  MODIFY COLUMN severity SMALLINT NULL;
