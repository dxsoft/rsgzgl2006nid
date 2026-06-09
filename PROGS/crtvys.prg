FUNCTION crtvys

PARAMETERS ttUpdates,tcConn

IF USED("ys")
    USE IN ys
ENDIF

SQLEXEC(tcConn,"select * from ys where dwbm='"+m.pdwbm+"'","ys")

IF ttUpdates
	CURSORSETPROP("tables","ys","ys")
	CURSORSETPROP("keyfieldlist","dwbm,grbm","ys")

	fn=""
	FOR i=1 TO FCOUNT("ys")
    	fn=fn+","+FIELD(i,"ys")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"ys")

	fn=""
	FOR i=1 TO FCOUNT("ys")
   	    fn=fn+","+FIELD(i,"ys")+" ys."+FIELD(i,"ys")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"ys")

	CURSORSETPROP("sendupdates",.t.,"ys")
ENDIF

SELECT ys
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
