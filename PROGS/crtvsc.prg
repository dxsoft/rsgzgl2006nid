FUNCTION crtvsc

PARAMETERS ttUpdates,tcConn

IF USED("sc")
    USE IN sc
ENDIF

SQLEXEC(tcConn,"select * from sc where dwbm='"+m.pdwbm+"'","sc")

IF ttUpdates
	CURSORSETPROP("tables","sc","sc")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","sc")

	fn=""
	FOR i=1 TO FCOUNT("sc")
    	fn=fn+","+FIELD(i,"sc")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"sc")

	fn=""
	FOR i=1 TO FCOUNT("sc")
   	    fn=fn+","+FIELD(i,"sc")+" sc."+FIELD(i,"sc")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"sc")

	CURSORSETPROP("sendupdates",.t.,"sc")
ENDIF

SELECT sc
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
