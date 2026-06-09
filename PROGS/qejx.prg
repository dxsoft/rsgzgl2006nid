FUNCTION qejx

&&计算基础绩效

PARAMETERS tcdwbm,tczwbm,tcbz

LOCAL  latdfbt,v_bl,ajxbl,lnBz

IF LEFT(tczwbm,2)="03"
    tczwbm="01"+STRTRAN(STRTRAN(STRTRAN(SUBSTR(tczwbm,4,1),"B","A"),"C","B"),"D","B")+"0"
ENDIF

IF LEFT(tczwbm,2)='04'
	tczwbm="01"+SUBSTR(tczwbm,4,2)
ENDIF

IF AT("F",tczwbm)>0 AND LEFT(tcbz,4)<='2009'&&试用期，2009年前津补贴标准不分学历
    tczwbm=LEFT(tczwbm,3)+"F"
ENDIF

DIMENSION latdfbt[1]
DIMENSION ajxbl[1]

SELECT jxbl,jxlb FROM dwbm WHERE dwbm=tcdwbm INTO ARRAY ajxbl

IF _tally>0
	IF tcbz>='201410' AND INLIST(LEFT(tczwbm,2),"07","08","09","10","11")&&201410前区分绩效类别
		SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=5 INTO ARRAY latdfbt
	ELSE
	    IF INLIST(LEFT(tczwbm,2),"07","08","09","10","11")
		    SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=ajxbl[1,2] INTO ARRAY latdfbt
		ELSE
		    SELECT bz FROM bz06_jbt WHERE UPPER(item)=="DFBT2" AND zwbm=tczwbm AND tbnd=tcbz AND jxlb=1 INTO ARRAY latdfbt
        ENDIF
	ENDIF
		
	IF _tally>0
	    m.lnBz=latdfbt[1]
	ELSE
	    m.lnBz=0000
	ENDIF

    RETURN m.lnBz
	
ELSE
    RETURN 0000
ENDIF

