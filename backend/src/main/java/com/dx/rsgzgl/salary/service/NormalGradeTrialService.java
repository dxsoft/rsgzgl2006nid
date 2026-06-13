package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;

public interface NormalGradeTrialService {

    NormalGradeTrialResult trial(NormalGradeTrialCommand command);
}
