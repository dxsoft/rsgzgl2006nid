package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryBatchReconcileResult;

public interface SalaryBatchReconcileService {

    SalaryBatchReconcileResult reconcile(SalaryBatchReconcileCommand command);
}
