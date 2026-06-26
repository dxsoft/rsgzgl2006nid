-- Roll back SDBT to the original shared configuration if needed.
UPDATE fldgz
SET category = '00',
    category6 = '00'
WHERE UPPER(TRIM(field_name)) = 'SDBT';
