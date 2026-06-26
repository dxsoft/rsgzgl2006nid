package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.SalaryReconcileCommand;
import com.dx.rsgzgl.salary.dto.SalaryReconcileResult;

public interface SalaryReconcileService {

    SalaryReconcileResult reconcile(SalaryReconcileCommand command);
}
