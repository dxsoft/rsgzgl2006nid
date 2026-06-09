FUNCTION crtvlczjltxf

PARAMETERS ttUpdates,tcConn

IF USED("lczjltxf")
    USE IN lczjltxf
ENDIF

SQLEXEC(tcConn,"select * from lczjltxf where dwbm='"+m.pdwbm+"'","lczjltxf")

*!*	SQLEXEC(tcConn,"select * from lczjltxf","lczjltxf")

IF ttUpdates
	CURSORSETPROP("tables","lczjltxf","lczjltxf")
	CURSORSETPROP("keyfieldlist","dwbm,grbm,bdyj,bdsj","lczjltxf")

	fn=""
	FOR i=1 TO FCOUNT("lczjltxf")
    	fn=fn+","+FIELD(i,"lczjltxf")
	ENDFOR

	CURSORSETPROP("updatablefieldlist",SUBSTR(fn,2),"lczjltxf")

	fn=""
	FOR i=1 TO FCOUNT("lczjltxf")
   	    fn=fn+","+FIELD(i,"lczjltxf")+" lczjltxf."+FIELD(i,"lczjltxf")
	ENDFOR

	CURSORSETPROP("updatenamelist",SUBSTR(fn,2),"lczjltxf")

	CURSORSETPROP("sendupdates",.t.,"lczjltxf")
ENDIF

SELECT lczjltxf
INDEX ON dwbm+grbm TAG dwgrbm ADDITIVE
INDEX ON bdsj TAG bdsj ADDITIVE
INDEX ON dwbm+grbm+ALLTRIM(STR(gzbdxh)) TAG bm ADDITIVE
INDEX ON dwbm+grbm+bdyj+bdsj TAG bdbm ADDITIVE
