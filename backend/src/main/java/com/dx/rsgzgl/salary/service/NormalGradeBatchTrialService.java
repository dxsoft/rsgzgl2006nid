package com.dx.rsgzgl.salary.service;

import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeBatchTrialResult;

public interface NormalGradeBatchTrialService {

    NormalGradeBatchTrialResult trial(NormalGradeBatchTrialCommand command);
}
