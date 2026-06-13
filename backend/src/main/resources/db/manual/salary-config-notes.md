# Salary Field Configuration

Salary item titles and composition come from `fldgz`.

## Category

- `00`: shared by civil servant and institution staff.
- `01`: civil servant only.
- `10`: institution staff only.

The application uses `category6` for salary records in 2006 and earlier, and `category` for later records.

## Titles

- `field_cap`: title used for civil servants and ordinary administrative rows.
- `field_caps`: title used for institution staff and special post types where `dwsx` is `07`, `08`, `09`, or `10`.

For example, `DFBT2` can remain shared:

- civil servant title: `field_cap = 生活性补贴`
- institution title: `field_caps = 基础绩效`

`SDBT` should be civil-servant-only:

```sql
UPDATE fldgz
SET category = '01',
    category6 = '01'
WHERE UPPER(TRIM(field_name)) = 'SDBT';
```

This update has been applied to the local `gzjsgl` database. Use
`salary-field-category-rollback.sql` only if the old shared configuration must
be restored.
