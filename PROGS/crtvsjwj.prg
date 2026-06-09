FUNCTION crtvsjwj

PARAMETERS tcConn

IF USED("sjwj")
    USE IN sjwj
ENDIF
SQLEXEC(tcConn,"select * from sjwj where sym1='zz'","sjwj")
