-- Salary item composition is controlled by fldgz.category/category6:
--   00 = shared item
--   01 = civil servant item
--   10 = institution staff item
--
-- SDBT is "working allowance" for civil servants. Institution staff do not have
-- this item, so it should not be configured as shared.
UPDATE fldgz
SET category = '01',
    category6 = '01'
WHERE UPPER(TRIM(field_name)) = 'SDBT';
