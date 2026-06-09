FUNCTION newid

LOCAL lnid

IF UPPER(m.dbtype)="MYSQL"
    SQLEXEC(conn,'select uuid() as id','ll')
ELSE
    SQLEXEC(conn,'select newid() as id','ll')
ENDIF
lnid=ll.id
USE IN ll

RETURN lnid