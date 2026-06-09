PROCEDURE initi

IF MESSAGEBOX("系统初始化将删除所有的人员信息(单位信息除外), 请慎重选择! "+CHR(10)+CHR(13)+CHR(10)+CHR(13)+"是否继续?",4+48+256,"警告!")=6
    WAIT "正在进行系统初始化, 请耐心等待......" WINDOW AT SROWS()/2,SCOLS()/2-10 NOWAIT
    
    IF UPPER(m.dbtype)="MYSQL"
		llSuccess=.t.
		
		* 设置事务模式为手动提交
		SQLSETPROP(conn, "Transactions", 2)  && 2 = 手动提交

	    lnResult=SQLEXEC(conn, "delete from dwbm;delete from djxgzb;delete from dryjbxxb;delete from dndkhb;delete from dryzwbhb;delete from dtgxxb;delete from dxlb;delete from hisbaseb;"+"delete from hjxxb;delete from jdzwb;delete from qtqkb;delete from tgqgz2006b;delete from jytgzzbfb;delete from jfjsb;")
        IF lnResult<=0
            llSucess=.f.
        ELSE
	        lnResult=SQLEXEC(conn, "delete from djxgz;delete from dryjbxx;delete from dndkh;delete from dryzwbh;delete from dtgxx;delete from dxl;delete from hisbase;"+"delete from hjxx;delete from jdzw;delete from qtqk;delete from tgqgz2006;delete from jytgzzbf;delete from jfjs;")
	        IF lnResult<=0
	            llSuccess=.f.
	        ENDIF
	    ENDIF

		* 事务处理
		IF llSuccess
		    * 提交事务
		    lnCommit = SQLCOMMIT(conn)
		    IF lnCommit > 0
		        MESSAGEBOX("数据初始化完成！ ", 64, "成功")
		    ELSE
		        MESSAGEBOX("事务提交失败", 16, "错误")
		    ENDIF
		ELSE
		    * 回滚事务
		    lnRollback = SQLROLLBACK(conn)
		    MESSAGEBOX("数据导入失败，事务已回滚", 16, "错误")
		ENDIF

		* 设置事务模式为手动提交
		SQLSETPROP(conn, "Transactions", 1)  && 2 = 手动提交


    ELSE
	    result=0
	    aa=SQLEXEC(conn, "p_init ?@result")
	    IF result>0
	        MESSAGEBOX("初始化失败！",64,"提示")
		ELSE
		    MESSAGEBOX("初始化完毕!",64,"消息")
	    ENDIF
	ENDIF
ENDIF
        