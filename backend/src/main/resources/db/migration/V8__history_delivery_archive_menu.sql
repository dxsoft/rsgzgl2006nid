INSERT INTO sys_menu(code, parent_code, title, icon, view_name, sequence_no, status)
VALUES ('SALARY_DELIVERY_ARCHIVE', 'SALARY', '交付归档', 'archive', 'workbench', 130, 'ACTIVE')
ON DUPLICATE KEY UPDATE
    parent_code = VALUES(parent_code),
    title = VALUES(title),
    icon = VALUES(icon),
    view_name = VALUES(view_name),
    sequence_no = VALUES(sequence_no),
    status = VALUES(status);
