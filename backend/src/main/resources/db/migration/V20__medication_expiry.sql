-- V20: fecha de caducidad del medicamento + clonado de categorias globales.
-- (1) Caducidad del medicamento (opcional, nullable)
ALTER TABLE medication
  ADD COLUMN expires_at DATE NULL AFTER barcode;

-- (2) Copiar todas las categorias globales de gastos e ingresos a cada
--     cuenta existente, asignandoles account_id propio para que el
--     usuario pueda editarlas y/o eliminarlas. Se evita duplicar
--     cuando ya existe una con el mismo code para esa cuenta.
INSERT INTO expense_category (account_id, code, name_es, name_en, kind, created_at, updated_at)
SELECT a.id, g.code, g.name_es, g.name_en, g.kind, NOW(), NOW()
  FROM account a
 CROSS JOIN expense_category g
 WHERE g.account_id IS NULL
   AND NOT EXISTS (
     SELECT 1 FROM expense_category e
      WHERE e.account_id = a.id AND e.code = g.code
   );

INSERT INTO income_category (account_id, code, name_es, name_en, kind, created_at, updated_at)
SELECT a.id, g.code, g.name_es, g.name_en, g.kind, NOW(), NOW()
  FROM account a
 CROSS JOIN income_category g
 WHERE g.account_id IS NULL
   AND NOT EXISTS (
     SELECT 1 FROM income_category i
      WHERE i.account_id = a.id AND i.code = g.code
   );

-- (3) Eliminar las globales para que no aparezcan como "no editables".
--     Sus copias ya viven en cada cuenta.
DELETE FROM expense_category WHERE account_id IS NULL;
DELETE FROM income_category WHERE account_id IS NULL;
