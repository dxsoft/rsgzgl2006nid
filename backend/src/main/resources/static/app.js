const TEXT = {
    loadingOrgs: "\u6b63\u5728\u52a0\u8f7d\u5355\u4f4d...",
    loadingPeople: "\u6b63\u5728\u52a0\u8f7d\u4eba\u5458...",
    noOrgs: "\u6682\u65e0\u5355\u4f4d\u6570\u636e",
    noPeople: "\u6ca1\u6709\u627e\u5230\u4eba\u5458\u3002",
    noHistory: "\u6682\u65e0\u5de5\u8d44\u5386\u53f2\u3002",
    noSalary: "\u6682\u65e0\u660e\u7ec6\u3002",
    noConfig: "\u6682\u65e0\u5de5\u8d44\u9879\u76ee\u914d\u7f6e\u3002",
    chooseSalary: "\u9009\u62e9\u5de5\u8d44\u5386\u53f2\u6216\u70b9\u51fb\u8bd5\u7b97\u3002",
    refresh: "\u6b63\u5728\u5237\u65b0\u6570\u636e...",
    serviceOk: "\u670d\u52a1\u8fde\u63a5\u6b63\u5e38",
    serviceFail: "\u670d\u52a1\u8fde\u63a5\u5931\u8d25",
    requestFail: "\u8bf7\u6c42\u5931\u8d25",
    loginRequired: "\u8bf7\u5148\u767b\u5f55",
    loggingIn: "\u6b63\u5728\u767b\u5f55...",
    loginFailed: "\u767b\u5f55\u5931\u8d25",
    loggedOut: "\u5df2\u9000\u51fa\u767b\u5f55",
    passwordChanged: "\u5bc6\u7801\u5df2\u4fee\u6539",
    passwordReset: "\u5bc6\u7801\u5df2\u91cd\u7f6e\u4e3a 123456",
    salaryDetail: "\u5de5\u8d44\u660e\u7ec6",
    trialBaseline: "\u8bd5\u7b97\u57fa\u7ebf",
    ruleTrial: "\u5de5\u8d44\u89c4\u5219\u8bd5\u7b97",
    ruleTrialPassed: "\u5339\u914d\u5386\u53f2\u7ed3\u679c",
    ruleTrialFailed: "\u4e0e\u5386\u53f2\u6709\u5dee\u5f02",
    ruleTrialNoExpected: "\u65e0\u5386\u53f2\u8bb0\u5f55",
    loadingRuleTrial: "\u6b63\u5728\u8bd5\u7b97\u5de5\u8d44\u89c4\u5219...",
    loadingTimeline: "\u6b63\u5728\u91cd\u653e\u5de5\u8d44\u65f6\u95f4\u7ebf...",
    loadingGeneratedTimeline: "\u6b63\u5728\u6839\u636e\u57fa\u7840\u4fe1\u606f\u751f\u6210\u5e94\u53d1\u5de5\u8d44\u53d8\u52a8...",
    loadingWorkbench: "\u6b63\u5728\u52a0\u8f7d\u5de5\u4f5c\u53f0...",
    openingWorkItem: "\u6b63\u5728\u6253\u5f00\u5de5\u8d44\u4e1a\u52a1...",
    loadingCaseDetail: "\u6b63\u5728\u8bfb\u53d6\u529e\u7406\u8bb0\u5f55...",
    caseDetail: "\u5de5\u8d44\u529e\u7406\u8bb0\u5f55",
    previewingWorkItem: "\u6b63\u5728\u9884\u89c8\u5de5\u8d44\u8bd5\u7b97...",
    casePreview: "\u5de5\u8d44\u529e\u7406\u9884\u89c8",
    completingWorkItem: "\u6b63\u5728\u529e\u7406\u5de5\u8d44\u4e1a\u52a1...",
    workItemCompleted: "\u5de5\u8d44\u4e1a\u52a1\u5df2\u6807\u8bb0\u529e\u7406",
    historyWriteExecuteConfirm: "\u786e\u5b9a\u5c06\u5199\u5165\u8ba1\u5212 {caseNo} \u5199\u5165 hisbase \u5417\uff1f",
    historyWriteRollbackConfirm: "\u786e\u5b9a\u64a4\u9500\u5199\u5165\u8ba1\u5212 {caseNo} \u5417\uff1f",
    historyWriteBatchExecuteConfirm: "\u5f53\u524d\u7b5b\u9009\u5171 {total} \u6761\uff0c\u9884\u68c0\u53ef\u5199 {ready} \u6761\uff0c\u963b\u65ad {blocked} \u6761\uff0c\u8b66\u544a {warning} \u6761\u3002\u786e\u5b9a\u6279\u91cf\u5199\u5165\u53ef\u5199\u8ba1\u5212\u5417\uff1f",
    historyWriteBatchRollbackConfirm: "\u5f53\u524d\u7b5b\u9009\u5171 {total} \u6761\uff0c\u53ef\u64a4\u9500 {eligible} \u6761\u3002\u786e\u5b9a\u6279\u91cf\u64a4\u9500\u5df2\u6210\u529f\u5199\u5165\u7684\u8ba1\u5212\u5417\uff1f",
    refreshingTodoCache: "\u6b63\u5728\u5237\u65b0\u5de5\u8d44\u5f85\u529e\u7f13\u5b58...",
    todoCacheRefreshed: "\u5de5\u8d44\u5f85\u529e\u7f13\u5b58\u5df2\u5237\u65b0",
    cancellingWorkItem: "\u6b63\u5728\u64a4\u56de\u5de5\u8d44\u4e1a\u52a1...",
    workItemCancelled: "\u5de5\u8d44\u4e1a\u52a1\u5df2\u64a4\u56de",
    reviewingWorkItem: "\u6b63\u5728\u590d\u6838\u5de5\u8d44\u4e1a\u52a1...",
    workItemReviewed: "\u5de5\u8d44\u4e1a\u52a1\u5df2\u590d\u6838",
    forceConfirmHint: "\u8bd5\u7b97\u5f02\u5e38\uff0c\u786e\u8ba4\u540e\u5c06\u6309\u5f53\u524d\u5f85\u529e\u4fe1\u606f\u5f3a\u5236\u529e\u7406\u5e76\u56fa\u5316\u8be5\u5f02\u5e38\u8bf4\u660e\u3002",
    differenceConfirmHint: "\u8bd5\u7b97\u4e0e\u5386\u53f2\u6709\u5dee\u5f02\uff0c\u786e\u8ba4\u540e\u5c06\u56fa\u5316\u8be5\u5dee\u5f02\u8bf4\u660e\u3002",
    forceReasonRequired: "\u8bf7\u586b\u5199\u5f3a\u5236\u529e\u7406\u8bf4\u660e\u3002",
    differenceReasonRequired: "\u8bf7\u586b\u5199\u5dee\u5f02\u786e\u8ba4\u8bf4\u660e\u3002",
    cancelReasonRequired: "\u8bf7\u586b\u5199\u64a4\u56de\u529e\u7406\u8bf4\u660e\u3002",
    reviewReasonRequired: "\u8bf7\u586b\u5199\u590d\u6838\u8bf4\u660e\u3002",
    noWorkItems: "\u6682\u65e0\u4e1a\u52a1\u8bb0\u5f55\u3002",
    workbenchReady: "\u5de5\u4f5c\u53f0\u5df2\u66f4\u65b0",
    loadingMore: "\u6b63\u5728\u52a0\u8f7d\u66f4\u591a\u4e1a\u52a1...",
    noMoreItems: "\u6ca1\u6709\u66f4\u591a\u8bb0\u5f55\u3002",
    salaryWorkspaceReady: "\u5df2\u8fdb\u5165\u4eba\u5458\u5de5\u8d44",
    loadingSystem: "\u6b63\u5728\u52a0\u8f7d\u7cfb\u7edf\u7ba1\u7406...",
    savingSystem: "\u6b63\u5728\u4fdd\u5b58\u7cfb\u7edf\u7ba1\u7406...",
    systemReady: "\u7cfb\u7edf\u7ba1\u7406\u5df2\u66f4\u65b0",
    systemSaved: "\u7cfb\u7edf\u7ba1\u7406\u5df2\u4fdd\u5b58",
    menuPlaceholder: "\u8be5\u529f\u80fd\u6a21\u5757\u5df2\u7eb3\u5165\u83dc\u5355\uff0c\u5f85\u540e\u7eed\u63a5\u5165\u9875\u9762\u3002",
    timeline: "\u5de5\u8d44\u65f6\u95f4\u7ebf",
    timelineSummary: "\u68c0\u67e5 {checked} \u6761 | \u5339\u914d {matched} | \u5dee\u5f02 {different} | \u9519\u8bef {errors}",
    generatedTimeline: "\u57fa\u7840\u4fe1\u606f\u5e94\u53d1\u53d8\u52a8",
    generatedTimelineSummary: "\u5e94\u53d1 {expected} \u6761 | \u5339\u914d {matched} | \u5dee\u5f02 {different} | \u7f3a\u5931 {missing} | \u9519\u8bef {errors} | \u975e\u751f\u6210\u5386\u53f2 {unsupported}",
    reconcileResult: "\u5de5\u8d44\u5bf9\u8d26",
    reconcilePassed: "\u5bf9\u8d26\u901a\u8fc7",
    reconcileFailed: "\u5bf9\u8d26\u5f02\u5e38",
    batchReconcile: "\u6279\u91cf\u5bf9\u8d26",
    batchReconcileSummary: "\u68c0\u67e5 {checked} \u4eba | \u901a\u8fc7 {passed} | \u5dee\u5f02 {failed} | \u8df3\u8fc7 {skipped} | \u5dee\u989d {diff}",
    normalGradeBatch: "\u6279\u91cf\u664b\u6863\u8bd5\u7b97",
    normalGradeBatchSummary: "\u68c0\u67e5 {checked} \u4eba | \u5339\u914d {matched} | \u5dee\u5f02 {different} | \u65e0\u8bb0\u5f55 {noExpected} | \u8df3\u8fc7 {skipped} | \u7ea7\u522b\u664b\u5347 {levelPromotion} | \u4e0d\u7b26\u5408\u6761\u4ef6 {notEligible} | \u5012\u6863\u5dee {reverseStep} | \u5dee\u989d {diff}",
    chooseOrgForBatch: "\u8bf7\u5148\u9009\u62e9\u5355\u4f4d\u518d\u6279\u91cf\u5bf9\u8d26\u3002",
    loadingNormalGradeBatch: "\u6b63\u5728\u6279\u91cf\u8bd5\u7b97\u6b63\u5e38\u664b\u6863...",
    exportStarted: "\u6b63\u5728\u5bfc\u51fa\u5bf9\u8d26\u7ed3\u679c...",
    exportNormalGradeStarted: "\u6b63\u5728\u5bfc\u51fa\u6b63\u5e38\u664b\u6863\u8bd5\u7b97...",
    periodLoaded: "\u5df2\u9009\u7528\u6700\u65b0\u53ef\u5bf9\u8d26\u5e74\u6708 {year}-{month}",
    noPeriods: "\u5f53\u524d\u5355\u4f4d\u6682\u65e0\u5de5\u8d44\u5386\u53f2\u5e74\u6708\u3002",
    chooseHistoryForReconcile: "\u8bf7\u5148\u9009\u62e9\u4e00\u6761\u5de5\u8d44\u5386\u53f2\u518d\u5bf9\u8d26\u3002",
    salaryRecord: "\u5de5\u8d44\u8bb0\u5f55",
    total: "\u5408\u8ba1",
    page: "\u7b2c {page} \u9875",
    loadingConfig: "\u6b63\u5728\u52a0\u8f7d\u5de5\u8d44\u9879\u76ee\u914d\u7f6e...",
    loadingAllConfig: "\u6b63\u5728\u52a0\u8f7d\u5168\u90e8\u5de5\u8d44\u9879\u76ee...",
    loadingConfigItem: "\u6b63\u5728\u8bfb\u53d6\u5de5\u8d44\u9879\u914d\u7f6e...",
    editing: "\u6b63\u5728\u7f16\u8f91 {code}",
    editorTitle: "\u7f16\u8f91\u5de5\u8d44\u9879 {code}",
    edit: "\u7f16\u8f91",
    savingConfig: "\u6b63\u5728\u4fdd\u5b58\u5de5\u8d44\u9879\u914d\u7f6e...",
    savedConfig: "\u5de5\u8d44\u9879\u914d\u7f6e\u5df2\u4fdd\u5b58",
    noAudit: "\u6682\u65e0\u53d8\u66f4\u8bb0\u5f55\u3002",
    noBaseChanges: "\u6682\u65e0\u57fa\u7840\u8d44\u6599\u53d8\u66f4\u767b\u8bb0\u3002",
    loadingBaseInfo: "\u6b63\u5728\u52a0\u8f7d\u4eba\u5458\u57fa\u672c\u4fe1\u606f...",
    savingBaseInfo: "\u6b63\u5728\u4fdd\u5b58\u4eba\u5458\u57fa\u672c\u4fe1\u606f...",
    baseInfoSaved: "\u4eba\u5458\u57fa\u672c\u4fe1\u606f\u5df2\u4fdd\u5b58\uff0c\u5de5\u8d44\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0",
    loadingBaseStatus: "\u6b63\u5728\u52a0\u8f7d\u57fa\u7840\u8d44\u6599\u72b6\u6001...",
    refreshTodoCacheAction: "\u5237\u65b0\u5f85\u529e\u7f13\u5b58",
    loadingBaseChanges: "\u6b63\u5728\u52a0\u8f7d\u57fa\u7840\u8d44\u6599\u53d8\u66f4...",
    savingBaseChange: "\u6b63\u5728\u767b\u8bb0\u57fa\u7840\u8d44\u6599\u53d8\u66f4...",
    baseChangeSaved: "\u57fa\u7840\u8d44\u6599\u53d8\u66f4\u5df2\u767b\u8bb0\uff0c\u5de5\u8d44\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0",
    baseChangeSummaryRequired: "\u8bf7\u586b\u5199\u57fa\u7840\u8d44\u6599\u53d8\u66f4\u6458\u8981\u3002",
    noPersonPosts: "\u6682\u65e0\u4efb\u804c\u4fe1\u606f\u3002",
    loadingPersonPosts: "\u6b63\u5728\u52a0\u8f7d\u4efb\u804c\u4fe1\u606f...",
    savingPersonPost: "\u6b63\u5728\u4fdd\u5b58\u4efb\u804c\u4fe1\u606f...",
    personPostSaved: "\u4efb\u804c\u4fe1\u606f\u5df2\u4fdd\u5b58\uff0c\u5de5\u8d44\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0",
    personPostRequired: "\u8bf7\u586b\u5199\u4efb\u804c\u7f16\u7801\u548c\u4efb\u804c\u5e74\u6708\u3002",
    noEducations: "\u6682\u65e0\u5b66\u5386\u4fe1\u606f\u3002",
    loadingEducations: "\u6b63\u5728\u52a0\u8f7d\u5b66\u5386\u4fe1\u606f...",
    savingEducation: "\u6b63\u5728\u4fdd\u5b58\u5b66\u5386\u4fe1\u606f...",
    educationSaved: "\u5b66\u5386\u4fe1\u606f\u5df2\u4fdd\u5b58\uff0c\u5de5\u8d44\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0",
    educationRequired: "\u8bf7\u586b\u5199\u5b66\u5386\u7f16\u7801\u548c\u6bd5\u4e1a\u65f6\u95f4\u3002",
    noAssessments: "\u6682\u65e0\u5e74\u5ea6\u8003\u6838\u3002",
    loadingAssessments: "\u6b63\u5728\u52a0\u8f7d\u5e74\u5ea6\u8003\u6838...",
    savingAssessment: "\u6b63\u5728\u4fdd\u5b58\u5e74\u5ea6\u8003\u6838...",
    assessmentSaved: "\u5e74\u5ea6\u8003\u6838\u5df2\u4fdd\u5b58\uff0c\u5de5\u8d44\u5f85\u529e\u7f13\u5b58\u9700\u5237\u65b0",
    assessmentRequired: "\u8bf7\u586b\u5199\u8003\u6838\u5e74\u5ea6\u548c\u7ed3\u679c\u3002",
    loadingAudit: "\u6b63\u5728\u52a0\u8f7d\u53d8\u66f4\u8bb0\u5f55...",
    auditBadge: "\u5df2\u53d8\u66f4 {count} \u6b21",
    selected: "\u5df2\u9009\u62e9 {name}",
    loadingAcceptanceSample: "\u6b63\u5728\u8f7d\u5165\u4e1a\u52a1\u62bd\u67e5\u6837\u672c...",
    chooseAcceptanceSample: "\u8bf7\u5148\u9009\u62e9\u4e00\u6761\u62bd\u67e5\u6837\u672c\u3002",
    acceptanceSampleLoaded: "\u5df2\u8f7d\u5165\u62bd\u67e5\u6837\u672c {personCode} {year}-{month}",
    fields: {
        org: "\u5355\u4f4d",
        idCard: "\u8eab\u4efd\u8bc1",
        gender: "\u6027\u522b",
        birthDate: "\u51fa\u751f\u5e74\u6708",
        personCategory: "\u4eba\u5458\u7c7b\u522b",
        organizationType: "\u5355\u4f4d\u6027\u8d28",
        postCategory: "\u5c97\u4f4d\u7c7b\u522b",
        workStartDate: "\u53c2\u52a0\u5de5\u4f5c",
        joinOrgDate: "\u8fdb\u5165\u5355\u4f4d",
        currentPost: "\u73b0\u4efb\u804c\u52a1",
        postLevel: "\u804c\u52a1\u5c42\u7ea7",
        postStartDate: "\u4efb\u804c\u65f6\u95f4",
        workYears: "\u5de5\u9f84",
        education: "\u5b66\u5386",
        politicalStatus: "\u653f\u6cbb\u9762\u8c8c",
        nation: "\u6c11\u65cf",
        bankAccount: "\u94f6\u884c\u8d26\u53f7"
    }
};

const ACCEPTANCE_SAMPLES = [
    { label: "\u8c03\u6807/\u6d25\u8d34 00111-00019 2018-07", personCode: "00111-00019", orgCode: "00111", year: 2018, month: 7, changeType: "\u6d25\u8d34\u53d8\u5316" },
    { label: "\u804c\u52a1\u53d8\u5316-\u4e8b\u4e1a 00111-00053 2025-12", personCode: "00111-00053", orgCode: "00111", year: 2025, month: 12, changeType: "\u804c\u52a1\u53d8\u5316" },
    { label: "\u804c\u52a1\u53d8\u5316-\u6cd5\u68c0 044-00100 2021-06", personCode: "044-00100", orgCode: "044", year: 2021, month: 6, changeType: "\u804c\u52a1\u53d8\u5316" },
    { label: "\u65b0\u8fdb\u5de5\u8d44 00119-00041 2024-10", personCode: "00119-00041", orgCode: "00119", year: 2024, month: 10, changeType: "\u65b0\u8fdb\u5de5\u8d44" },
    { label: "\u89c1\u4e60\u5de5\u8d44 00806-00868 2024-11", personCode: "00806-00868", orgCode: "00806", year: 2024, month: 11, changeType: "\u89c1\u4e60\u5de5\u8d44" },
    { label: "\u8f6c\u6b63\u5b9a\u7ea7 00806-00868 2025-11", personCode: "00806-00868", orgCode: "00806", year: 2025, month: 11, changeType: "\u8f6c\u6b63\u5b9a\u7ea7" },
    { label: "\u8c03\u5165\u5b9a\u8d44 00806-00937 2025-01", personCode: "00806-00937", orgCode: "00806", year: 2025, month: 1, changeType: "\u8c03\u5165\u5b9a\u8d44" },
    { label: "\u8f6c\u4e1a\u5b9a\u8d44 001-00260 2022-01", personCode: "001-00260", orgCode: "001", year: 2022, month: 1, changeType: "\u8f6c\u4e1a\u5b9a\u8d44" },
    { label: "\u9000\u4f0d\u5b9a\u8d44 0142202-00019 2020-01", personCode: "0142202-00019", orgCode: "0142202", year: 2020, month: 1, changeType: "\u9000\u4f0d\u5b9a\u8d44" },
    { label: "\u5b66\u5386\u53d8\u5316-\u516c\u52a1\u5458 041-00210 2024-01", personCode: "041-00210", orgCode: "041", year: 2024, month: 1, changeType: "\u5b66\u5386\u53d8\u5316" },
    { label: "\u5b66\u5386\u53d8\u5316-\u4e8b\u4e1a 00122-00006 2022-07", personCode: "00122-00006", orgCode: "00122", year: 2022, month: 7, changeType: "\u5b66\u5386\u53d8\u5316" },
    { label: "\u6559\u62a4\u6d25\u8d34 00801-00025 2017-01", personCode: "00801-00025", orgCode: "00801", year: 2017, month: 1, changeType: "\u6559\u62a4\u6d25\u8d34" },
    { label: "\u6b63\u5e38\u664b\u6863/\u85aa\u7ea7 00105-00008 2025-01", personCode: "00105-00008", orgCode: "00105", year: 2025, month: 1, changeType: "\u6b63\u5e38\u6863\u6b21" },
    { label: "\u8b66\u8854\u53d8\u5316 024-00042 2024-08", personCode: "024-00042", orgCode: "024", year: 2024, month: 8, changeType: "\u8b66\u8854\u53d8\u5316" },
    { label: "\u8b66\u5458\u5957\u6539 024-00415 2023-03", personCode: "024-00415", orgCode: "024", year: 2023, month: 3, changeType: "\u8b66\u5458\u5957\u6539" },
    { label: "\u804c\u7ea7\u5957\u6539 00919-00036 2024-10", personCode: "00919-00036", orgCode: "00919", year: 2024, month: 10, changeType: "\u804c\u7ea7\u5957\u6539" },
    { label: "\u804c\u7ea7\u664b\u5347 025-00019 2024-12", personCode: "025-00019", orgCode: "025", year: 2024, month: 12, changeType: "\u804c\u7ea7\u664b\u5347" },
    { label: "\u6cd5\u68c0\u5957\u6539 044-00318 2023-09", personCode: "044-00318", orgCode: "044", year: 2023, month: 9, changeType: "\u6cd5\u68c0\u5957\u6539" },
    { label: "\u6cd5\u5b98\u7b49\u7ea7 044-00329 2006-12", personCode: "044-00329", orgCode: "044", year: 2006, month: 12, changeType: "\u6cd5\u5b98\u7b49\u7ea7" },
    { label: "2006\u5957\u6539 001-00055 2006-07", personCode: "001-00055", orgCode: "001", year: 2006, month: 7, changeType: "2006\u5957\u6539" },
    { label: "\u964d\u8d44\u5904\u5206 001-00291 2025-01", personCode: "001-00291", orgCode: "001", year: 2025, month: 1, changeType: "\u964d\u8d44\u5904\u5206" },
    { label: "\u5956\u52b1\u664b\u5347 00806-00089 2024-10", personCode: "00806-00089", orgCode: "00806", year: 2024, month: 10, changeType: "\u5956\u52b1\u664b\u5347" },
    { label: "\u5176\u5b83\u60c5\u51b5 02406-00058 2023-01", personCode: "02406-00058", orgCode: "02406", year: 2023, month: 1, changeType: "\u5176\u5b83\u60c5\u51b5" }
];

const state = {
    activeView: "workbench",
    activeMenuCode: "WORKBENCH",
    menus: [],
    workbench: null,
    workbenchTodoLoaded: 0,
    workbenchDoneLoaded: 0,
    workbenchRequestId: 0,
    historyPlanSummary: { total: 0, prepared: 0, executable: 0, blocked: 0, executed: 0, rolledBack: 0, issues: 0 },
    historyPlanMismatchField: "",
    historyPlanRetestStatus: "",
    historyPlanLocate: null,
    historyPlanQueueFilter: null,
    historyPlanSelected: new Map(),
    historyPlanCurrentItems: [],
    maintenanceReturn: null,
    currentUsername: "",
    orgs: [],
    selectedOrgCode: "",
    people: [],
    selectedPersonCode: "",
    selectedHistoryId: "",
    salaryHistoryRecords: [],
    configCategory: "01",
    configDwsx: "01",
    configMode: "effective",
    configIssues: [],
    editingConfigCode: "",
    auditFilters: {
        module: "",
        operator: "",
        targetCode: "",
        start: "",
        end: "",
        limit: 100
    },
    page: 1,
    size: 20,
    keyword: ""
};

const HISTORY_PLAN_QUEUE_STORAGE_KEY = "rsgzgl.historyPlanQueue.v1";
const HISTORY_PLAN_QUEUE_STATE_KEY = "history-plan-queue";
let historyPlanQueuePersistTimer = null;

function historyPlanQueuePayload() {
    return {
        queueFilter: state.historyPlanQueueFilter,
        selected: Array.from(state.historyPlanSelected.values())
    };
}

function historyPlanQueuePayloadEmpty(payload = historyPlanQueuePayload()) {
    return !payload.queueFilter && !payload.selected?.length;
}

function applyHistoryPlanQueuePayload(payload = {}) {
    const caseNos = Array.isArray(payload?.queueFilter?.caseNos)
        ? payload.queueFilter.caseNos.filter(Boolean)
        : [];
    state.historyPlanQueueFilter = caseNos.length
        ? { ...payload.queueFilter, caseNos, autoSelect: Boolean(payload.queueFilter?.autoSelect) }
        : null;
    const selected = Array.isArray(payload?.selected) ? payload.selected : [];
    state.historyPlanSelected = new Map(selected
        .filter((item) => item?.caseNo)
        .map((item) => [item.caseNo, {
            caseNo: item.caseNo,
            personCode: item.personCode || "",
            actionCode: item.actionCode || ""
        }]));
}

function persistHistoryPlanQueueState() {
    try {
        const payload = historyPlanQueuePayload();
        if (historyPlanQueuePayloadEmpty(payload)) {
            localStorage.removeItem(HISTORY_PLAN_QUEUE_STORAGE_KEY);
        } else {
            localStorage.setItem(HISTORY_PLAN_QUEUE_STORAGE_KEY, JSON.stringify(payload));
        }
    } catch (_) {
        // Local storage can be unavailable in restricted browser modes.
    }
    persistHistoryPlanQueueStateRemoteSoon();
}

function restoreHistoryPlanQueueState() {
    try {
        const raw = localStorage.getItem(HISTORY_PLAN_QUEUE_STORAGE_KEY);
        if (!raw) {
            return;
        }
        const payload = JSON.parse(raw);
        applyHistoryPlanQueuePayload(payload);
    } catch (_) {
        localStorage.removeItem(HISTORY_PLAN_QUEUE_STORAGE_KEY);
    }
}

restoreHistoryPlanQueueState();

function persistHistoryPlanQueueStateRemoteSoon() {
    if (!state.currentUsername) {
        return;
    }
    window.clearTimeout(historyPlanQueuePersistTimer);
    historyPlanQueuePersistTimer = window.setTimeout(() => {
        persistHistoryPlanQueueStateRemote();
    }, 250);
}

async function persistHistoryPlanQueueStateRemote() {
    if (!state.currentUsername) {
        return;
    }
    const payload = historyPlanQueuePayload();
    try {
        if (historyPlanQueuePayloadEmpty(payload)) {
            await Api.request(`/api/workbench/user-states/${encodeURIComponent(HISTORY_PLAN_QUEUE_STATE_KEY)}`, {
                method: "DELETE"
            });
            return;
        }
        await Api.request(`/api/workbench/user-states/${encodeURIComponent(HISTORY_PLAN_QUEUE_STATE_KEY)}`, {
            method: "PUT",
            body: JSON.stringify({ state: payload })
        });
    } catch (_) {
        // Local storage remains the fallback when the session or network is unavailable.
    }
}

async function restoreHistoryPlanQueueStateRemote() {
    if (!state.currentUsername) {
        return;
    }
    try {
        const response = await Api.request(`/api/workbench/user-states/${encodeURIComponent(HISTORY_PLAN_QUEUE_STATE_KEY)}`);
        const remoteState = response?.state || {};
        const remoteHasState = !historyPlanQueuePayloadEmpty(remoteState);
        if (remoteHasState) {
            applyHistoryPlanQueuePayload(remoteState);
            try {
                localStorage.setItem(HISTORY_PLAN_QUEUE_STORAGE_KEY, JSON.stringify(historyPlanQueuePayload()));
            } catch (_) {
                // Ignore local fallback write failures.
            }
        } else if (!historyPlanQueuePayloadEmpty()) {
            await persistHistoryPlanQueueStateRemote();
        }
    } catch (_) {
        // Keep the local fallback state.
    }
}

const els = {
    loginView: document.querySelector("#loginView"),
    loginForm: document.querySelector("#loginForm"),
    loginUsername: document.querySelector("#loginUsername"),
    loginPassword: document.querySelector("#loginPassword"),
    loginMessage: document.querySelector("#loginMessage"),
    loginButton: document.querySelector("#loginButton"),
    appShell: document.querySelector("#appShell"),
    statusText: document.querySelector("#statusText"),
    currentUserName: document.querySelector("#currentUserName"),
    currentUserMeta: document.querySelector("#currentUserMeta"),
    changePasswordToggleButton: document.querySelector("#changePasswordToggleButton"),
    changePasswordForm: document.querySelector("#changePasswordForm"),
    oldPasswordInput: document.querySelector("#oldPasswordInput"),
    newPasswordInput: document.querySelector("#newPasswordInput"),
    cancelPasswordButton: document.querySelector("#cancelPasswordButton"),
    logoutButton: document.querySelector("#logoutButton"),
    menuTree: document.querySelector("#menuTree"),
    workbenchView: document.querySelector("#workbenchView"),
    salaryWorkspace: document.querySelector("#salaryWorkspace"),
    systemView: document.querySelector("#systemView"),
    systemViewTitle: document.querySelector("#systemViewTitle"),
    systemViewHint: document.querySelector("#systemViewHint"),
    systemContent: document.querySelector("#systemContent"),
    systemRefreshButton: document.querySelector("#systemRefreshButton"),
    workbenchRefreshButton: document.querySelector("#workbenchRefreshButton"),
    workbenchFilterForm: document.querySelector("#workbenchFilterForm"),
    workbenchKeywordInput: document.querySelector("#workbenchKeywordInput"),
    workbenchChangeTypeSelect: document.querySelector("#workbenchChangeTypeSelect"),
    workbenchCaseStatusSelect: document.querySelector("#workbenchCaseStatusSelect"),
    workbenchTrialStatusSelect: document.querySelector("#workbenchTrialStatusSelect"),
    workbenchReviewStatusSelect: document.querySelector("#workbenchReviewStatusSelect"),
    workbenchFilterSummary: document.querySelector("#workbenchFilterSummary"),
    refreshTodoCacheButton: document.querySelector("#refreshTodoCacheButton"),
    exportTodoButton: document.querySelector("#exportTodoButton"),
    exportDoneButton: document.querySelector("#exportDoneButton"),
    workbenchMetrics: document.querySelector("#workbenchMetrics"),
    todoWorkItems: document.querySelector("#todoWorkItems"),
    doneWorkItems: document.querySelector("#doneWorkItems"),
    historyWritePlans: document.querySelector("#historyWritePlans"),
    historyPlanSummary: document.querySelector("#historyPlanSummary"),
    historyReviewLedger: document.querySelector("#historyReviewLedger"),
    historyPlanStatusSelect: document.querySelector("#historyPlanStatusSelect"),
    historyPlanComparisonSelect: document.querySelector("#historyPlanComparisonSelect"),
    historyPlanReviewSelect: document.querySelector("#historyPlanReviewSelect"),
    historyPlanRetestSelect: document.querySelector("#historyPlanRetestSelect"),
    historyPlanMaintenanceSelect: document.querySelector("#historyPlanMaintenanceSelect"),
    historyPlanPrioritySelect: document.querySelector("#historyPlanPrioritySelect"),
    historyPlanActionSelect: document.querySelector("#historyPlanActionSelect"),
    historyPlanRefreshButton: document.querySelector("#historyPlanRefreshButton"),
    historyPlanClearFiltersButton: document.querySelector("#historyPlanClearFiltersButton"),
    historyPlanBatchPreviewButton: document.querySelector("#historyPlanBatchPreviewButton"),
    historyPlanBatchRetestButton: document.querySelector("#historyPlanBatchRetestButton"),
    historyPlanBatchRetestApproveButton: document.querySelector("#historyPlanBatchRetestApproveButton"),
    historyPlanBatchExecuteButton: document.querySelector("#historyPlanBatchExecuteButton"),
    historyPlanBatchRollbackButton: document.querySelector("#historyPlanBatchRollbackButton"),
    historyPlanExportButton: document.querySelector("#historyPlanExportButton"),
    loadMoreTodoButton: document.querySelector("#loadMoreTodoButton"),
    loadMoreDoneButton: document.querySelector("#loadMoreDoneButton"),
    todoCount: document.querySelector("#todoCount"),
    doneCount: document.querySelector("#doneCount"),
    refreshButton: document.querySelector("#refreshButton"),
    batchYearInput: document.querySelector("#batchYearInput"),
    batchMonthInput: document.querySelector("#batchMonthInput"),
    batchLimitInput: document.querySelector("#batchLimitInput"),
    batchChangeTypeInput: document.querySelector("#batchChangeTypeInput"),
    acceptanceSampleSelect: document.querySelector("#acceptanceSampleSelect"),
    loadAcceptanceSampleButton: document.querySelector("#loadAcceptanceSampleButton"),
    batchReconcileButton: document.querySelector("#batchReconcileButton"),
    normalGradeBatchButton: document.querySelector("#normalGradeBatchButton"),
    exportBatchReconcileButton: document.querySelector("#exportBatchReconcileButton"),
    exportNormalGradeButton: document.querySelector("#exportNormalGradeButton"),
    orgTree: document.querySelector("#orgTree"),
    orgCount: document.querySelector("#orgCount"),
    peopleList: document.querySelector("#peopleList"),
    peopleTotal: document.querySelector("#peopleTotal"),
    pageText: document.querySelector("#pageText"),
    prevPageButton: document.querySelector("#prevPageButton"),
    nextPageButton: document.querySelector("#nextPageButton"),
    searchForm: document.querySelector("#searchForm"),
    keywordInput: document.querySelector("#keywordInput"),
    emptyState: document.querySelector("#emptyState"),
    detailContent: document.querySelector("#detailContent"),
    maintenanceReturnBar: document.querySelector("#maintenanceReturnBar"),
    personName: document.querySelector("#personName"),
    personMeta: document.querySelector("#personMeta"),
    profileGrid: document.querySelector("#profileGrid"),
    baseStatusSummary: document.querySelector("#baseStatusSummary"),
    personBaseInfoForm: document.querySelector("#personBaseInfoForm"),
    basePersonCategoryInput: document.querySelector("#basePersonCategoryInput"),
    baseOrganizationTypeInput: document.querySelector("#baseOrganizationTypeInput"),
    basePostCategoryInput: document.querySelector("#basePostCategoryInput"),
    baseWorkStartInput: document.querySelector("#baseWorkStartInput"),
    baseJoinOrgInput: document.querySelector("#baseJoinOrgInput"),
    baseTeacherNurseStartInput: document.querySelector("#baseTeacherNurseStartInput"),
    baseTeacherNurseFixedInput: document.querySelector("#baseTeacherNurseFixedInput"),
    baseEducationCodeInput: document.querySelector("#baseEducationCodeInput"),
    baseEducationInput: document.querySelector("#baseEducationInput"),
    baseRankCodeInput: document.querySelector("#baseRankCodeInput"),
    baseCurrentPostInput: document.querySelector("#baseCurrentPostInput"),
    basePostLevelInput: document.querySelector("#basePostLevelInput"),
    basePostStartInput: document.querySelector("#basePostStartInput"),
    baseInfoSummaryInput: document.querySelector("#baseInfoSummaryInput"),
    saveBaseInfoButton: document.querySelector("#saveBaseInfoButton"),
    baseChangeForm: document.querySelector("#baseChangeForm"),
    baseChangeTypeInput: document.querySelector("#baseChangeTypeInput"),
    baseChangeYearInput: document.querySelector("#baseChangeYearInput"),
    baseChangeMonthInput: document.querySelector("#baseChangeMonthInput"),
    baseChangeSourceIdInput: document.querySelector("#baseChangeSourceIdInput"),
    baseChangeSummaryInput: document.querySelector("#baseChangeSummaryInput"),
    saveBaseChangeButton: document.querySelector("#saveBaseChangeButton"),
    baseChangeList: document.querySelector("#baseChangeList"),
    newPersonPostButton: document.querySelector("#newPersonPostButton"),
    personPostForm: document.querySelector("#personPostForm"),
    personPostIdInput: document.querySelector("#personPostIdInput"),
    personPostCodeInput: document.querySelector("#personPostCodeInput"),
    personPostNameInput: document.querySelector("#personPostNameInput"),
    personPostLevelInput: document.querySelector("#personPostLevelInput"),
    personPostStartInput: document.querySelector("#personPostStartInput"),
    personPostRankInput: document.querySelector("#personPostRankInput"),
    personPostCurrentCodeInput: document.querySelector("#personPostCurrentCodeInput"),
    personPostExcludedYearsInput: document.querySelector("#personPostExcludedYearsInput"),
    personPostCurrentFlagInput: document.querySelector("#personPostCurrentFlagInput"),
    personPostPayrollFlagInput: document.querySelector("#personPostPayrollFlagInput"),
    personPostSummaryInput: document.querySelector("#personPostSummaryInput"),
    savePersonPostButton: document.querySelector("#savePersonPostButton"),
    cancelPersonPostEditButton: document.querySelector("#cancelPersonPostEditButton"),
    personPostList: document.querySelector("#personPostList"),
    newEducationButton: document.querySelector("#newEducationButton"),
    educationForm: document.querySelector("#educationForm"),
    educationIdInput: document.querySelector("#educationIdInput"),
    educationCodeInput: document.querySelector("#educationCodeInput"),
    educationNameInput: document.querySelector("#educationNameInput"),
    educationSchoolInput: document.querySelector("#educationSchoolInput"),
    educationEnrollInput: document.querySelector("#educationEnrollInput"),
    educationGraduationInput: document.querySelector("#educationGraduationInput"),
    educationYearsInput: document.querySelector("#educationYearsInput"),
    educationTypeInput: document.querySelector("#educationTypeInput"),
    educationNoteInput: document.querySelector("#educationNoteInput"),
    educationSummaryInput: document.querySelector("#educationSummaryInput"),
    saveEducationButton: document.querySelector("#saveEducationButton"),
    cancelEducationEditButton: document.querySelector("#cancelEducationEditButton"),
    educationList: document.querySelector("#educationList"),
    newAssessmentButton: document.querySelector("#newAssessmentButton"),
    assessmentForm: document.querySelector("#assessmentForm"),
    assessmentIdInput: document.querySelector("#assessmentIdInput"),
    assessmentYearInput: document.querySelector("#assessmentYearInput"),
    assessmentResultInput: document.querySelector("#assessmentResultInput"),
    assessmentSummaryInput: document.querySelector("#assessmentSummaryInput"),
    saveAssessmentButton: document.querySelector("#saveAssessmentButton"),
    cancelAssessmentEditButton: document.querySelector("#cancelAssessmentEditButton"),
    assessmentList: document.querySelector("#assessmentList"),
    salaryHistory: document.querySelector("#salaryHistory"),
    salaryTitle: document.querySelector("#salaryTitle"),
    salaryTotal: document.querySelector("#salaryTotal"),
    salaryDetails: document.querySelector("#salaryDetails"),
    trialCalcButton: document.querySelector("#trialCalcButton"),
    reconcileButton: document.querySelector("#reconcileButton"),
    timelineButton: document.querySelector("#timelineButton"),
    generatedTimelineButton: document.querySelector("#generatedTimelineButton"),
    civilConfigButton: document.querySelector("#civilConfigButton"),
    institutionConfigButton: document.querySelector("#institutionConfigButton"),
    allConfigButton: document.querySelector("#allConfigButton"),
    configYearInput: document.querySelector("#configYearInput"),
    fieldConfigList: document.querySelector("#fieldConfigList"),
    fieldConfigEditor: document.querySelector("#fieldConfigEditor"),
    configEditorTitle: document.querySelector("#configEditorTitle"),
    cancelConfigEditButton: document.querySelector("#cancelConfigEditButton"),
    saveConfigButton: document.querySelector("#saveConfigButton"),
    configFieldCapInput: document.querySelector("#configFieldCapInput"),
    configFieldCapsInput: document.querySelector("#configFieldCapsInput"),
    configCategoryInput: document.querySelector("#configCategoryInput"),
    configCategory6Input: document.querySelector("#configCategory6Input"),
    configActiveInput: document.querySelector("#configActiveInput"),
    configActive2006Input: document.querySelector("#configActive2006Input"),
    configSequenceInput: document.querySelector("#configSequenceInput"),
    configAuditList: document.querySelector("#configAuditList")
};

const Permissions = {
    has(code) {
        return SystemShell.hasMenuCode(code);
    },
    show(element, allowed) {
        if (!element) {
            return;
        }
        element.classList.toggle("hidden", !allowed);
    },
    applySalary() {
        const canTrial = Permissions.has("SALARY_TRIAL");
        const canReconcile = Permissions.has("SALARY_RECONCILE");
        const canExport = Permissions.has("SALARY_EXPORT");
        const canConfig = Permissions.has("SALARY_CONFIG");

        Permissions.show(els.normalGradeBatchButton, canTrial);
        Permissions.show(els.trialCalcButton, canTrial);
        Permissions.show(els.batchReconcileButton, canReconcile);
        Permissions.show(els.reconcileButton, canReconcile);
        Permissions.show(els.exportBatchReconcileButton, canExport);
        Permissions.show(els.exportNormalGradeButton, canExport);

        const configBand = els.fieldConfigList?.closest(".config-band");
        Permissions.show(configBand, canConfig);
        if (!canConfig) {
            ConfigPanel.hideEditor();
            els.fieldConfigList.innerHTML = "";
        }
    },
    applyWorkbench() {
        const canTodo = Permissions.has("SALARY_TODO") || Permissions.has("APPLICATION_TODO");
        const canDone = Permissions.has("SALARY_DONE") || Permissions.has("APPLICATION_DONE");
        const canExport = Permissions.has("SALARY_EXPORT");

        Permissions.show(els.todoWorkItems?.closest(".workbench-panel"), canTodo);
        Permissions.show(els.doneWorkItems?.closest(".workbench-panel"), canDone);
        Permissions.show(els.historyWritePlans?.closest(".workbench-panel"), Permissions.has("SALARY_DONE"));
        Permissions.show(els.refreshTodoCacheButton, Permissions.has("SALARY_TODO"));
        Permissions.show(els.exportTodoButton, canExport && canTodo);
        Permissions.show(els.exportDoneButton, canExport && canDone);
        Permissions.show(els.historyPlanBatchPreviewButton, Permissions.has("SALARY_DONE"));
        Permissions.show(els.historyPlanBatchRetestButton, Permissions.has("SALARY_DONE"));
        Permissions.show(els.historyPlanBatchRetestApproveButton, Permissions.has("SALARY_DONE"));
        Permissions.show(els.historyPlanBatchExecuteButton, Permissions.has("SALARY_DONE"));
        Permissions.show(els.historyPlanBatchRollbackButton, Permissions.has("SALARY_DONE"));
        Permissions.show(els.historyPlanExportButton, canExport && Permissions.has("SALARY_DONE"));
        WorkbenchPanel.updateHistoryPlanActionState();
        if (!canTodo) {
            state.workbenchTodoLoaded = 0;
            els.todoCount.textContent = "0";
            els.todoWorkItems.innerHTML = "";
        }
        if (!canDone) {
            state.workbenchDoneLoaded = 0;
            els.doneCount.textContent = "0";
            els.doneWorkItems.innerHTML = "";
        }
        if (!Permissions.has("SALARY_DONE") && els.historyWritePlans) {
            els.historyWritePlans.innerHTML = "";
        }
    },
    guard(code) {
        if (Permissions.has(code)) {
            return true;
        }
        setStatus(TEXT.menuPlaceholder);
        return false;
    }
};

const Api = {
    async request(path, options = {}) {
        const response = await fetch(path, {
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
            ...options
        });
        const payload = await response.json();
        if (!response.ok || !payload.success) {
            throw new Error(payload.message || `${TEXT.requestFail}: ${response.status}`);
        }
        return payload.data;
    }
};

const AuthPanel = {
    showLogin(message = TEXT.loginRequired) {
        els.loginMessage.textContent = message;
        els.loginView.classList.remove("hidden");
        els.appShell.classList.add("hidden");
        els.loginPassword.value = "";
        els.loginUsername.focus();
    },
    showApp() {
        els.loginView.classList.add("hidden");
        els.appShell.classList.remove("hidden");
    },
    async boot() {
        try {
            await SystemShell.loadCurrentUser();
            AuthPanel.showApp();
            await restoreHistoryPlanQueueStateRemote();
            await SystemShell.loadMenus();
            await SystemShell.selectInitialView();
            setStatus(TEXT.serviceOk);
        } catch (error) {
            AuthPanel.showLogin(error.message || TEXT.loginRequired);
        }
    },
    async login(event) {
        event.preventDefault();
        try {
            els.loginButton.disabled = true;
            els.loginMessage.textContent = TEXT.loggingIn;
            const username = els.loginUsername.value.trim();
            const password = els.loginPassword.value;
            await Api.request("/api/auth/login", {
                method: "POST",
                body: JSON.stringify({ username, password })
            });
            await AuthPanel.boot();
        } catch (error) {
            els.loginMessage.textContent = error.message || TEXT.loginFailed;
        } finally {
            els.loginButton.disabled = false;
        }
    },
    async logout() {
        await Api.request("/api/auth/logout", { method: "POST", body: "{}" });
        state.currentUsername = "";
        state.historyPlanQueueFilter = null;
        state.historyPlanSelected.clear();
        persistHistoryPlanQueueState();
        AuthPanel.showLogin(TEXT.loggedOut);
    },
    togglePasswordForm(show) {
        els.changePasswordForm.classList.toggle("hidden", !show);
        if (show) {
            els.oldPasswordInput.value = "";
            els.newPasswordInput.value = "";
            els.oldPasswordInput.focus();
        }
    },
    async changePassword(event) {
        event.preventDefault();
        setStatus(TEXT.savingSystem);
        await Api.request("/api/auth/change-password", {
            method: "POST",
            body: JSON.stringify({
                oldPassword: els.oldPasswordInput.value,
                newPassword: els.newPasswordInput.value
            })
        });
        AuthPanel.togglePasswordForm(false);
        setStatus(TEXT.passwordChanged);
    }
};

const Format = {
    amount(value) {
        const number = Number(value || 0);
        return number.toLocaleString("zh-CN", { minimumFractionDigits: 0, maximumFractionDigits: 2 });
    },
    optionalAmount(value) {
        if (value === null || value === undefined || value === "") {
            return "-";
        }
        return Format.amount(value);
    },
    html(value) {
        return String(value ?? "").replace(/[&<>"']/g, (char) => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            "\"": "&quot;",
            "'": "&#39;"
        }[char]));
    },
    csv(value) {
        const text = String(value ?? "");
        return /[",\r\n]/.test(text) ? `"${text.replace(/"/g, "\"\"")}"` : text;
    },
    text(template, values) {
        return template.replace(/\{(\w+)}/g, (_, key) => values[key] ?? "");
    },
    statusText(status) {
        return {
            MATCH: "\u5339\u914d",
            DIFFERENT: "\u6709\u5dee\u5f02",
            ERROR: "\u8bd5\u7b97\u5f02\u5e38",
            SKIPPED: "\u672a\u8bd5\u7b97"
        }[status] || status || "-"
    },
    trialStatusClass(status) {
        return {
            MATCH: "match",
            DIFFERENT: "different",
            ERROR: "error",
            SKIPPED: "skipped"
        }[status] || ""
    },
    reviewStatusText(status) {
        return {
            PENDING: "\u5f85\u590d\u6838",
            REVIEWED: "\u5df2\u590d\u6838"
        }[status] || status || "-"
    },
    reviewStatusClass(status) {
        return {
            PENDING: "pending",
            REVIEWED: "reviewed"
        }[status] || ""
    },
    historyWriteReviewCategoryText(category) {
        return {
            BASE_MISSING: "\u57fa\u7840\u4fe1\u606f\u7f3a\u5931",
            BASE_CHANGED: "\u57fa\u7840\u4fe1\u606f\u5df2\u53d8\u66f4",
            POLICY_DIFF: "\u653f\u7b56\u53d6\u503c\u5dee\u5f02",
            MANUAL_INPUT: "\u624b\u5de5\u5f55\u5165",
            HISTORY_SPECIAL: "\u5386\u53f2\u7279\u6b8a\u5904\u7406",
            OTHER: "\u5176\u4ed6"
        }[category] || category || "-"
    },
    historyWriteReviewSourceText(source) {
        return {
            SUGGESTED: "\u5efa\u8bae\u5e26\u5165",
            RETEST: "\u590d\u6d4b\u901a\u8fc7",
            MANUAL: "\u624b\u5de5\u6838\u67e5"
        }[source] || source || "-"
    },
    historyWriteRetestStatusText(status) {
        return {
            NOT_RETESTED: "\u672a\u590d\u6d4b",
            RETEST_MISMATCHED: "\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02",
            RETEST_MATCHED: "\u590d\u6d4b\u4e00\u81f4"
        }[status] || status || "-"
    },
    historyWriteRetestStatusClass(status) {
        return {
            NOT_RETESTED: "pending",
            RETEST_MISMATCHED: "error",
            RETEST_MATCHED: "reviewed"
        }[status] || "pending"
    },
    historyWritePriorityText(priority) {
        return {
            HIGH: "\u9ad8",
            MEDIUM: "\u4e2d",
            LOW: "\u4f4e",
            DONE: "\u5df2\u5b8c\u6210"
        }[priority] || priority || "-"
    },
    historyWritePriorityClass(priority) {
        return {
            HIGH: "error",
            MEDIUM: "different",
            LOW: "pending",
            DONE: "reviewed"
        }[priority] || "pending"
    },
    historyWriteActionText(actionCode, fallback = "") {
        return {
            RETEST_FIRST: "\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b",
            MAINTAIN_AND_RETEST: "\u7ef4\u62a4\u540e\u590d\u6d4b",
            APPROVE_RETEST: "\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7",
            REVIEWED: "\u5df2\u6838\u67e5",
            NOT_REQUIRED: "\u65e0\u9700\u6838\u67e5"
        }[actionCode] || fallback || actionCode || "-"
    },
    historyWriteMaintenanceText(target) {
        return {
            base: "\u57fa\u672c\u4fe1\u606f",
            post: "\u4efb\u804c",
            education: "\u5b66\u5386",
            assessment: "\u8003\u6838",
            standard: "\u6807\u51c6/\u6d25\u8865\u8d34",
            other: "\u5176\u4ed6"
        }[target] || target || "-"
    },
    historyWriteStatusText(status) {
        return {
            PREPARED: "\u5f85\u5199\u5165",
            EXECUTED: "\u5df2\u5199\u5165",
            ROLLED_BACK: "\u5df2\u64a4\u9500",
            BLOCKED: "\u5df2\u963b\u65ad",
            FAILED: "\u5199\u5165\u5931\u8d25",
            SUCCESS: "\u6210\u529f"
        }[status] || status || "-"
    },
    historyWriteComparisonText(status) {
        return {
            NOT_WRITTEN: "\u672a\u5199\u5165",
            MATCHED: "\u5df2\u4e00\u81f4",
            MISMATCHED: "\u5199\u5165\u540e\u4e0d\u4e00\u81f4",
            ROLLED_BACK: "\u5df2\u56de\u6eda",
            BLOCKED: "\u5df2\u963b\u65ad",
            UNKNOWN: "\u672a\u77e5"
        }[status] || status || "-"
    },
    historyWriteComparisonClass(status) {
        return {
            NOT_WRITTEN: "pending",
            MATCHED: "reviewed",
            MISMATCHED: "error",
            ROLLED_BACK: "skipped",
            BLOCKED: "error",
            UNKNOWN: "different"
        }[status] || "pending"
    },
    auditActionText(action) {
        return {
            "salary-case-done": "\u529e\u7ed3\u5de5\u8d44\u4e1a\u52a1",
            "salary-case-cancel": "\u64a4\u56de\u5de5\u8d44\u4e1a\u52a1",
            "salary-case-review": "\u590d\u6838\u5de5\u8d44\u4e1a\u52a1",
            "history-write-execute": "\u5199\u5165\u5386\u53f2",
            "history-write-rollback": "\u64a4\u9500\u5199\u5165",
            "history-write-batch-execute": "\u6279\u91cf\u5199\u5165\u5386\u53f2",
            "history-write-batch-rollback": "\u6279\u91cf\u64a4\u9500\u5199\u5165",
            "history-write-comparison-review": "\u6838\u67e5\u5199\u5165\u5dee\u5f02",
            "history-write-comparison-retest": "\u590d\u6d4b\u5199\u5165\u5dee\u5f02",
            "history-write-comparison-retest-approve": "\u590d\u6d4b\u901a\u8fc7"
        }[action] || action || "-"
    },
    businessStatusText(status) {
        return {
            DONE: "\u5df2\u529e",
            CANCELLED: "\u5df2\u64a4\u56de",
            TODO: "\u5f85\u529e",
            PREVIEW: "\u9884\u89c8"
        }[status] || status || "-"
    }
};

function setStatus(text) {
    els.statusText.textContent = text;
}

function batchChangeType() {
    return (els.batchChangeTypeInput.value || "").trim();
}

function baseChangeTypeLabel(value) {
    return {
        dryzwbh: "\u4efb\u804c\u4fe1\u606f",
        dxl: "\u5b66\u5386\u4fe1\u606f",
        dndkh: "\u5e74\u5ea6\u8003\u6838",
        dryjbxx: "\u4eba\u5458\u57fa\u672c\u4fe1\u606f"
    }[value] || value || "-";
}

const WorkbenchPanel = {
    riskMetricCodes: ["SALARY_REVIEW_PENDING", "SALARY_TRIAL_DIFFERENT", "SALARY_TRIAL_ERROR"],
    historyPlanMetricCodes: ["HISTORY_PLAN_PREPARED", "HISTORY_PLAN_EXECUTED", "HISTORY_PLAN_ROLLED_BACK", "HISTORY_PLAN_BLOCKED", "HISTORY_PLAN_REVIEW_PENDING"],
    metricCountText(metric) {
        return Number(metric.count) < 0 ? "..." : metric.count;
    },
    renderMetrics(metrics) {
        if (!metrics.length) {
            els.workbenchMetrics.innerHTML = `<div class="loading">${TEXT.noWorkItems}</div>`;
            return;
        }
        const canExport = Permissions.has("SALARY_EXPORT");
        els.workbenchMetrics.innerHTML = metrics.map((metric) => `
            <div class="metric-card ${WorkbenchPanel.riskMetricCodes.includes(metric.code) ? "risk" : ""} ${String(metric.hint || "").includes("\u9700\u5237\u65b0") ? "dirty" : ""}" data-metric-code="${Format.html(metric.code || "")}">
                <span>${Format.html(metric.title)}</span>
                <strong>${Format.html(WorkbenchPanel.metricCountText(metric))}</strong>
                <small>${Format.html(metric.hint || "")}</small>
                ${WorkbenchPanel.riskMetricCodes.includes(metric.code) || WorkbenchPanel.historyPlanMetricCodes.includes(metric.code) ? `
                    <div class="metric-actions">
                        <button type="button" data-metric-view="${Format.html(metric.code)}">\u67e5\u770b</button>
                        ${canExport && WorkbenchPanel.riskMetricCodes.includes(metric.code) ? `<button type="button" data-metric-export="${Format.html(metric.code)}">\u5bfc\u51fa</button>` : ""}
                    </div>
                ` : ""}
            </div>
        `).join("");
    },
    updateMetric(metric) {
        const metrics = [...(state.workbench?.metrics || [])];
        const index = metrics.findIndex((item) => item.code === metric.code);
        if (index >= 0) {
            metrics[index] = metric;
        } else {
            metrics.push(metric);
        }
        state.workbench = { ...(state.workbench || {}), metrics };
        WorkbenchPanel.renderMetrics(metrics);
    },
    applyRiskMetric(metricCode) {
        if (metricCode === "SALARY_REVIEW_PENDING") {
            els.workbenchKeywordInput.value = "";
            els.workbenchChangeTypeSelect.value = "";
            els.workbenchCaseStatusSelect.value = "DONE";
            els.workbenchTrialStatusSelect.value = "";
            els.workbenchReviewStatusSelect.value = "PENDING";
            return true;
        }
        if (metricCode === "SALARY_TRIAL_DIFFERENT" || metricCode === "SALARY_TRIAL_ERROR") {
            els.workbenchKeywordInput.value = "";
            els.workbenchChangeTypeSelect.value = "";
            els.workbenchCaseStatusSelect.value = "DONE";
            els.workbenchTrialStatusSelect.value = metricCode === "SALARY_TRIAL_DIFFERENT" ? "DIFFERENT" : "ERROR";
            els.workbenchReviewStatusSelect.value = "";
            return true;
        }
        return false;
    },
    applyHistoryPlanMetric(metricCode) {
        if (metricCode === "HISTORY_PLAN_REVIEW_PENDING") {
            if (els.historyPlanStatusSelect) {
                els.historyPlanStatusSelect.value = "EXECUTED";
            }
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
            if (els.historyPlanReviewSelect) {
                els.historyPlanReviewSelect.value = "PENDING";
            }
            return true;
        }
        const status = {
            HISTORY_PLAN_PREPARED: "PREPARED",
            HISTORY_PLAN_EXECUTED: "EXECUTED",
            HISTORY_PLAN_ROLLED_BACK: "ROLLED_BACK",
            HISTORY_PLAN_BLOCKED: "BLOCKED"
        }[metricCode];
        if (!status || !els.historyPlanStatusSelect) {
            return false;
        }
        els.historyPlanStatusSelect.value = status;
        if (els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "";
        }
        if (els.historyPlanReviewSelect) {
            els.historyPlanReviewSelect.value = "";
        }
        return true;
    },
    async openMetric(metricCode) {
        if (WorkbenchPanel.applyRiskMetric(metricCode)) {
            state.workbenchDoneLoaded = 0;
            setStatus(TEXT.loadingWorkbench);
            await WorkbenchPanel.loadPage("DONE", true);
            setStatus(TEXT.workbenchReady);
            return;
        }
        if (WorkbenchPanel.applyHistoryPlanMetric(metricCode)) {
            setStatus(TEXT.loadingWorkbench);
            await WorkbenchPanel.loadHistoryWritePlans();
            setStatus(TEXT.workbenchReady);
        }
    },
    exportMetric(metricCode) {
        if (WorkbenchPanel.applyRiskMetric(metricCode)) {
            WorkbenchPanel.exportItems("DONE");
        }
    },
    async refreshTodoCache() {
        if (!Permissions.has("SALARY_TODO")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        els.refreshTodoCacheButton.disabled = true;
        setStatus(TEXT.refreshingTodoCache);
        try {
            const metric = await Api.request("/api/workbench/salary-todo-cache/refresh", {
                method: "POST"
            });
            WorkbenchPanel.updateMetric(metric);
            state.workbenchTodoLoaded = 0;
            await WorkbenchPanel.loadPage("TODO", true);
            setStatus(TEXT.todoCacheRefreshed);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.refreshTodoCacheButton.disabled = false;
        }
    },
    itemHtml(item) {
        const dateText = item.year ? `${item.year}-${String(item.month || 1).padStart(2, "0")}` : "-";
        const personText = [item.personName, item.personCode].filter(Boolean).join(" ");
        const canComplete = item.status === "TODO" && item.source === "SALARY_EVENT" && Permissions.has("SALARY_TODO") && Permissions.has("SALARY_DONE");
        const statusText = item.status === "CANCELLED" ? Format.businessStatusText(item.status) : "";
        const trialText = item.trialStatus ? Format.statusText(item.trialStatus) : "";
        const trialClass = Format.trialStatusClass(item.trialStatus);
        const reviewText = item.reviewStatus ? Format.reviewStatusText(item.reviewStatus) : "";
        const reviewClass = Format.reviewStatusClass(item.reviewStatus);
        return `
            <div class="work-item"
                data-work-id="${Format.html(item.id)}"
                data-source="${Format.html(item.source || "")}"
                data-status="${Format.html(item.status || "")}"
                data-person-code="${Format.html(item.personCode || "")}"
                data-person-name="${Format.html(item.personName || "")}"
                data-org-code="${Format.html(item.orgCode || "")}"
                data-year="${Format.html(item.year || "")}"
                data-month="${Format.html(item.month || "")}"
                data-change-type="${Format.html(item.businessType || "")}"
                data-title="${Format.html(item.title || "")}"
                data-summary="${Format.html(item.summary || "")}">
                <button type="button" class="work-item-open" data-work-id="${Format.html(item.id)}">
                    <span class="work-item-main">
                        <strong>${Format.html(item.title || item.businessType || "-")}</strong>
                        <span>${Format.html(dateText)}</span>
                    </span>
                    <span class="work-item-meta">${Format.html(personText || item.orgCode || "-")}</span>
                    <span class="work-item-summary">${Format.html(item.summary || "")}</span>
                    ${statusText ? `<span class="work-item-state">${statusText}</span>` : ""}
                    ${trialText ? `<span class="work-item-trial ${Format.html(trialClass)}">${Format.html(trialText)}</span>` : ""}
                    ${reviewText ? `<span class="work-item-review ${Format.html(reviewClass)}">${Format.html(reviewText)}</span>` : ""}
                </button>
                ${canComplete ? `<button type="button" class="work-item-complete" data-complete-work-id="${Format.html(item.id)}">\u529e\u7406</button>` : ""}
            </div>
        `;
    },
    renderItems(items, container) {
        if (!items.length) {
            container.innerHTML = `<div class="loading">${TEXT.noWorkItems}</div>`;
            return;
        }
        container.innerHTML = items.map((item) => WorkbenchPanel.itemHtml(item)).join("");
    },
    appendItems(items, container) {
        if (!items.length) {
            return;
        }
        container.insertAdjacentHTML("beforeend", items.map((item) => WorkbenchPanel.itemHtml(item)).join(""));
    },
    planStatusText(status) {
        const value = status || "";
        if (value === "PREPARED") {
            return "\u5f85\u6267\u884c";
        }
        if (value === "EXECUTED") {
            return "\u5df2\u5199\u5165";
        }
        if (value === "ROLLED_BACK") {
            return "\u5df2\u64a4\u9500";
        }
        if (value === "BLOCKED") {
            return "\u5df2\u963b\u65ad";
        }
        return value || "-";
    },
    historyWriteReviewSource(plan) {
        const reason = plan?.comparisonReviewReason || "";
        if (reason.startsWith("\u6309\u5efa\u8bae\u68c0\u67e5\u65b9\u5411\u767b\u8bb0")) {
            return "SUGGESTED";
        }
        if (reason.includes("\u590d\u6d4b\u5df2\u4e00\u81f4")) {
            return "RETEST";
        }
        return "MANUAL";
    },
    historyWriteWorkflow(plan) {
        if (plan?.processingPriority || plan?.nextAction || plan?.nextActionCode) {
            return { priority: plan.processingPriority || "DONE", actionCode: plan.nextActionCode || "", action: plan.nextAction || Format.historyWriteActionText(plan.nextActionCode) };
        }
        const comparisonStatus = plan?.comparisonStatus || "";
        const reviewStatus = plan?.comparisonReviewStatus || "";
        const retestStatus = plan?.comparisonRetestStatus || "NOT_RETESTED";
        const mismatchCount = Number(plan?.comparisonMismatchCount || 0);
        if (comparisonStatus !== "MISMATCHED") {
            return { priority: "DONE", actionCode: "NOT_REQUIRED", action: "\u65e0\u9700\u6838\u67e5" };
        }
        if (reviewStatus === "REVIEWED") {
            return { priority: "DONE", actionCode: "REVIEWED", action: "\u5df2\u6838\u67e5" };
        }
        if (retestStatus === "RETEST_MISMATCHED") {
            return { priority: "HIGH", actionCode: "MAINTAIN_AND_RETEST", action: "\u68c0\u67e5\u57fa\u7840/\u4efb\u804c/\u5b66\u5386/\u8003\u6838\u540e\u590d\u6d4b" };
        }
        if (retestStatus === "RETEST_MATCHED") {
            return { priority: "MEDIUM", actionCode: "APPROVE_RETEST", action: "\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7" };
        }
        if (mismatchCount >= 3) {
            return { priority: "HIGH", actionCode: "RETEST_FIRST", action: "\u5148\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b" };
        }
        return { priority: "MEDIUM", actionCode: "RETEST_FIRST", action: "\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b" };
    },
    historyPlanMaintenanceSuggestions(plan) {
        const raw = plan?.maintenanceSuggestionJson || "[]";
        try {
            const suggestions = JSON.parse(raw);
            return Array.isArray(suggestions) ? suggestions : [];
        } catch (_) {
            return [];
        }
    },
    historyPlanMaintenanceHtml(plan, retestStatus) {
        if (retestStatus !== "RETEST_MISMATCHED") {
            return "";
        }
        const suggestions = WorkbenchPanel.historyPlanMaintenanceSuggestions(plan);
        const maintenanceTargets = new Set(["base", "post", "education", "assessment"]);
        if (suggestions.length) {
            return `<div class="history-plan-maintenance suggested">
                <small>\u5efa\u8bae</small>
                ${suggestions.map((suggestion) => {
                    const fields = Array.isArray(suggestion.fields) ? suggestion.fields.join("\u3001") : "";
                    const title = `${suggestion.reason || ""}${fields ? `\uff1a${fields}` : ""}`;
                    if (maintenanceTargets.has(suggestion.target)) {
                        return `<button type="button" title="${Format.html(title)}" data-open-person-maintenance="${Format.html(suggestion.target)}" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}" data-maintenance-label="${Format.html(suggestion.label || "")}" data-maintenance-reason="${Format.html(suggestion.reason || "")}" data-maintenance-fields="${Format.html(fields)}">${Format.html(suggestion.label || "-")} ${Format.html(suggestion.count || 0)}</button>`;
                    }
                    return `<span title="${Format.html(title)}">${Format.html(suggestion.label || "-")} ${Format.html(suggestion.count || 0)}</span>`;
                }).join("")}
            </div>`;
        }
        return `<div class="history-plan-maintenance">
            <button type="button" data-open-person-maintenance="base" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u57fa\u672c</button>
            <button type="button" data-open-person-maintenance="post" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u4efb\u804c</button>
            <button type="button" data-open-person-maintenance="education" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u5b66\u5386</button>
            <button type="button" data-open-person-maintenance="assessment" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u8003\u6838</button>
        </div>`;
    },
    historyPlanRetestButtonHtml(plan, retestStatus) {
        if (plan.comparisonStatus !== "MISMATCHED" || plan.comparisonReviewStatus === "REVIEWED") {
            return "";
        }
        if (!["NOT_RETESTED", "RETEST_MISMATCHED"].includes(retestStatus)) {
            return "";
        }
        return `<button type="button" class="history-plan-retest" data-history-write-comparison-retest-case-no="${Format.html(plan.caseNo || "")}">\u590d\u6d4b</button>`;
    },
    historyPlanNextActionsHtml(plan, workflow, retestStatus) {
        if (plan.comparisonStatus !== "MISMATCHED" || plan.comparisonReviewStatus === "REVIEWED") {
            return "";
        }
        const actionCode = workflow?.actionCode || "";
        if (actionCode === "APPROVE_RETEST" || retestStatus === "RETEST_MATCHED") {
            return `<div class="history-plan-next-actions">
                <span>\u4e0b\u4e00\u6b65</span>
                <button type="button" class="history-plan-approve" data-history-write-retest-approve-case-no="${Format.html(plan.caseNo || "")}">\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7</button>
            </div>`;
        }
        if (actionCode === "MAINTAIN_AND_RETEST" || retestStatus === "RETEST_MISMATCHED") {
            return `<div class="history-plan-next-actions">
                <span>\u4e0b\u4e00\u6b65</span>
                <b>\u5148\u7ef4\u62a4\u57fa\u7840\u4fe1\u606f</b>
                <button type="button" class="history-plan-retest" data-history-write-comparison-retest-case-no="${Format.html(plan.caseNo || "")}">\u91cd\u65b0\u590d\u6d4b</button>
            </div>`;
        }
        return `<div class="history-plan-next-actions">
            <span>\u4e0b\u4e00\u6b65</span>
            <button type="button" class="history-plan-retest" data-history-write-comparison-retest-case-no="${Format.html(plan.caseNo || "")}">\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b</button>
        </div>`;
    },
    historyPlanSelectable(plan, workflow) {
        return plan.comparisonStatus === "MISMATCHED"
            && plan.comparisonReviewStatus !== "REVIEWED"
            && ["RETEST_FIRST", "MAINTAIN_AND_RETEST", "APPROVE_RETEST"].includes(workflow?.actionCode || "");
    },
    historyPlanSelectionSummaryHtml() {
        const selected = Array.from(state.historyPlanSelected.values());
        if (!selected.length) {
            return "";
        }
        const actionCodes = Array.from(new Set(selected.map((item) => item.actionCode || ""))).filter(Boolean);
        const sameAction = actionCodes.length === 1;
        const actionText = sameAction ? Format.historyWriteActionText(actionCodes[0]) : "\u591a\u79cd\u4e0b\u4e00\u6b65\u52a8\u4f5c";
        const actionCode = sameAction ? actionCodes[0] : "";
        const canRetest = ["RETEST_FIRST", "MAINTAIN_AND_RETEST"].includes(actionCode);
        const canApprove = actionCode === "APPROVE_RETEST";
        const maintenanceTargets = WorkbenchPanel.selectedHistoryPlanMaintenanceTargets();
        const queueFilter = state.historyPlanQueueFilter;
        return `<div class="history-plan-selection">
            <span>\u5df2\u9009 ${Format.html(selected.length)} \u6761 | ${Format.html(actionText)}</span>
            ${queueFilter ? `<small>\u6765\u81ea\u672c\u8f6e\u4ecd\u6709\u5dee\u5f02\u961f\u5217</small>` : ""}
            ${actionCode === "MAINTAIN_AND_RETEST" && maintenanceTargets.length ? `<small>\u7ef4\u62a4</small>${maintenanceTargets.map((target) => `<button type="button" data-history-plan-selection-maintenance="${Format.html(target)}">${Format.html(WorkbenchPanel.maintenanceTargetText(target))}</button>`).join("")}` : ""}
            ${canRetest ? `<button type="button" data-history-plan-selection-retest>\u590d\u6d4b\u9009\u4e2d\u9879</button>` : ""}
            ${canApprove ? `<button type="button" data-history-plan-selection-approve>\u6807\u8bb0\u9009\u4e2d\u901a\u8fc7</button>` : ""}
            ${sameAction ? `<button type="button" data-history-plan-selection-filter="${Format.html(actionCode)}">\u7b5b\u5230\u6b64\u52a8\u4f5c</button>` : `<small>\u8bf7\u9009\u62e9\u540c\u4e00\u7c7b\u4e0b\u4e00\u6b65\u52a8\u4f5c</small>`}
            <button type="button" data-history-plan-selection-clear>\u6e05\u9664\u9009\u62e9</button>
        </div>`;
    },
    historyPlanQueuePanelHtml(plans = []) {
        const queueFilter = state.historyPlanQueueFilter;
        if (!queueFilter?.caseNos?.length) {
            return "";
        }
        const items = Array.isArray(plans) ? plans : [];
        const selectedCount = state.historyPlanSelected.size;
        const maintainCount = items.filter((plan) => WorkbenchPanel.historyWriteWorkflow(plan).actionCode === "MAINTAIN_AND_RETEST").length;
        const retestCount = items.filter((plan) => WorkbenchPanel.historyWriteWorkflow(plan).actionCode === "RETEST_FIRST").length;
        const approveCount = items.filter((plan) => WorkbenchPanel.historyWriteWorkflow(plan).actionCode === "APPROVE_RETEST").length;
        const maintenanceTargets = WorkbenchPanel.selectedHistoryPlanMaintenanceTargets();
        return `<div class="history-plan-queue-panel">
            <span>
                <strong>\u5f53\u524d\u5904\u7406\u961f\u5217</strong>
                <em>\u603b\u6570 ${Format.html(queueFilter.caseNos.length)} | \u5f53\u524d\u53ef\u89c1 ${Format.html(items.length)} | \u5df2\u9009 ${Format.html(selectedCount)} | \u5f85\u7ef4\u62a4 ${Format.html(maintainCount)} | \u5f85\u590d\u6d4b ${Format.html(retestCount)} | \u5f85\u901a\u8fc7 ${Format.html(approveCount)}</em>
            </span>
            <div>
                ${maintenanceTargets.map((target) => `<button type="button" data-history-plan-selection-maintenance="${Format.html(target)}">\u7ef4\u62a4${Format.html(WorkbenchPanel.maintenanceTargetText(target))}</button>`).join("")}
                ${retestCount ? `<button type="button" data-history-plan-selection-retest>\u590d\u6d4b\u961f\u5217</button>` : ""}
                ${approveCount ? `<button type="button" data-history-plan-selection-approve>\u901a\u8fc7\u961f\u5217</button>` : ""}
                <button type="button" data-history-plan-queue-autoselect>\u91cd\u65b0\u9009\u4e2d\u961f\u5217</button>
                <button type="button" data-history-ledger-clear-queue-filter>\u6e05\u9664\u961f\u5217</button>
            </div>
        </div>`;
    },
    selectedHistoryPlans() {
        const selectedCaseNos = new Set(state.historyPlanSelected.keys());
        return (state.historyPlanCurrentItems || []).filter((plan) => selectedCaseNos.has(plan.caseNo || ""));
    },
    selectedHistoryPlanCaseNos() {
        return Array.from(state.historyPlanSelected.keys()).filter(Boolean);
    },
    selectedHistoryPlanMaintenanceTargets() {
        const allowed = ["base", "post", "education", "assessment"];
        const targets = new Set();
        for (const plan of WorkbenchPanel.selectedHistoryPlans()) {
            for (const suggestion of WorkbenchPanel.historyPlanMaintenanceSuggestions(plan)) {
                if (allowed.includes(suggestion.target)) {
                    targets.add(suggestion.target);
                }
            }
        }
        return targets.size ? allowed.filter((target) => targets.has(target)) : [];
    },
    historyPlanHtml(plan) {
        const period = plan.year ? `${plan.year}-${String(plan.month || 1).padStart(2, "0")}` : "-";
        const comparisonClass = Format.historyWriteComparisonClass(plan.comparisonStatus);
        const mismatchCount = Number(plan.comparisonMismatchCount || 0);
        const mismatchHtml = plan.comparisonStatus === "MISMATCHED"
            ? `<span class="history-plan-diff-count">\u5dee\u5f02 ${Format.html(mismatchCount)}</span>`
            : "";
        const reviewSource = WorkbenchPanel.historyWriteReviewSource(plan);
        const reviewHtml = plan.comparisonReviewStatus === "REVIEWED"
            ? `<span class="work-item-review reviewed">\u5df2\u6838\u67e5 ${Format.html(Format.historyWriteReviewCategoryText(plan.comparisonReviewCategory))}${reviewSource === "SUGGESTED" ? " / \u5efa\u8bae\u5e26\u5165" : ""}</span>`
            : "";
        const retestStatus = plan.comparisonRetestStatus || "NOT_RETESTED";
        const retestHtml = plan.comparisonStatus === "MISMATCHED"
            ? `<span class="work-item-trial ${Format.html(Format.historyWriteRetestStatusClass(retestStatus))}">${Format.html(Format.historyWriteRetestStatusText(retestStatus))}</span>`
            : "";
        const workflow = WorkbenchPanel.historyWriteWorkflow(plan);
        const selectable = WorkbenchPanel.historyPlanSelectable(plan, workflow);
        const selected = state.historyPlanSelected.has(plan.caseNo || "");
        const workflowHtml = plan.comparisonStatus === "MISMATCHED"
            ? `<span class="work-item-trial ${Format.html(Format.historyWritePriorityClass(workflow.priority))}">\u4f18\u5148\u7ea7 ${Format.html(Format.historyWritePriorityText(workflow.priority))}</span>`
            : "";
        const maintenanceHtml = WorkbenchPanel.historyPlanMaintenanceHtml(plan, retestStatus);
        const nextActionsHtml = WorkbenchPanel.historyPlanNextActionsHtml(plan, workflow, retestStatus);
        return `
            <div class="history-plan-row ${plan.comparisonStatus === "MISMATCHED" ? "mismatched" : ""} ${selected ? "selected" : ""}">
                ${selectable ? `<label class="history-plan-select" title="\u9009\u62e9\u6b64\u8ba1\u5212">
                    <input type="checkbox" data-history-plan-select-case-no="${Format.html(plan.caseNo || "")}" data-history-plan-select-person-code="${Format.html(plan.personCode || "")}" data-history-plan-select-action-code="${Format.html(workflow.actionCode || "")}" ${selected ? "checked" : ""}>
                </label>` : `<span class="history-plan-select placeholder"></span>`}
                <button type="button" class="history-plan-item" data-history-write-plan-case-no="${Format.html(plan.caseNo)}">
                    <span class="history-plan-main">
                        <strong>${Format.html(plan.personCode || "-")}</strong>
                        <span>${Format.html(plan.businessType || "-")} | ${Format.html(period)}</span>
                    </span>
                    <span class="history-plan-meta">
                        <span>${Format.html(WorkbenchPanel.planStatusText(plan.planStatus))}</span>
                        <small>${Format.html(plan.planNo || "-")}</small>
                    </span>
                    <span class="history-plan-flags">
                        <span class="work-item-trial ${Format.html(comparisonClass)}">${Format.html(Format.historyWriteComparisonText(plan.comparisonStatus))}</span>
                        ${mismatchHtml}
                        ${retestHtml}
                        ${workflowHtml}
                        ${reviewHtml}
                    </span>
                    <span class="history-plan-sub">${Format.html(workflow.action || plan.executionMessage || plan.rollbackMessage || plan.workItemId || "-")}</span>
                </button>
                <div class="history-plan-actions">
                    <button type="button" class="history-plan-compare" data-history-write-comparison-case-no="${Format.html(plan.caseNo)}">\u5b57\u6bb5\u5bf9\u7167</button>
                    ${nextActionsHtml}
                    ${maintenanceHtml}
                </div>
            </div>
        `;
    },
    historyPlanIssueCount(plan) {
        if (!plan || !plan.issuesJson) {
            return 0;
        }
        try {
            const issues = JSON.parse(plan.issuesJson);
            return Array.isArray(issues) ? issues.length : 0;
        } catch (_) {
            return 0;
        }
    },
    downloadCsv(filename, rows) {
        const csv = `\uFEFF${rows.map((row) => row.map(Format.csv).join(",")).join("\n")}`;
        const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
    },
    batchPreviewCsvRows(result) {
        const rows = [["办理编号", "写入计划号", "人员编码", "单位编码", "年度", "月份", "变动类别", "预检状态", "是否可写", "问题"]];
        for (const item of result.items || []) {
            rows.push([
                item.caseNo,
                item.writePlanId,
                item.personCode,
                item.orgCode,
                item.year,
                item.month,
                item.businessType,
                item.status,
                item.writable ? "是" : "否",
                (item.issues || []).join(" | ")
            ]);
        }
        return rows;
    },
    batchRetestCsvRows(result) {
        const rows = [["\u529e\u7406\u7f16\u53f7", "\u4eba\u5458\u7f16\u7801", "\u5355\u4f4d\u7f16\u7801", "\u53d8\u52a8\u7c7b\u522b", "\u590d\u6d4b\u72b6\u6001", "\u5408\u8ba1\u662f\u5426\u4e00\u81f4", "\u5dee\u5f02\u6570", "\u8bf4\u660e"]];
        for (const item of result.items || []) {
            rows.push([
                item.caseNo,
                item.personCode,
                item.orgCode,
                item.businessType,
                item.status,
                item.totalMatched ? "\u662f" : "\u5426",
                item.mismatchCount,
                item.message
            ]);
        }
        return rows;
    },
    batchExecuteCsvRows(result) {
        const rows = [["办理编号", "写入计划号", "人员编码", "单位编码", "历史行ID", "状态", "是否调整sid", "说明"]];
        for (const item of result.items || []) {
            rows.push([
                item.caseNo,
                item.writePlanId,
                item.personCode,
                item.orgCode,
                item.historyId,
                item.status,
                item.sidUpdateRequired ? "是" : "否",
                item.message
            ]);
        }
        return rows;
    },
    batchResultStatusText(status) {
        return {
            REVIEWED: "\u5df2\u6807\u8bb0",
            EXECUTED: "\u6210\u529f",
            ROLLED_BACK: "\u5df2\u64a4\u9500",
            FAILED: "\u5931\u8d25",
            SKIPPED: "\u8df3\u8fc7"
        }[status] || status || "-";
    },
    batchResultStatusClass(status) {
        return {
            REVIEWED: "matched",
            EXECUTED: "matched",
            ROLLED_BACK: "matched",
            FAILED: "blocked",
            SKIPPED: "warning"
        }[status] || "";
    },
    batchResultStats(items) {
        return (items || []).reduce((stats, item) => {
            const status = item.status || "UNKNOWN";
            stats[status] = (stats[status] || 0) + 1;
            return stats;
        }, {});
    },
    batchResultFilterHtml(stats) {
        const total = Object.values(stats || {}).reduce((sum, value) => sum + Number(value || 0), 0);
        const order = ["ALL", "REVIEWED", "EXECUTED", "ROLLED_BACK", "SKIPPED", "FAILED"];
        const filters = order
            .map((status) => [status, status === "ALL" ? "\u5168\u90e8" : WorkbenchPanel.batchResultStatusText(status), status === "ALL" ? total : (stats[status] || 0)])
            .filter(([status, , count]) => status === "ALL" || Number(count || 0) > 0);
        return `<div class="batch-result-filters">
            ${filters.map(([value, label, count]) => `<button type="button" class="${value === "ALL" ? "active" : ""}" data-batch-result-filter="${Format.html(value)}">${Format.html(label)} ${Format.html(count)}</button>`).join("")}
        </div>`;
    },
    historyPlanFilterCount() {
        return [
            els.historyPlanStatusSelect?.value || "",
            els.historyPlanComparisonSelect?.value || "",
            els.historyPlanReviewSelect?.value || "",
            els.historyPlanRetestSelect?.value || state.historyPlanRetestStatus || "",
            els.historyPlanMaintenanceSelect?.value || "",
            els.historyPlanPrioritySelect?.value || "",
            els.historyPlanActionSelect?.value || "",
            state.historyPlanMismatchField || ""
        ].filter(Boolean).length;
    },
    updateHistoryPlanActionState(summary = state.historyPlanSummary) {
        const canUsePlans = Permissions.has("SALARY_DONE");
        if (els.historyPlanClearFiltersButton) {
            els.historyPlanClearFiltersButton.disabled = !canUsePlans || !WorkbenchPanel.historyPlanFilterCount();
        }
        if (els.historyPlanBatchPreviewButton) {
            els.historyPlanBatchPreviewButton.disabled = !canUsePlans || !Number(summary.total || 0);
        }
        if (els.historyPlanBatchRetestButton) {
            els.historyPlanBatchRetestButton.disabled = !canUsePlans || !Number(summary.total || 0);
        }
        if (els.historyPlanBatchRetestApproveButton) {
            els.historyPlanBatchRetestApproveButton.disabled = !canUsePlans || !Number(summary.total || 0);
        }
        if (els.historyPlanBatchExecuteButton) {
            els.historyPlanBatchExecuteButton.disabled = !canUsePlans || !Number(summary.executable || 0);
        }
        if (els.historyPlanBatchRollbackButton) {
            els.historyPlanBatchRollbackButton.disabled = !canUsePlans || !Number(summary.executed || 0);
        }
        if (els.historyPlanExportButton) {
            els.historyPlanExportButton.disabled = !canUsePlans || !Permissions.has("SALARY_EXPORT") || !Number(summary.total || 0);
        }
    },
    renderHistoryPlanSummary(plans) {
        if (!els.historyPlanSummary) {
            return;
        }
        const items = Array.isArray(plans) ? plans : [];
        const summary = items.reduce((acc, plan) => {
            const status = plan.planStatus || "";
            const issues = WorkbenchPanel.historyPlanIssueCount(plan);
            acc.total += 1;
            acc.prepared += status === "PREPARED" ? 1 : 0;
            acc.executable += status === "PREPARED" && plan.writable === true ? 1 : 0;
            acc.blocked += status === "BLOCKED" || plan.writable === false ? 1 : 0;
            acc.executed += status === "EXECUTED" ? 1 : 0;
            acc.rolledBack += status === "ROLLED_BACK" ? 1 : 0;
            acc.matched += plan.comparisonStatus === "MATCHED" ? 1 : 0;
            acc.mismatched += plan.comparisonStatus === "MISMATCHED" ? 1 : 0;
            acc.pendingReview += plan.comparisonStatus === "MISMATCHED" && plan.comparisonReviewStatus !== "REVIEWED" ? 1 : 0;
            acc.retestMismatched += plan.comparisonRetestStatus === "RETEST_MISMATCHED" ? 1 : 0;
            const workflow = WorkbenchPanel.historyWriteWorkflow(plan);
            acc.highPriority += workflow.priority === "HIGH" ? 1 : 0;
            acc.mediumPriority += workflow.priority === "MEDIUM" ? 1 : 0;
            acc.issues += issues;
            return acc;
        }, { total: 0, prepared: 0, executable: 0, blocked: 0, executed: 0, rolledBack: 0, matched: 0, mismatched: 0, pendingReview: 0, retestMismatched: 0, highPriority: 0, mediumPriority: 0, issues: 0 });
        state.historyPlanSummary = summary;
        WorkbenchPanel.updateHistoryPlanActionState(summary);
        els.historyPlanSummary.innerHTML = `
            ${WorkbenchPanel.historyPlanQueuePanelHtml(items)}
            ${WorkbenchPanel.historyPlanSelectionSummaryHtml()}
            <span>\u5f53\u524d ${summary.total} \u6761</span>
            <span>\u5f85\u6267\u884c ${summary.prepared}</span>
            <span>\u53ef\u5199\u5165 ${summary.executable}</span>
            <span>\u5df2\u963b\u65ad ${summary.blocked}</span>
            <span>\u5df2\u5199\u5165 ${summary.executed}</span>
            <span>\u5df2\u4e00\u81f4 ${summary.matched}</span>
            <span>\u4e0d\u4e00\u81f4 ${summary.mismatched}</span>
            <button type="button" class="history-plan-summary-action" data-history-plan-exception-filter>\u5f02\u5e38\u5f85\u6838\u67e5 ${summary.pendingReview}</button>
            <button type="button" class="history-plan-summary-action" data-history-plan-retest-mismatch-filter>\u590d\u6d4b\u6709\u5dee\u5f02 ${summary.retestMismatched}</button>
            <button type="button" class="history-plan-summary-action" data-history-plan-priority-filter="HIGH">\u9ad8\u4f18\u5148\u7ea7 ${summary.highPriority}</button>
            <button type="button" class="history-plan-summary-action" data-history-plan-priority-filter="MEDIUM">\u4e2d\u4f18\u5148\u7ea7 ${summary.mediumPriority}</button>
            <span>\u5df2\u64a4\u9500 ${summary.rolledBack}</span>
            <span>\u95ee\u9898 ${summary.issues}</span>
        `;
    },
    async showPendingHistoryWriteReviews() {
        if (els.historyPlanStatusSelect) {
            els.historyPlanStatusSelect.value = "EXECUTED";
        }
        if (els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "MISMATCHED";
        }
        if (els.historyPlanReviewSelect) {
            els.historyPlanReviewSelect.value = "PENDING";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async showRetestMismatchedHistoryWritePlans() {
        if (els.historyPlanStatusSelect) {
            els.historyPlanStatusSelect.value = "EXECUTED";
        }
        if (els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "MISMATCHED";
        }
        if (els.historyPlanRetestSelect) {
            els.historyPlanRetestSelect.value = "RETEST_MISMATCHED";
        }
        state.historyPlanRetestStatus = "RETEST_MISMATCHED";
        if (els.historyPlanPrioritySelect) {
            els.historyPlanPrioritySelect.value = "HIGH";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async showPriorityHistoryWritePlans(priority) {
        if (els.historyPlanPrioritySelect) {
            els.historyPlanPrioritySelect.value = priority || "";
        }
        if ((priority === "HIGH" || priority === "MEDIUM") && els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "MISMATCHED";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    historyReviewStatusText(status) {
        const value = status || "";
        if (value === "PENDING") {
            return "\u5f85\u6838\u67e5";
        }
        if (value === "REVIEWED") {
            return "\u5df2\u6838\u67e5";
        }
        if (value === "NOT_REQUIRED") {
            return "\u65e0\u9700\u6838\u67e5";
        }
        return value || "-";
    },
    ledgerGroupHtml(title, groups, filterType) {
        const items = Array.isArray(groups) ? groups : [];
        const rows = items.length
            ? items.map((group) => `
                <button type="button" data-history-ledger-filter="${Format.html(filterType)}" data-history-ledger-value="${Format.html(group.key || "")}">
                    <strong>${Format.html(group.title || group.key || "-")}</strong>
                    <em>${Format.html(group.count || 0)}</em>
                    ${Number(group.pending || 0) ? `<small>\u5f85 ${Format.html(group.pending)}</small>` : ""}
                </button>
            `).join("")
            : `<span><strong>-</strong><em>0</em></span>`;
        return `
            <div class="history-review-ledger-group">
                <b>${Format.html(title)}</b>
                <div>${rows}</div>
            </div>
        `;
    },
    ledgerFieldHtml(fields) {
        const items = Array.isArray(fields) ? fields : [];
        const rows = items.length
            ? items.map((field) => `
                <button type="button" data-history-ledger-filter="FIELD" data-history-ledger-value="${Format.html(field.historyField || field.itemCode || field.itemName || "")}">
                    <strong>${Format.html(field.itemName || field.historyField || field.itemCode || "-")}</strong>
                    <em>${Format.html(field.count || 0)}</em>
                    ${field.historyField ? `<small>${Format.html(field.historyField)}</small>` : ""}
                </button>
            `).join("")
            : `<span><strong>-</strong><em>0</em></span>`;
        return `
            <div class="history-review-ledger-group">
                <b>\u5dee\u5f02\u5b57\u6bb5</b>
                <div>${rows}</div>
            </div>
        `;
    },
    ledgerMetricHtml(label, count, filterType = "", filterValue = "") {
        const safeCount = Number(count || 0);
        if (filterType && safeCount > 0) {
            return `<button type="button" class="history-review-ledger-stat" data-history-ledger-filter="${Format.html(filterType)}" data-history-ledger-value="${Format.html(filterValue)}">${Format.html(label)} ${Format.html(safeCount)}</button>`;
        }
        return `<span>${Format.html(label)} ${Format.html(safeCount)}</span>`;
    },
    historyPlanFilterChipHtml(type, label, value, text) {
        if (!value) {
            return "";
        }
        return `<button type="button" data-history-ledger-clear-filter="${Format.html(type)}">${Format.html(label)} ${Format.html(text || value)} \u00d7</button>`;
    },
    historyPlanFilterChipsHtml() {
        const status = els.historyPlanStatusSelect?.value || "";
        const comparison = els.historyPlanComparisonSelect?.value || "";
        const review = els.historyPlanReviewSelect?.value || "";
        const priority = els.historyPlanPrioritySelect?.value || "";
        const action = els.historyPlanActionSelect?.value || "";
        const locate = state.historyPlanLocate;
        const queueFilter = state.historyPlanQueueFilter;
        return [
            locate ? `<button type="button" class="history-plan-locate-chip" data-history-ledger-clear-locate>\u6279\u91cf\u7ed3\u679c\u5b9a\u4f4d ${Format.html(locate.keyword || "-")} ${Format.html(locate.statusText || "")} \u00d7</button>` : "",
            queueFilter ? `<button type="button" class="history-plan-locate-chip" data-history-ledger-clear-queue-filter>\u961f\u5217\u4ecd\u6709\u5dee\u5f02 ${Format.html(queueFilter.caseNos?.length || 0)} \u00d7</button>` : "",
            WorkbenchPanel.historyPlanFilterChipHtml("status", "\u72b6\u6001", status, WorkbenchPanel.planStatusText(status)),
            WorkbenchPanel.historyPlanFilterChipHtml("comparison", "\u5bf9\u7167", comparison, Format.historyWriteComparisonText(comparison)),
            WorkbenchPanel.historyPlanFilterChipHtml("review", "\u6838\u67e5", review, WorkbenchPanel.historyReviewStatusText(review)),
            WorkbenchPanel.historyPlanFilterChipHtml("priority", "\u4f18\u5148\u7ea7", priority, Format.historyWritePriorityText(priority)),
            WorkbenchPanel.historyPlanFilterChipHtml("action", "\u52a8\u4f5c", action, Format.historyWriteActionText(action))
        ].join("");
    },
    renderHistoryReviewLedger(ledger) {
        if (!els.historyReviewLedger) {
            return;
        }
        if (!ledger) {
            els.historyReviewLedger.innerHTML = "";
            return;
        }
        const mismatchField = state.historyPlanMismatchField || "";
        const retestStatus = state.historyPlanRetestStatus || "";
        const maintenanceTarget = els.historyPlanMaintenanceSelect?.value || "";
        const filterCount = WorkbenchPanel.historyPlanFilterCount();
        els.historyReviewLedger.innerHTML = `
            <div class="history-review-ledger-total">
                ${WorkbenchPanel.ledgerMetricHtml("\u53f0\u8d26", ledger.total)}
                ${filterCount ? `<button type="button" data-history-ledger-clear-all>\u7b5b\u9009 ${Format.html(filterCount)} \u9879 \u00d7</button>` : ""}
                ${WorkbenchPanel.historyPlanFilterChipsHtml()}
                ${WorkbenchPanel.ledgerMetricHtml("\u5f85\u6838\u67e5", ledger.pending, "REVIEW", "PENDING")}
                ${WorkbenchPanel.ledgerMetricHtml("\u5df2\u6838\u67e5", ledger.reviewed, "REVIEW", "REVIEWED")}
                ${WorkbenchPanel.ledgerMetricHtml("\u4e0d\u4e00\u81f4", ledger.mismatched, "COMPARISON", "MISMATCHED")}
                ${WorkbenchPanel.ledgerMetricHtml("\u5df2\u4e00\u81f4", ledger.matched, "COMPARISON", "MATCHED")}
                ${WorkbenchPanel.ledgerMetricHtml("\u5df2\u590d\u6d4b", ledger.retested)}
                ${WorkbenchPanel.ledgerMetricHtml("\u590d\u6d4b\u4e00\u81f4", ledger.retestMatched, "RETEST", "RETEST_MATCHED")}
                ${WorkbenchPanel.ledgerMetricHtml("\u590d\u6d4b\u6709\u5dee\u5f02", ledger.retestMismatched, "RETEST", "RETEST_MISMATCHED")}
                ${WorkbenchPanel.ledgerMetricHtml("\u5efa\u8bae\u5e26\u5165", ledger.suggestedReviewed, "SOURCE", "SUGGESTED")}
                ${WorkbenchPanel.ledgerMetricHtml("\u590d\u6d4b\u901a\u8fc7", ledger.retestReviewed, "SOURCE", "RETEST")}
                <span>\u4eba\u5de5\u6838\u67e5 ${Format.html(ledger.manualReviewed || 0)}</span>
                ${WorkbenchPanel.ledgerMetricHtml("\u5f85\u6279\u91cf\u590d\u6d4b", ledger.pendingRetestFirst, "ACTION", "RETEST_FIRST")}
                ${Number(ledger.pendingRetestFirst || 0) ? `<button type="button" class="history-review-ledger-primary" data-history-ledger-batch-retest-approve>\u6279\u91cf\u590d\u6d4b\u5e76\u901a\u8fc7\u4e00\u81f4\u9879 ${Format.html(ledger.pendingRetestFirst || 0)}</button>` : ""}
                ${WorkbenchPanel.ledgerMetricHtml("\u5f85\u7ef4\u62a4\u590d\u6d4b", ledger.pendingMaintainAndRetest, "ACTION", "MAINTAIN_AND_RETEST")}
                ${WorkbenchPanel.ledgerMetricHtml("\u9ad8\u4f18\u5148\u7ea7", ledger.highPriority, "PRIORITY", "HIGH")}
                ${WorkbenchPanel.ledgerMetricHtml("\u4e2d\u4f18\u5148\u7ea7", ledger.mediumPriority, "PRIORITY", "MEDIUM")}
                ${WorkbenchPanel.ledgerMetricHtml("\u5df2\u5b8c\u6210", ledger.donePriority, "PRIORITY", "DONE")}
                ${retestStatus ? `<button type="button" data-history-ledger-clear-retest>\u590d\u6d4b ${Format.html(Format.historyWriteRetestStatusText(retestStatus))} \u00d7</button>` : ""}
                ${maintenanceTarget ? `<button type="button" data-history-ledger-clear-maintenance>\u5efa\u8bae ${Format.html(Format.historyWriteMaintenanceText(maintenanceTarget))} \u00d7</button>` : ""}
                ${mismatchField ? `<button type="button" data-history-ledger-clear-field>\u5b57\u6bb5 ${Format.html(mismatchField)} \u00d7</button>` : ""}
            </div>
            ${WorkbenchPanel.ledgerGroupHtml("\u5355\u4f4d", ledger.byOrg, "ORG")}
            ${WorkbenchPanel.ledgerGroupHtml("\u53d8\u52a8\u7c7b\u578b", ledger.byBusinessType, "BUSINESS")}
            ${WorkbenchPanel.ledgerGroupHtml("\u6838\u67e5\u72b6\u6001", (ledger.byReviewStatus || []).map((group) => ({
                ...group,
                title: WorkbenchPanel.historyReviewStatusText(group.key)
            })), "REVIEW")}
            ${WorkbenchPanel.ledgerGroupHtml("\u6838\u67e5\u5206\u7c7b", (ledger.byReviewCategory || []).map((group) => ({
                ...group,
                title: Format.historyWriteReviewCategoryText(group.key)
            })), "CATEGORY")}
            ${WorkbenchPanel.ledgerGroupHtml("\u6838\u67e5\u6765\u6e90", (ledger.byReviewSource || []).map((group) => ({
                ...group,
                title: Format.historyWriteReviewSourceText(group.key)
            })), "SOURCE")}
            ${WorkbenchPanel.ledgerGroupHtml("\u5efa\u8bae\u65b9\u5411", (ledger.byMaintenanceTarget || []).map((group) => ({
                ...group,
                title: Format.historyWriteMaintenanceText(group.key)
            })), "MAINTENANCE")}
            ${WorkbenchPanel.ledgerGroupHtml("\u5904\u7406\u4f18\u5148\u7ea7", (ledger.byPriority || []).map((group) => ({
                ...group,
                title: Format.historyWritePriorityText(group.key)
            })), "PRIORITY")}
            ${WorkbenchPanel.ledgerGroupHtml("\u4e0b\u4e00\u6b65\u52a8\u4f5c", (ledger.byNextAction || []).map((group) => ({
                ...group,
                title: Format.historyWriteActionText(group.key)
            })), "ACTION")}
            ${WorkbenchPanel.ledgerGroupHtml("\u590d\u6d4b\u72b6\u6001", (ledger.byRetestStatus || []).map((group) => ({
                ...group,
                title: Format.historyWriteRetestStatusText(group.key)
            })), "RETEST")}
            ${WorkbenchPanel.ledgerFieldHtml(ledger.topMismatchFields)}
        `;
    },
    async applyHistoryLedgerFilter(type, value) {
        const safeType = type || "";
        const safeValue = value || "";
        if (safeType === "ORG" || safeType === "BUSINESS") {
            els.workbenchKeywordInput.value = safeValue === "-" ? "" : safeValue;
            state.historyPlanMismatchField = "";
            state.historyPlanRetestStatus = "";
            if (els.historyPlanMaintenanceSelect) {
                els.historyPlanMaintenanceSelect.value = "";
            }
        } else if (safeType === "REVIEW") {
            if (els.historyPlanReviewSelect) {
                els.historyPlanReviewSelect.value = safeValue === "NOT_REQUIRED" ? "" : safeValue;
            }
            if ((safeValue === "PENDING" || safeValue === "REVIEWED") && els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
            if (els.historyPlanStatusSelect) {
                els.historyPlanStatusSelect.value = safeValue === "NOT_REQUIRED" ? "" : "EXECUTED";
            }
        } else if (safeType === "COMPARISON") {
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = safeValue === "-" ? "" : safeValue;
            }
            if (els.historyPlanStatusSelect) {
                els.historyPlanStatusSelect.value = "EXECUTED";
            }
            if (els.historyPlanReviewSelect && safeValue === "MATCHED") {
                els.historyPlanReviewSelect.value = "";
            }
        } else if (safeType === "CATEGORY") {
            els.workbenchKeywordInput.value = safeValue === "-" ? "" : safeValue;
            if (els.historyPlanReviewSelect) {
                els.historyPlanReviewSelect.value = "REVIEWED";
            }
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
        } else if (safeType === "SOURCE") {
            const sourceKeyword = {
                SUGGESTED: "\u6309\u5efa\u8bae\u68c0\u67e5\u65b9\u5411\u767b\u8bb0",
                RETEST: "\u590d\u6d4b\u5df2\u4e00\u81f4",
                MANUAL: ""
            }[safeValue] || "";
            els.workbenchKeywordInput.value = sourceKeyword;
            if (els.historyPlanReviewSelect) {
                els.historyPlanReviewSelect.value = "REVIEWED";
            }
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
        } else if (safeType === "MAINTENANCE") {
            if (els.historyPlanMaintenanceSelect) {
                els.historyPlanMaintenanceSelect.value = safeValue === "-" ? "" : safeValue;
            }
            if (els.historyPlanStatusSelect) {
                els.historyPlanStatusSelect.value = "EXECUTED";
            }
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
            if (els.historyPlanReviewSelect) {
                els.historyPlanReviewSelect.value = "PENDING";
            }
            if (els.historyPlanActionSelect) {
                els.historyPlanActionSelect.value = "MAINTAIN_AND_RETEST";
            }
        } else if (safeType === "FIELD") {
            state.historyPlanMismatchField = safeValue;
            if (els.historyPlanStatusSelect) {
                els.historyPlanStatusSelect.value = "EXECUTED";
            }
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
        } else if (safeType === "RETEST") {
            state.historyPlanRetestStatus = safeValue === "-" ? "" : safeValue;
            if (els.historyPlanRetestSelect) {
                els.historyPlanRetestSelect.value = state.historyPlanRetestStatus;
            }
            if (els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
        } else if (safeType === "PRIORITY") {
            if (els.historyPlanPrioritySelect) {
                els.historyPlanPrioritySelect.value = safeValue === "-" ? "" : safeValue;
            }
            if ((safeValue === "HIGH" || safeValue === "MEDIUM") && els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
        } else if (safeType === "ACTION") {
            if (els.historyPlanActionSelect) {
                els.historyPlanActionSelect.value = safeValue === "-" ? "" : safeValue;
            }
            if (!["REVIEWED", "NOT_REQUIRED", ""].includes(safeValue) && els.historyPlanComparisonSelect) {
                els.historyPlanComparisonSelect.value = "MISMATCHED";
            }
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async clearHistoryLedgerFieldFilter() {
        state.historyPlanMismatchField = "";
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async clearHistoryLedgerRetestFilter() {
        state.historyPlanRetestStatus = "";
        if (els.historyPlanRetestSelect) {
            els.historyPlanRetestSelect.value = "";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async clearHistoryLedgerMaintenanceFilter() {
        if (els.historyPlanMaintenanceSelect) {
            els.historyPlanMaintenanceSelect.value = "";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async batchApproveRetestFromLedger() {
        if (els.historyPlanStatusSelect) {
            els.historyPlanStatusSelect.value = "EXECUTED";
        }
        if (els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "MISMATCHED";
        }
        if (els.historyPlanReviewSelect) {
            els.historyPlanReviewSelect.value = "PENDING";
        }
        if (els.historyPlanActionSelect) {
            els.historyPlanActionSelect.value = "RETEST_FIRST";
        }
        if (els.historyPlanRetestSelect) {
            els.historyPlanRetestSelect.value = "";
        }
        state.historyPlanRetestStatus = "";
        await WorkbenchPanel.batchApproveRetestHistoryWritePlans();
    },
    async locateBatchResult(item = {}) {
        const keyword = item.personCode || item.caseNo || item.writePlanId || "";
        if (els.workbenchKeywordInput) {
            els.workbenchKeywordInput.value = keyword;
        }
        if (els.historyPlanStatusSelect) {
            els.historyPlanStatusSelect.value = item.status === "ROLLED_BACK" ? "ROLLED_BACK" : "";
        }
        if (els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "";
        }
        if (els.historyPlanReviewSelect) {
            els.historyPlanReviewSelect.value = item.status === "REVIEWED" ? "REVIEWED" : "";
        }
        if (els.historyPlanRetestSelect) {
            els.historyPlanRetestSelect.value = "";
        }
        if (els.historyPlanMaintenanceSelect) {
            els.historyPlanMaintenanceSelect.value = "";
        }
        if (els.historyPlanPrioritySelect) {
            els.historyPlanPrioritySelect.value = "";
        }
        if (els.historyPlanActionSelect) {
            els.historyPlanActionSelect.value = "";
        }
        state.historyPlanMismatchField = "";
        state.historyPlanRetestStatus = "";
        state.historyPlanLocate = {
            keyword,
            status: item.status || "",
            statusText: WorkbenchPanel.batchResultStatusText(item.status)
        };
        await WorkbenchPanel.loadHistoryWritePlans();
        els.historyReviewLedger?.scrollIntoView({ behavior: "smooth", block: "start" });
        setStatus(keyword ? `\u5df2\u5b9a\u4f4d ${keyword} \u7684\u5386\u53f2\u5199\u5165\u8ba1\u5212` : TEXT.workbenchReady);
    },
    async clearHistoryPlanLocate() {
        state.historyPlanLocate = null;
        if (els.workbenchKeywordInput) {
            els.workbenchKeywordInput.value = "";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async clearHistoryPlanQueueFilter() {
        state.historyPlanQueueFilter = null;
        state.historyPlanSelected.clear();
        persistHistoryPlanQueueState();
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async autoSelectHistoryPlanQueue() {
        if (!state.historyPlanQueueFilter?.caseNos?.length) {
            return;
        }
        state.historyPlanQueueFilter = { ...state.historyPlanQueueFilter, autoSelect: true };
        persistHistoryPlanQueueState();
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async clearHistoryPlanFilter(type) {
        const selectByType = {
            status: els.historyPlanStatusSelect,
            comparison: els.historyPlanComparisonSelect,
            review: els.historyPlanReviewSelect,
            priority: els.historyPlanPrioritySelect,
            action: els.historyPlanActionSelect
        };
        const select = selectByType[type || ""];
        if (select) {
            select.value = "";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    async clearHistoryPlanFilters() {
        [
            els.historyPlanStatusSelect,
            els.historyPlanComparisonSelect,
            els.historyPlanReviewSelect,
            els.historyPlanRetestSelect,
            els.historyPlanMaintenanceSelect,
            els.historyPlanPrioritySelect,
            els.historyPlanActionSelect
        ].forEach((select) => {
            if (select) {
                select.value = "";
            }
        });
        state.historyPlanMismatchField = "";
        state.historyPlanRetestStatus = "";
        state.historyPlanLocate = null;
        state.historyPlanQueueFilter = null;
        state.historyPlanSelected.clear();
        persistHistoryPlanQueueState();
        if (els.workbenchKeywordInput) {
            els.workbenchKeywordInput.value = "";
        }
        await WorkbenchPanel.loadHistoryWritePlans();
    },
    renderHistoryPlans(plans) {
        if (!els.historyWritePlans) {
            return;
        }
        state.historyPlanCurrentItems = Array.isArray(plans) ? plans : [];
        const visibleCaseNos = new Set((plans || []).map((plan) => plan.caseNo || "").filter(Boolean));
        let selectionChanged = false;
        for (const caseNo of Array.from(state.historyPlanSelected.keys())) {
            if (!visibleCaseNos.has(caseNo)) {
                state.historyPlanSelected.delete(caseNo);
                selectionChanged = true;
            }
        }
        if (state.historyPlanQueueFilter?.autoSelect) {
            state.historyPlanSelected.clear();
            for (const plan of plans || []) {
                const workflow = WorkbenchPanel.historyWriteWorkflow(plan);
                if (WorkbenchPanel.historyPlanSelectable(plan, workflow)) {
                    state.historyPlanSelected.set(plan.caseNo || "", {
                        caseNo: plan.caseNo || "",
                        personCode: plan.personCode || "",
                        actionCode: workflow.actionCode || ""
                    });
                }
            }
            state.historyPlanQueueFilter = { ...state.historyPlanQueueFilter, autoSelect: false };
            selectionChanged = true;
        }
        if (selectionChanged) {
            persistHistoryPlanQueueState();
        }
        WorkbenchPanel.renderHistoryPlanSummary(plans);
        if (!plans.length) {
            els.historyWritePlans.innerHTML = `<div class="loading">${TEXT.noWorkItems}</div>`;
            return;
        }
        els.historyWritePlans.innerHTML = plans.map((plan) => WorkbenchPanel.historyPlanHtml(plan)).join("");
    },
    historyPlanParams(defaultStatus = "") {
        const filters = WorkbenchPanel.filters();
        return new URLSearchParams({
            status: els.historyPlanStatusSelect?.value || defaultStatus,
            comparisonStatus: els.historyPlanComparisonSelect?.value || "",
            reviewStatus: els.historyPlanReviewSelect?.value || "",
            keyword: filters.keyword,
            mismatchField: state.historyPlanMismatchField || "",
            maintenanceTarget: els.historyPlanMaintenanceSelect?.value || "",
            retestStatus: els.historyPlanRetestSelect?.value || state.historyPlanRetestStatus || "",
            priority: els.historyPlanPrioritySelect?.value || "",
            actionCode: els.historyPlanActionSelect?.value || "",
            limit: 50
        });
    },
    async loadHistoryWritePlans(requestId = state.workbenchRequestId) {
        if (!Permissions.has("SALARY_DONE")) {
            WorkbenchPanel.renderHistoryReviewLedger(null);
            WorkbenchPanel.renderHistoryPlans([]);
            WorkbenchPanel.updateHistoryPlanActionState();
            return [];
        }
        const params = WorkbenchPanel.historyPlanParams("");
        const ledgerParams = new URLSearchParams(params);
        ledgerParams.set("limit", "500");
        const [plans, ledger] = await Promise.all([
            Api.request(`/api/workbench/history-write-plans?${params.toString()}`),
            Api.request(`/api/workbench/history-write-review-ledger?${ledgerParams.toString()}`)
        ]);
        if (requestId !== state.workbenchRequestId) {
            return plans;
        }
        const visiblePlans = WorkbenchPanel.applyHistoryPlanQueueFilter(plans || []);
        WorkbenchPanel.renderHistoryReviewLedger(ledger);
        WorkbenchPanel.renderHistoryPlans(visiblePlans);
        WorkbenchPanel.updateHistoryPlanActionState();
        return visiblePlans;
    },
    applyHistoryPlanQueueFilter(plans) {
        const filter = state.historyPlanQueueFilter;
        if (!filter?.caseNos?.length) {
            return plans;
        }
        const caseNos = new Set(filter.caseNos);
        return (plans || []).filter((plan) => caseNos.has(plan.caseNo || ""));
    },
    async batchPreviewHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const params = WorkbenchPanel.historyPlanParams("PREPARED");
        const result = await Api.request(`/api/workbench/history-write-plans/batch-preview?${params.toString()}`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteBatchPreview(result);
        await WorkbenchPanel.loadHistoryWritePlans();
        setStatus(TEXT.caseDetail);
    },
    async batchRetestHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const params = WorkbenchPanel.historyPlanParams("EXECUTED");
        if (!params.get("comparisonStatus")) {
            params.set("comparisonStatus", "MISMATCHED");
        }
        if (!params.get("actionCode")) {
            params.set("actionCode", "RETEST_FIRST");
        }
        const result = await Api.request(`/api/workbench/history-write-plans/batch-retest-preview?${params.toString()}`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteBatchRetest(result);
        await WorkbenchPanel.loadHistoryWritePlans();
        setStatus(TEXT.caseDetail);
    },
    async batchRetestSelectedHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        const caseNos = WorkbenchPanel.selectedHistoryPlanCaseNos();
        if (!caseNos.length) {
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const result = await Api.request("/api/workbench/history-write-plans/selected-retest-preview", {
            method: "POST",
            body: JSON.stringify({ caseNos })
        });
        WorkbenchPanel.showHistoryWriteBatchRetest(result);
        state.historyPlanSelected.clear();
        persistHistoryPlanQueueState();
        await WorkbenchPanel.loadHistoryWritePlans();
        setStatus(TEXT.caseDetail);
    },
    async batchApproveRetestHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const params = WorkbenchPanel.historyPlanParams("EXECUTED");
        if (!params.get("comparisonStatus")) {
            params.set("comparisonStatus", "MISMATCHED");
        }
        if (!params.get("reviewStatus")) {
            params.set("reviewStatus", "PENDING");
        }
        if (!params.get("actionCode")) {
            params.set("actionCode", "RETEST_FIRST");
        }
        const preview = await Api.request(`/api/workbench/history-write-plans/batch-retest-preview?${params.toString()}`, {
            method: "POST"
        });
        if (!Number(preview.matched || 0)) {
            WorkbenchPanel.showHistoryWriteBatchRetest(preview);
            await WorkbenchPanel.loadHistoryWritePlans();
            setStatus(TEXT.caseDetail);
            return;
        }
        if (!window.confirm(`当前筛选共 ${preview.total || 0} 条，复测一致 ${preview.matched || 0} 条，仍有差异 ${preview.mismatched || 0} 条。确定将复测一致的记录标记为通过吗？`)) {
            setStatus(TEXT.workbenchReady);
            return;
        }
        const result = await Api.request(`/api/workbench/history-write-plans/batch-retest-approve?${params.toString()}`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteBatchRetestApprove(result);
        await WorkbenchPanel.load();
        setStatus(TEXT.caseDetail);
    },
    async batchApproveSelectedRetestHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        const caseNos = WorkbenchPanel.selectedHistoryPlanCaseNos();
        if (!caseNos.length) {
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const preview = await Api.request("/api/workbench/history-write-plans/selected-retest-preview", {
            method: "POST",
            body: JSON.stringify({ caseNos })
        });
        if (!Number(preview.matched || 0)) {
            WorkbenchPanel.showHistoryWriteBatchRetest(preview);
            state.historyPlanSelected.clear();
            persistHistoryPlanQueueState();
            await WorkbenchPanel.loadHistoryWritePlans();
            setStatus(TEXT.caseDetail);
            return;
        }
        if (!window.confirm(`选中 ${caseNos.length} 条，复测一致 ${preview.matched || 0} 条，仍有差异 ${preview.mismatched || 0} 条。确定将复测一致的记录标记为通过吗？`)) {
            setStatus(TEXT.workbenchReady);
            return;
        }
        const result = await Api.request("/api/workbench/history-write-plans/selected-retest-approve", {
            method: "POST",
            body: JSON.stringify({ caseNos })
        });
        WorkbenchPanel.showHistoryWriteBatchRetestApprove(result);
        state.historyPlanSelected.clear();
        persistHistoryPlanQueueState();
        await WorkbenchPanel.load();
        setStatus(TEXT.caseDetail);
    },
    selectedHistoryPlanMaintenanceSource(plan, target, queue) {
        const suggestion = WorkbenchPanel.historyPlanMaintenanceSuggestions(plan)
            .find((item) => item.target === target) || {};
        const fields = Array.isArray(suggestion.fields) ? suggestion.fields.join("\u3001") : "";
        return {
            caseNo: plan.caseNo || "",
            label: suggestion.label || WorkbenchPanel.maintenanceTargetText(target),
            reason: suggestion.reason || "",
            fields,
            selectionQueue: queue,
            selectionTotal: queue.length
        };
    },
    async openSelectedHistoryPlanMaintenance(target) {
        const plans = WorkbenchPanel.selectedHistoryPlans();
        if (!plans.length) {
            return;
        }
        const preferred = plans.find((plan) => WorkbenchPanel.historyPlanMaintenanceSuggestions(plan).some((item) => item.target === target)) || plans[0];
        const queue = plans.map((plan) => ({
            caseNo: plan.caseNo || "",
            personCode: plan.personCode || ""
        })).filter((item) => item.caseNo && item.personCode);
        await WorkbenchPanel.openPersonMaintenance(
            preferred.personCode,
            target,
            WorkbenchPanel.selectedHistoryPlanMaintenanceSource(preferred, target, queue)
        );
    },
    async batchExecuteHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const params = WorkbenchPanel.historyPlanParams("PREPARED");
        const preview = await Api.request(`/api/workbench/history-write-plans/batch-preview?${params.toString()}`, {
            method: "POST"
        });
        if (!Number(preview.ready || 0)) {
            WorkbenchPanel.showHistoryWriteBatchPreview(preview);
            await WorkbenchPanel.loadHistoryWritePlans();
            setStatus(TEXT.caseDetail);
            return;
        }
        if (!window.confirm(Format.text(TEXT.historyWriteBatchExecuteConfirm, {
            total: preview.total || 0,
            ready: preview.ready || 0,
            blocked: preview.blocked || 0,
            warning: preview.warning || 0
        }))) {
            setStatus(TEXT.workbenchReady);
            return;
        }
        const result = await Api.request(`/api/workbench/history-write-plans/batch-execute?${params.toString()}`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteBatchExecute(result);
        await WorkbenchPanel.load();
        setStatus(TEXT.caseDetail);
    },
    async batchRollbackHistoryWritePlans() {
        if (!Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const selectedStatus = els.historyPlanStatusSelect?.value || "";
        const params = WorkbenchPanel.historyPlanParams(selectedStatus || "EXECUTED");
        const plans = await Api.request(`/api/workbench/history-write-plans?${params.toString()}`);
        const eligible = (plans || []).filter((plan) => plan.planStatus === "EXECUTED" && plan.executionResult === "SUCCESS").length;
        if (!eligible) {
            WorkbenchPanel.showHistoryWriteBatchRollback({
                total: (plans || []).length,
                success: 0,
                failed: 0,
                skipped: (plans || []).length,
                items: (plans || []).map((plan) => ({
                    caseNo: plan.caseNo,
                    workItemId: plan.workItemId,
                    personCode: plan.personCode,
                    orgCode: plan.orgCode,
                    writePlanId: plan.planNo,
                    historyId: plan.insertedHistoryId,
                    status: "SKIPPED",
                    sidUpdateRequired: Boolean(plan.previousHistoryId || plan.nextHistoryId),
                    message: `Skipped because plan status is ${plan.planStatus || "-"}`
                }))
            });
            setStatus(TEXT.caseDetail);
            return;
        }
        if (!window.confirm(Format.text(TEXT.historyWriteBatchRollbackConfirm, {
            total: (plans || []).length,
            eligible
        }))) {
            setStatus(TEXT.workbenchReady);
            return;
        }
        const result = await Api.request(`/api/workbench/history-write-plans/batch-rollback?${params.toString()}`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteBatchRollback(result);
        await WorkbenchPanel.load();
        setStatus(TEXT.caseDetail);
    },
    exportHistoryWritePlans() {
        if (!Permissions.has("SALARY_EXPORT") || !Permissions.has("SALARY_DONE")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        const params = WorkbenchPanel.historyPlanParams("");
        params.set("limit", "5000");
        window.location.href = `/api/workbench/history-write-plans.csv?${params.toString()}`;
    },
    async load() {
        const requestId = ++state.workbenchRequestId;
        if (state.activeView === "workbench") {
            setStatus(TEXT.loadingWorkbench);
        }
        Permissions.applyWorkbench();
        els.workbenchMetrics.innerHTML = `<div class="loading">${TEXT.loadingWorkbench}</div>`;
        els.todoWorkItems.innerHTML = `<div class="loading">${TEXT.loadingWorkbench}</div>`;
        els.doneWorkItems.innerHTML = `<div class="loading">${TEXT.loadingWorkbench}</div>`;
        if (els.historyWritePlans) {
            els.historyWritePlans.innerHTML = `<div class="loading">${TEXT.loadingWorkbench}</div>`;
        }
        const loads = [Api.request("/api/workbench/summary").then((summary) => {
            if (requestId !== state.workbenchRequestId) {
                return;
            }
            const existing = new Map((state.workbench?.metrics || []).map((metric) => [metric.code, metric]));
            const metrics = (summary.metrics || []).map((metric) => {
                const current = existing.get(metric.code);
                return current && Number(current.count) >= 0 && Number(metric.count) < 0 ? current : metric;
            });
            state.workbench = { ...summary, metrics };
            WorkbenchPanel.renderMetrics(metrics);
        })];
        if (Permissions.has("SALARY_TODO") || Permissions.has("APPLICATION_TODO")) {
            loads.push(Api.request("/api/workbench/metrics/salary-todo").then((metric) => {
                if (requestId !== state.workbenchRequestId) {
                    return;
                }
                WorkbenchPanel.updateMetric(metric);
            }));
            loads.push(WorkbenchPanel.loadPage("TODO", true, requestId));
        } else {
            els.todoWorkItems.innerHTML = `<div class="loading">${TEXT.noWorkItems}</div>`;
            els.todoCount.textContent = "0";
        }
        if (Permissions.has("SALARY_DONE") || Permissions.has("APPLICATION_DONE")) {
            loads.push(WorkbenchPanel.loadPage("DONE", true, requestId));
            if (Permissions.has("SALARY_DONE")) {
                loads.push(WorkbenchPanel.loadHistoryWritePlans(requestId));
            }
        } else {
            els.doneWorkItems.innerHTML = `<div class="loading">${TEXT.noWorkItems}</div>`;
            els.doneCount.textContent = "0";
            if (els.historyWritePlans) {
                els.historyWritePlans.innerHTML = `<div class="loading">${TEXT.noWorkItems}</div>`;
            }
        }
        await Promise.all(loads);
        Permissions.applyWorkbench();
        if (requestId !== state.workbenchRequestId) {
            return;
        }
        if (state.activeView === "workbench") {
            setStatus(TEXT.workbenchReady);
        }
    },
    filters() {
        return {
            keyword: (els.workbenchKeywordInput.value || "").trim(),
            changeType: els.workbenchChangeTypeSelect.value || "",
            caseStatus: els.workbenchCaseStatusSelect.value || "DONE",
            trialStatus: els.workbenchTrialStatusSelect.value || "",
            reviewStatus: els.workbenchReviewStatusSelect.value || ""
        };
    },
    renderFilterSummary() {
        const filters = WorkbenchPanel.filters();
        const parts = [];
        if (filters.keyword) {
            parts.push(`\u5173\u952e\u8bcd\uff1a${filters.keyword}`);
        }
        if (filters.changeType) {
            parts.push(`\u53d8\u52a8\uff1a${filters.changeType}`);
        }
        parts.push(`\u5df2\u529e\u72b6\u6001\uff1a${Format.businessStatusText(filters.caseStatus)}`);
        if (filters.trialStatus) {
            parts.push(`\u8bd5\u7b97\uff1a${Format.statusText(filters.trialStatus)}`);
        }
        if (filters.reviewStatus) {
            parts.push(`\u590d\u6838\uff1a${Format.reviewStatusText(filters.reviewStatus)}`);
        }
        els.workbenchFilterSummary.textContent = parts.length ? parts.join(" | ") : "\u5168\u90e8\u4e1a\u52a1";
    },
    updateMoreButtons(todoTotal = Number(els.todoCount.textContent || 0), doneTotal = Number(els.doneCount.textContent || 0)) {
        els.loadMoreTodoButton.disabled = state.workbenchTodoLoaded >= todoTotal;
        els.loadMoreDoneButton.disabled = state.workbenchDoneLoaded >= doneTotal;
    },
    async loadPage(status, reset, requestId = state.workbenchRequestId) {
        const isDone = status === "DONE";
        const allowed = isDone
            ? (Permissions.has("SALARY_DONE") || Permissions.has("APPLICATION_DONE"))
            : (Permissions.has("SALARY_TODO") || Permissions.has("APPLICATION_TODO"));
        if (!allowed) {
            return { items: [], total: 0 };
        }
        const offset = reset ? 0 : (isDone ? state.workbenchDoneLoaded : state.workbenchTodoLoaded);
        const filters = WorkbenchPanel.filters();
        WorkbenchPanel.renderFilterSummary();
        const params = new URLSearchParams({
            status,
            offset,
            limit: 12,
            keyword: filters.keyword,
            changeType: filters.changeType,
            caseStatus: isDone ? filters.caseStatus : "",
            trialStatus: isDone ? filters.trialStatus : "",
            reviewStatus: isDone ? filters.reviewStatus : ""
        });
        const page = await Api.request(`/api/workbench/items?${params.toString()}`);
        if (requestId !== state.workbenchRequestId) {
            return page;
        }
        const container = isDone ? els.doneWorkItems : els.todoWorkItems;
        if (reset) {
            WorkbenchPanel.renderItems(page.items || [], container);
        } else {
            WorkbenchPanel.appendItems(page.items || [], container);
        }
        if (isDone) {
            state.workbenchDoneLoaded = reset ? (page.items || []).length : state.workbenchDoneLoaded + (page.items || []).length;
            els.doneCount.textContent = page.total;
        } else {
            state.workbenchTodoLoaded = reset ? (page.items || []).length : state.workbenchTodoLoaded + (page.items || []).length;
            els.todoCount.textContent = page.total;
        }
        WorkbenchPanel.updateMoreButtons();
        return page;
    },
    async loadMore(status) {
        const isDone = status === "DONE";
        const allowed = isDone
            ? (Permissions.has("SALARY_DONE") || Permissions.has("APPLICATION_DONE"))
            : (Permissions.has("SALARY_TODO") || Permissions.has("APPLICATION_TODO"));
        if (!allowed) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        const button = isDone ? els.loadMoreDoneButton : els.loadMoreTodoButton;
        button.disabled = true;
        setStatus(TEXT.loadingMore);
        const page = await WorkbenchPanel.loadPage(status, false);
        if (!(page.items || []).length) {
            setStatus(TEXT.noMoreItems);
            return;
        }
        setStatus(TEXT.workbenchReady);
    },
    exportItems(status) {
        const isDone = status === "DONE";
        const statusAllowed = isDone
            ? (Permissions.has("SALARY_DONE") || Permissions.has("APPLICATION_DONE"))
            : (Permissions.has("SALARY_TODO") || Permissions.has("APPLICATION_TODO"));
        if (!Permissions.has("SALARY_EXPORT") || !statusAllowed) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        const filters = WorkbenchPanel.filters();
        const params = new URLSearchParams({
            status,
            keyword: filters.keyword,
            changeType: filters.changeType,
            caseStatus: isDone ? filters.caseStatus : "",
            trialStatus: isDone ? filters.trialStatus : "",
            reviewStatus: isDone ? filters.reviewStatus : "",
            limit: 5000
        });
        window.location.href = `/api/workbench/items.csv?${params.toString()}`;
    },
    workItemRequest(item) {
        return {
            workItemId: item.dataset.workId,
            source: item.dataset.source,
            businessType: item.dataset.changeType,
            personCode: item.dataset.personCode,
            personName: item.dataset.personName,
            orgCode: item.dataset.orgCode,
            year: Number(item.dataset.year || 0) || null,
            month: Number(item.dataset.month || 0) || null,
            title: item.dataset.title,
            summary: item.dataset.summary
        };
    },
    async completeWorkItem(button) {
        const item = button.closest(".work-item");
        if (!item) {
            return;
        }
        button.disabled = true;
        try {
            setStatus(TEXT.previewingWorkItem);
            const request = WorkbenchPanel.workItemRequest(item);
            const preview = await Api.request("/api/workbench/salary-cases/preview", {
                method: "POST",
                body: JSON.stringify(request)
            });
            WorkbenchPanel.showCasePreview(preview, request);
            setStatus(TEXT.casePreview);
        } catch (error) {
            setStatus(error.message);
        } finally {
            button.disabled = false;
        }
    },
    async confirmCompleteCase(request) {
        setStatus(TEXT.completingWorkItem);
        const completed = await Api.request("/api/workbench/salary-cases", {
            method: "POST",
            body: JSON.stringify(request)
        });
        document.querySelector(".case-detail-overlay")?.remove();
        await WorkbenchPanel.load();
        if (completed?.id) {
            await WorkbenchPanel.openCaseDetail(completed.id);
            return;
        }
        setStatus(TEXT.workItemCompleted);
    },
    async openCaseDetail(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const detail = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}`);
        WorkbenchPanel.showCaseDetail(detail);
        setStatus(TEXT.caseDetail);
    },
    async cancelCase(caseNo, cancelReason) {
        setStatus(TEXT.cancellingWorkItem);
        await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/cancel`, {
            method: "POST",
            body: JSON.stringify({ cancelReason })
        });
        document.querySelector(".case-detail-overlay")?.remove();
        await WorkbenchPanel.load();
        setStatus(TEXT.workItemCancelled);
    },
    async reviewCase(caseNo, reviewReason) {
        setStatus(TEXT.reviewingWorkItem);
        await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/review`, {
            method: "POST",
            body: JSON.stringify({ reviewReason })
        });
        document.querySelector(".case-detail-overlay")?.remove();
        await WorkbenchPanel.load();
        setStatus(TEXT.workItemReviewed);
    },
    async openCaseSnapshot(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const snapshot = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/snapshot`);
        WorkbenchPanel.showSnapshotDetail(snapshot);
        setStatus(TEXT.caseDetail);
    },
    async openHistoryWritePreview(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const preview = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-preview`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWritePreview(preview);
        setStatus(TEXT.caseDetail);
    },
    async executeHistoryWrite(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const result = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-execute`, {
            method: "POST"
        });
        await WorkbenchPanel.load();
        WorkbenchPanel.showHistoryWriteExecuteResult(result);
        setStatus(TEXT.caseDetail);
    },
    async rollbackHistoryWrite(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const result = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-rollback`, {
            method: "POST"
        });
        await WorkbenchPanel.load();
        WorkbenchPanel.showHistoryWriteExecuteResult(result);
        setStatus(TEXT.caseDetail);
    },
    async openHistoryWritePlan(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const [plan, audits] = await Promise.all([
            Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-plan`),
            Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-audits`)
        ]);
        WorkbenchPanel.showHistoryWritePlan({ ...plan, audits });
        setStatus(TEXT.caseDetail);
    },
    async openHistoryWriteComparison(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const comparison = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-comparison`);
        WorkbenchPanel.showHistoryWriteComparison(comparison);
        setStatus(TEXT.caseDetail);
    },
    async retestHistoryWriteComparison(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const comparison = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-comparison-retest`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteComparison(comparison, true);
        await WorkbenchPanel.load();
        setStatus(TEXT.caseDetail);
    },
    async approveRetestPassedHistoryWriteComparison(caseNo) {
        setStatus(TEXT.loadingCaseDetail);
        const comparison = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-comparison-retest-approve`, {
            method: "POST"
        });
        WorkbenchPanel.showHistoryWriteComparison(comparison);
        await WorkbenchPanel.loadHistoryWritePlans();
        setStatus(TEXT.caseDetail);
    },
    async reviewHistoryWriteComparison(caseNo, reviewCategory, reviewReason) {
        setStatus(TEXT.loadingCaseDetail);
        const comparison = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(caseNo)}/history-write-comparison-review`, {
            method: "POST",
            body: JSON.stringify({ reviewCategory, reviewReason })
        });
        WorkbenchPanel.showHistoryWriteComparison(comparison);
        await WorkbenchPanel.loadHistoryWritePlans();
        setStatus(TEXT.caseDetail);
    },
    historyWriteFieldSuggestion(field, comparison = {}) {
        const code = String(field?.itemCode || field?.historyField || "").toUpperCase();
        const name = String(field?.itemName || "");
        const businessType = String(comparison?.businessType || "");
        const combined = `${code} ${name} ${businessType}`;
        if (/(\u8003\u6838|\u6b63\u5e38|\u664b\u6863|\u664b\u5347|\u85aa\u7ea7|\u7ea7\u522b\u664b\u5347)/.test(combined)) {
            return { target: "assessment", label: "\u8003\u6838", reason: "\u5e74\u5ea6\u8003\u6838\u6216\u6b63\u5e38\u664b\u6863/\u664b\u5347\u6761\u4ef6\u53ef\u80fd\u5f71\u54cd\u8be5\u9879" };
        }
        if (/(\u5b66\u5386|\u5b66\u4f4d|\u6bd5\u4e1a|\u89c1\u4e60|\u8bd5\u7528|\u8f6c\u6b63|\u5b9a\u7ea7|\u65b0\u8fdb)/.test(combined)) {
            return { target: "education", label: "\u5b66\u5386", reason: "\u5b66\u5386\u3001\u89c1\u4e60\u671f\u6216\u8f6c\u6b63\u5b9a\u7ea7\u53ef\u80fd\u5f71\u54cd\u8be5\u9879" };
        }
        if (/ZWGZ|JBGZ|DJGZ|XJGZ|ZWGZSE|(\u804c\u52a1|\u5c97\u4f4d|\u7ea7\u522b|\u6863\u6b21|\u7b49\u7ea7|\u85aa\u7ea7|\u804c\u7ea7|\u6cd5\u5b98|\u68c0\u5bdf\u5b98|\u8b66\u8854|\u6280\u672f\u7b49\u7ea7|\u6280\u672f\u804c\u52a1)/.test(combined)) {
            return { target: "post", label: "\u4efb\u804c", reason: "\u804c\u52a1\u3001\u5c97\u4f4d\u3001\u7ea7\u522b\u6216\u6863\u6b21\u4fe1\u606f\u53ef\u80fd\u5f71\u54cd\u8be5\u9879" };
        }
        if (/GLGZ|JHL|(\u6559\u62a4\u9f84|\u62a4\u9f84|\u5de5\u9f84|\u5e74\u9650|\u53c2\u52a0\u5de5\u4f5c|\u5957\u6539|\u4efb\u804c\u5e74\u9650|\u5de5\u4f5c\u65f6\u95f4)/.test(combined)) {
            return { target: "base", label: "\u57fa\u672c", reason: "\u53c2\u52a0\u5de5\u4f5c\u3001\u6559\u62a4\u9f84\u6216\u5e74\u9650\u7c7b\u57fa\u7840\u4fe1\u606f\u53ef\u80fd\u5f71\u54cd\u8be5\u9879" };
        }
        if (/(\u6d25\u8d34|\u8865\u8d34|\u6807\u51c6|\u5730\u533a|\u8270\u82e6|\u5c71\u533a|\u4fdd\u7559|\u7269\u4e1a|\u4f4f\u623f|\u4ea4\u901a|\u901a\u4fe1|\u5c97\u4f4d\u6d25\u8d34|\u751f\u6d3b\u8865\u8d34)/.test(combined)) {
            return { target: "standard", label: "\u6807\u51c6/\u6d25\u8865\u8d34", reason: "\u53ef\u80fd\u4e0e\u6267\u884c\u6807\u51c6\u3001\u6d25\u8865\u8d34\u6807\u51c6\u6216\u624b\u5de5\u503c\u6709\u5173" };
        }
        return { target: "other", label: "\u5176\u4ed6", reason: "\u6682\u672a\u80fd\u6839\u636e\u5b57\u6bb5\u81ea\u52a8\u5224\u65ad\uff0c\u5efa\u8bae\u7ed3\u5408\u4e1a\u52a1\u7c7b\u578b\u548c\u5386\u53f2\u6d41\u6c34\u6838\u67e5" };
    },
    historyWriteReviewSuggestion(group) {
        const category = {
            base: "BASE_CHANGED",
            post: "BASE_CHANGED",
            education: "BASE_CHANGED",
            assessment: "BASE_CHANGED",
            standard: "POLICY_DIFF",
            other: "OTHER"
        }[group.target] || "OTHER";
        const fields = (group.fields || []).slice(0, 6).join("\u3001");
        return {
            category,
            reason: `\u6309\u5efa\u8bae\u68c0\u67e5\u65b9\u5411\u767b\u8bb0\uff1a${group.label}\uff1b${group.reason}\uff1b\u6d89\u53ca\u5b57\u6bb5\uff1a${fields}${(group.fields || []).length > 6 ? "\u7b49" : ""}`
        };
    },
    historyWriteSuggestionGroups(comparison, fields) {
        const groups = new Map();
        for (const field of fields || []) {
            const suggestion = WorkbenchPanel.historyWriteFieldSuggestion(field, comparison);
            const group = groups.get(suggestion.target) || { ...suggestion, count: 0, fields: [] };
            group.count += 1;
            group.fields.push(field.itemName || field.itemCode || field.historyField || "-");
            groups.set(suggestion.target, group);
        }
        return Array.from(groups.values()).sort((a, b) => b.count - a.count);
    },
    historyWriteSuggestionSummaryHtml(comparison, fields) {
        const groups = WorkbenchPanel.historyWriteSuggestionGroups(comparison, fields);
        if (!groups.length) {
            return "";
        }
        return `
            <div class="case-detail-section">
                <span>\u5efa\u8bae\u68c0\u67e5\u65b9\u5411</span>
                <div class="case-suggestion-list">
                    ${groups.map((group) => {
                        const reviewSuggestion = WorkbenchPanel.historyWriteReviewSuggestion(group);
                        return `
                        <div class="case-suggestion-card">
                            <strong>${Format.html(group.label)} <em>${Format.html(group.count)}</em></strong>
                            <small>${Format.html(group.reason)}</small>
                            <span>${Format.html(group.fields.slice(0, 4).join("\u3001"))}${group.fields.length > 4 ? "\u7b49" : ""}</span>
                            ${["base", "post", "education", "assessment"].includes(group.target)
                                ? `<button type="button" data-open-person-maintenance="${Format.html(group.target)}" data-person-code="${Format.html(comparison.personCode || "")}" data-maintenance-case-no="${Format.html(comparison.caseNo || "")}" data-maintenance-label="${Format.html(group.label || "")}" data-maintenance-reason="${Format.html(group.reason || "")}" data-maintenance-fields="${Format.html(group.fields.join("\u3001"))}">\u6253\u5f00${Format.html(group.label)}</button>`
                                : ""}
                            ${comparison.reviewStatus === "REVIEWED"
                                ? ""
                                : `<button type="button" data-history-review-suggestion-category="${Format.html(reviewSuggestion.category)}" data-history-review-suggestion-reason="${Format.html(reviewSuggestion.reason)}">\u5e26\u5165\u6838\u67e5</button>`}
                        </div>
                    `}).join("")}
                </div>
            </div>
        `;
    },
    showCaseDetail(detail) {
        const canCancel = detail.status === "DONE" && Permissions.has("SALARY_TODO") && Permissions.has("SALARY_DONE");
        const canReview = detail.status === "DONE"
            && ["DIFFERENT", "ERROR"].includes(detail.trialStatus || "")
            && detail.reviewStatus !== "REVIEWED"
            && Permissions.has("SALARY_DONE");
        const reviewReasonHtml = canReview
            ? `<label class="case-review-reason"><span>\u590d\u6838\u8bf4\u660e</span><textarea data-review-reason rows="3" maxlength="1000" placeholder="\u8bf7\u8bf4\u660e\u8be5\u5dee\u5f02\u6216\u5f02\u5e38\u5df2\u590d\u6838\u7684\u4f9d\u636e"></textarea></label>`
            : "";
        const cancelReasonHtml = canCancel
            ? `<label class="case-cancel-reason"><span>\u64a4\u56de\u529e\u7406\u8bf4\u660e</span><textarea data-cancel-reason rows="3" maxlength="1000" placeholder="\u8bf7\u8bf4\u660e\u64a4\u56de\u8be5\u529e\u7406\u8bb0\u5f55\u7684\u539f\u56e0"></textarea></label>`
            : "";
        const historyWriteAction = detail.snapshotExists
            ? (detail.historyWritePlan
                ? `<button type="button" class="case-snapshot-button" data-history-write-plan-case-no="${Format.html(detail.caseNo)}">\u5199\u5165\u8ba1\u5212</button>`
                : `<button type="button" class="case-snapshot-button" data-history-write-preview-case-no="${Format.html(detail.caseNo)}">\u751f\u6210\u5199\u5165\u9884\u68c0</button>`)
            : "";
        const actionButtons = [
            detail.snapshotExists ? `<button type="button" class="case-snapshot-button" data-snapshot-case-no="${Format.html(detail.caseNo)}">\u67e5\u770b\u5feb\u7167</button>` : "",
            historyWriteAction,
            canReview ? `<button type="button" class="case-review-button" data-review-case-no="${Format.html(detail.caseNo)}">\u6807\u8bb0\u5df2\u590d\u6838</button>` : "",
            canCancel ? `<button type="button" class="case-cancel-button" data-cancel-case-no="${Format.html(detail.caseNo)}">\u64a4\u56de\u529e\u7406</button>` : ""
        ].filter(Boolean).join("");
        WorkbenchPanel.showCaseWindow({
            title: TEXT.caseDetail,
            detail,
            fields: WorkbenchPanel.caseFields(detail),
            warningHtml: `${reviewReasonHtml}${cancelReasonHtml}`,
            actionsHtml: actionButtons
        });
    },
    showSnapshotDetail(snapshot) {
        const fields = [
            ["\u529e\u7406\u7f16\u53f7", snapshot.caseNo],
            ["\u5f85\u529e\u6807\u8bc6", snapshot.workItemId],
            ["\u4e1a\u52a1\u7c7b\u578b", snapshot.businessType],
            ["\u4eba\u5458\u7f16\u7801", snapshot.personCode],
            ["\u5355\u4f4d\u7f16\u7801", snapshot.orgCode],
            ["\u6267\u884c\u5e74\u6708", snapshot.year ? `${snapshot.year}-${String(snapshot.month || 1).padStart(2, "0")}` : "-"],
            ["\u5feb\u7167\u751f\u6210\u4eba", snapshot.snapshotBy],
            ["\u5feb\u7167\u65f6\u95f4", snapshot.snapshotAt]
        ];
        const salaryItems = snapshot.salaryItems || [];
        const salaryItemsHtml = salaryItems.length
            ? `<div class="case-detail-section"><span>\u5b8c\u6574\u5de5\u8d44\u9879</span><div class="case-change-list">
                ${salaryItems.map((item) => `
                    <div class="case-change-row">
                        <strong>${Format.html(item.itemCode || "-")} ${Format.html(item.itemName || "")}</strong>
                        <span>${Format.optionalAmount(item.amount)}</span>
                        <small>${Format.html(item.ruleNote || "-")}</small>
                    </div>
                `).join("")}
            </div></div>`
            : "";
        const snapshotJsonHtml = snapshot.snapshotJson
            ? `<div class="case-detail-section case-snapshot-json"><span>\u5feb\u7167JSON</span><pre>${Format.html(snapshot.snapshotJson)}</pre></div>`
            : "";
        WorkbenchPanel.showCaseWindow({
            title: "\u529e\u7406\u7ed3\u679c\u5feb\u7167",
            detail: {
                ...snapshot,
                source: "SNAPSHOT",
                status: "DONE",
                title: "\u529e\u7406\u7ed3\u679c\u5feb\u7167",
                summary: "\u5feb\u7167\u5df2\u56fa\u5316\uff0c\u53ef\u7528\u4e8e\u540e\u7eed\u5199\u5165\u5386\u53f2\u5de5\u8d44\u524d\u6838\u5bf9\u3002",
                trialSummary: "\u5feb\u7167\u5df2\u56fa\u5316\uff0c\u53ef\u7528\u4e8e\u540e\u7eed\u5199\u5165\u5386\u53f2\u5de5\u8d44\u524d\u6838\u5bf9\u3002"
            },
            fields,
            warningHtml: `${salaryItemsHtml}${snapshotJsonHtml}`,
            actionsHtml: `<button type="button" class="case-snapshot-button" data-history-write-preview-case-no="${Format.html(snapshot.caseNo)}">\u5386\u53f2\u5199\u5165\u9884\u89c8</button>`
        });
    },
    historyRowHtml(label, row) {
        if (!row) {
            return `
                <div class="case-history-row">
                    <strong>${Format.html(label)}</strong>
                    <span class="muted">\u65e0</span>
                </div>
            `;
        }
        const period = row.year ? `${row.year}-${String(row.month || 1).padStart(2, "0")}` : "-";
        return `
            <div class="case-history-row">
                <strong>${Format.html(label)}</strong>
                <span>${Format.html(row.historyId || "-")}</span>
                <small>${Format.html(period)} | ${Format.html(row.changeType || "-")} | \u5408\u8ba1 ${Format.optionalAmount(row.totalAmount)} | sid ${Format.html(row.nextId || "-")}</small>
            </div>
        `;
    },
    showHistoryWritePreview(preview) {
        document.querySelector(".case-detail-overlay")?.remove();
        const fields = preview.fields || [];
        const issues = preview.issues || [];
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u9884\u89c8">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u9884\u89c8</strong>
                        <span>${Format.html(preview.businessType || "-")} | ${Format.html(preview.personCode || "-")}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <dl class="case-detail-grid">
                    ${[
                        ["\u529e\u7406\u7f16\u53f7", preview.caseNo],
                        ["\u5199\u5165\u8ba1\u5212\u53f7", preview.writePlanId],
                        ["\u5f85\u529e\u6807\u8bc6", preview.workItemId],
                        ["\u5199\u5165\u72b6\u6001", preview.status],
                        ["\u662f\u5426\u53ef\u5199", preview.writable ? "\u662f" : "\u5426"],
                        ["\u4eba\u5458\u7f16\u7801", preview.personCode],
                        ["\u5355\u4f4d\u7f16\u7801", preview.orgCode],
                        ["\u6267\u884c\u5e74\u6708", preview.year ? `${preview.year}-${String(preview.month || 1).padStart(2, "0")}` : "-"],
                        ["\u53d8\u52a8\u7c7b\u522b", preview.businessType],
                        ["\u5df2\u6709\u5386\u53f2", preview.existingHistoryId || "-"]
                    ].map(([label, value]) => `
                        <div>
                            <dt>${Format.html(label)}</dt>
                            <dd>${Format.html(value || "-")}</dd>
                        </div>
                    `).join("")}
                </dl>
                <div class="case-history-status ${Format.html((preview.status || "").toLowerCase())}">
                    <strong>${Format.html(preview.status || "-")}</strong>
                    <span>${preview.writable ? "\u53ef\u8fdb\u5165\u5199\u5165\u786e\u8ba4\u524d\u590d\u6838" : "\u5b58\u5728\u963b\u65ad\u9879\uff0c\u6682\u4e0d\u5efa\u8bae\u5199\u5165"}</span>
                </div>
                <div class="case-detail-section">
                    <span>sid \u8c03\u6574\u9884\u6848</span>
                    <p>${Format.html(preview.sidPlan || "-")}</p>
                </div>
                <div class="case-detail-section">
                    <span>\u5386\u53f2\u94fe\u4f4d\u7f6e</span>
                    <div class="case-history-list">
                        ${WorkbenchPanel.historyRowHtml("\u524d\u4e00\u6761", preview.previousHistory)}
                        ${WorkbenchPanel.historyRowHtml("\u540e\u4e00\u6761", preview.nextHistory)}
                    </div>
                </div>
                <div class="case-detail-section">
                    <span>\u5b57\u6bb5\u6620\u5c04</span>
                    <div class="case-history-field-list">
                        ${fields.length ? fields.map((field) => `
                            <div class="case-history-field ${field.mapped ? "" : "blocked"}">
                                <strong>${Format.html(field.itemName || field.itemCode || "-")}</strong>
                                <span>${Format.html(field.historyField || "\u672a\u6620\u5c04")}</span>
                                <small>${Format.html(field.itemCode || "-")} | \u91d1\u989d ${Format.optionalAmount(field.amount)}${field.issue ? ` | ${Format.html(field.issue)}` : ""}</small>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u6620\u5c04\u5b57\u6bb5</p>`}
                    </div>
                </div>
                <div class="case-detail-section">
                    <span>\u95ee\u9898\u548c\u63d0\u793a</span>
                    <div class="case-history-issue-list">
                        ${issues.length ? issues.map((issue) => `<p>${Format.html(issue)}</p>`).join("") : `<p class="muted">\u672a\u53d1\u73b0\u963b\u65ad\u6216\u8b66\u544a</p>`}
                    </div>
                </div>
                <div class="case-error hidden" data-case-error></div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-history-write-plan-case-no="${Format.html(preview.caseNo)}">\u67e5\u770b\u5199\u5165\u8ba1\u5212</button>
                    ${preview.writable ? `<button type="button" class="case-confirm-button" data-history-write-execute-case-no="${Format.html(preview.caseNo)}">\u786e\u8ba4\u5199\u5165\u5386\u53f2</button>` : ""}
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const planButton = event.target.closest("button[data-history-write-plan-case-no]");
            if (planButton) {
                planButton.disabled = true;
                try {
                    await WorkbenchPanel.openHistoryWritePlan(planButton.dataset.historyWritePlanCaseNo);
                } catch (error) {
                    planButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const executeButton = event.target.closest("button[data-history-write-execute-case-no]");
            if (executeButton) {
                const caseNo = executeButton.dataset.historyWriteExecuteCaseNo;
                if (!window.confirm(Format.text(TEXT.historyWriteExecuteConfirm, { caseNo }))) {
                    return;
                }
                executeButton.disabled = true;
                try {
                    await WorkbenchPanel.executeHistoryWrite(caseNo);
                } catch (error) {
                    executeButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWritePlan(plan) {
        document.querySelector(".case-detail-overlay")?.remove();
        const audits = plan.audits || [];
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._auditExportUrl = `/api/workbench/salary-cases/${encodeURIComponent(plan.caseNo || "")}/history-write-audits.csv`;
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u8ba1\u5212">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u8ba1\u5212</strong>
                        <span>${Format.html(plan.personCode || "-")} | ${Format.html(plan.planStatus || "-")}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <dl class="case-detail-grid">
                    ${[
                        ["\u5199\u5165\u8ba1\u5212\u53f7", plan.planNo],
                        ["\u529e\u7406\u7f16\u53f7", plan.caseNo],
                        ["\u5f85\u529e\u6807\u8bc6", plan.workItemId],
                        ["\u4eba\u5458\u7f16\u7801", plan.personCode],
                        ["\u5355\u4f4d\u7f16\u7801", plan.orgCode],
                        ["\u6267\u884c\u5e74\u6708", plan.year ? `${plan.year}-${String(plan.month || 1).padStart(2, "0")}` : "-"],
                        ["\u53d8\u52a8\u7c7b\u522b", plan.businessType],
                        ["\u9884\u89c8\u72b6\u6001", plan.previewStatus],
                        ["\u8ba1\u5212\u72b6\u6001", plan.planStatus],
                        ["\u6267\u884c\u7ed3\u679c", plan.executionResult || "-"],
                        ["\u5199\u5165\u5386\u53f2ID", plan.insertedHistoryId || "-"],
                        ["\u524d\u4e00\u6761ID", plan.previousHistoryId || "-"],
                        ["\u540e\u4e00\u6761ID", plan.nextHistoryId || "-"],
                        ["\u751f\u6210\u4eba", plan.preparedBy || "-"],
                        ["\u751f\u6210\u65f6\u95f4", plan.preparedAt || "-"],
                        ["\u6267\u884c\u4eba", plan.executedBy || "-"],
                        ["\u6267\u884c\u65f6\u95f4", plan.executedAt || "-"],
                        ["\u64a4\u9500\u4eba", plan.rolledBackBy || "-"],
                        ["\u64a4\u9500\u65f6\u95f4", plan.rolledBackAt || "-"]
                    ].map(([label, value]) => `
                        <div>
                            <dt>${Format.html(label)}</dt>
                            <dd>${Format.html(value || "-")}</dd>
                        </div>
                    `).join("")}
                </dl>
                <div class="case-detail-section">
                    <span>\u6267\u884c\u8bf4\u660e</span>
                    <p>${Format.html(plan.executionMessage || plan.rollbackMessage || "-")}</p>
                </div>
                <div class="case-detail-section case-snapshot-json">
                    <span>\u95ee\u9898\u548c\u9884\u89c8JSON</span>
                    <pre>${Format.html(plan.issuesJson || plan.previewJson || "-")}</pre>
                </div>
                <div class="case-detail-section">
                    <span>\u5199\u5165\u6d41\u6c34</span>
                    <div class="case-audit-list">
                        ${audits.length ? audits.map((audit) => `
                            <div class="case-audit-row">
                                <strong>${Format.html(Format.auditActionText(audit.action))}</strong>
                                <small>${Format.html(audit.summary || "-")}</small>
                                <span>${Format.html(audit.operator || "-")} | ${Format.html(audit.createdAt || "-")}</span>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u5199\u5165\u6d41\u6c34</p>`}
                    </div>
                </div>
                <footer class="case-detail-actions">
                    ${plan.planStatus !== "EXECUTED" && plan.writable ? `<button type="button" class="case-confirm-button" data-history-write-execute-case-no="${Format.html(plan.caseNo)}">\u786e\u8ba4\u5199\u5165\u5386\u53f2</button>` : ""}
                    ${plan.planStatus === "EXECUTED" && plan.executionResult === "SUCCESS" ? `<button type="button" class="case-cancel-button" data-history-write-rollback-case-no="${Format.html(plan.caseNo)}">\u64a4\u9500\u5199\u5165</button>` : ""}
                    <button type="button" class="case-snapshot-button" data-history-write-comparison-case-no="${Format.html(plan.caseNo)}">\u5b57\u6bb5\u5bf9\u7167</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="base" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u57fa\u672c\u4fe1\u606f</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="post" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u4efb\u804c</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="education" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u5b66\u5386</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="assessment" data-person-code="${Format.html(plan.personCode || "")}" data-maintenance-case-no="${Format.html(plan.caseNo || "")}">\u8003\u6838</button>
                    <button type="button" class="case-snapshot-button" data-history-write-audit-export ${audits.length ? "" : "disabled"}>\u5bfc\u51fa\u6d41\u6c34</button>
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const auditExportButton = event.target.closest("button[data-history-write-audit-export]");
            if (auditExportButton && !auditExportButton.disabled) {
                window.location.href = overlay._auditExportUrl;
                return;
            }
            const comparisonButton = event.target.closest("button[data-history-write-comparison-case-no]");
            if (comparisonButton) {
                WorkbenchPanel.openHistoryWriteComparison(comparisonButton.dataset.historyWriteComparisonCaseNo);
                return;
            }
            const executeButton = event.target.closest("button[data-history-write-execute-case-no]");
            if (executeButton) {
                const caseNo = executeButton.dataset.historyWriteExecuteCaseNo;
                if (!window.confirm(Format.text(TEXT.historyWriteExecuteConfirm, { caseNo }))) {
                    return;
                }
                executeButton.disabled = true;
                try {
                    await WorkbenchPanel.executeHistoryWrite(caseNo);
                } catch (error) {
                    executeButton.disabled = false;
                    setStatus(error.message);
                }
                return;
            }
            const rollbackButton = event.target.closest("button[data-history-write-rollback-case-no]");
            if (rollbackButton) {
                const caseNo = rollbackButton.dataset.historyWriteRollbackCaseNo;
                if (!window.confirm(Format.text(TEXT.historyWriteRollbackConfirm, { caseNo }))) {
                    return;
                }
                rollbackButton.disabled = true;
                try {
                    await WorkbenchPanel.rollbackHistoryWrite(caseNo);
                } catch (error) {
                    rollbackButton.disabled = false;
                    setStatus(error.message);
                }
                return;
            }
            const maintenanceButton = event.target.closest("button[data-open-person-maintenance]");
            if (maintenanceButton) {
                maintenanceButton.disabled = true;
                try {
                    await WorkbenchPanel.openPersonMaintenance(maintenanceButton.dataset.personCode, maintenanceButton.dataset.openPersonMaintenance, {
                        caseNo: maintenanceButton.dataset.maintenanceCaseNo,
                        label: maintenanceButton.dataset.maintenanceLabel,
                        reason: maintenanceButton.dataset.maintenanceReason,
                        fields: maintenanceButton.dataset.maintenanceFields
                    });
                } catch (error) {
                    maintenanceButton.disabled = false;
                    setStatus(error.message);
                }
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWriteComparison(comparison, retested = false) {
        document.querySelector(".case-detail-overlay")?.remove();
        const fields = comparison.fields || [];
        const mismatchedFields = fields.filter((field) => !field.matched);
        const suggestionHtml = WorkbenchPanel.historyWriteSuggestionSummaryHtml(comparison, mismatchedFields);
        const hasMismatch = Boolean(mismatchedFields.length || !comparison.totalMatched);
        const reviewed = comparison.reviewStatus === "REVIEWED";
        const retestWorkflowHtml = reviewed
            ? `<div class="case-history-status ready">
                <strong>\u5df2\u5b8c\u6210\u6838\u67e5</strong>
                <span>\u8be5\u8ba1\u5212\u5df2\u767b\u8bb0\u6838\u67e5\u7ed3\u8bba\uff0c\u53ef\u4f5c\u4e3a\u540e\u7eed\u8ffd\u6eaf\u4f9d\u636e\u3002</span>
            </div>`
            : (retested
                ? (!hasMismatch
                    ? `<div class="case-history-status ready">
                        <strong>\u590d\u6d4b\u4e00\u81f4</strong>
                        <span>\u5f53\u524d\u57fa\u7840\u4fe1\u606f\u5df2\u4e0ehisbase\u4e00\u81f4\uff0c\u53ef\u70b9\u51fb\u201c\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7\u201d\u5173\u95ed\u5f85\u6838\u67e5\u3002</span>
                    </div>`
                    : `<div class="case-history-status warning">
                        <strong>\u590d\u6d4b\u4ecd\u6709\u5dee\u5f02</strong>
                        <span>\u53ef\u6309\u5efa\u8bae\u65b9\u5411\u7ee7\u7eed\u7ef4\u62a4\u57fa\u7840\u4fe1\u606f\uff1b\u5982\u5c5e\u5386\u53f2\u7279\u6b8a\u6216\u624b\u5de5\u503c\uff0c\u586b\u5199\u6838\u67e5\u8bf4\u660e\u540e\u6807\u8bb0\u5df2\u6838\u67e5\u3002</span>
                    </div>`)
                : (hasMismatch
                    ? `<div class="case-history-status warning">
                        <strong>\u5efa\u8bae\u5148\u590d\u6d4b</strong>
                        <span>\u5728\u7ef4\u62a4\u57fa\u7840\u4fe1\u606f\u540e\uff0c\u5148\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b\uff0c\u518d\u5224\u65ad\u662f\u6807\u8bb0\u901a\u8fc7\u8fd8\u662f\u7ee7\u7eed\u6838\u67e5\u3002</span>
                    </div>`
                    : ""));
        const reviewHtml = hasMismatch
            ? (reviewed
                ? `<div class="case-history-status ready">
                    <strong>\u5df2\u6838\u67e5</strong>
                    <span>${Format.html(comparison.reviewedBy || "-")} | ${Format.html(comparison.reviewedAt || "-")} | ${Format.html(Format.historyWriteReviewCategoryText(comparison.reviewCategory))} | ${Format.html(comparison.reviewReason || "-")}</span>
                </div>`
                : `<div class="case-review-reason">
                    <label>
                        <span>\u6838\u67e5\u5206\u7c7b</span>
                        <select data-history-write-review-category>
                            <option value="">\u8bf7\u9009\u62e9</option>
                            <option value="BASE_MISSING">\u57fa\u7840\u4fe1\u606f\u7f3a\u5931</option>
                            <option value="BASE_CHANGED">\u57fa\u7840\u4fe1\u606f\u5df2\u53d8\u66f4</option>
                            <option value="POLICY_DIFF">\u653f\u7b56\u53d6\u503c\u5dee\u5f02</option>
                            <option value="MANUAL_INPUT">\u624b\u5de5\u5f55\u5165</option>
                            <option value="HISTORY_SPECIAL">\u5386\u53f2\u7279\u6b8a\u5904\u7406</option>
                            <option value="OTHER">\u5176\u4ed6</option>
                        </select>
                    </label>
                    <label>
                        <span>\u6838\u67e5\u8bf4\u660e</span>
                        <textarea data-history-write-review-reason rows="3" maxlength="1000" placeholder="\u8bf7\u8bf4\u660e\u8be5\u5199\u5165\u5dee\u5f02\u5df2\u6838\u67e5\u7684\u4f9d\u636e"></textarea>
                    </label>
                </div>`)
            : "";
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._mismatchExport = {
            filename: `history-write-mismatches-${comparison.caseNo || "case"}.csv`,
            rows: WorkbenchPanel.historyWriteMismatchCsvRows(comparison, mismatchedFields)
        };
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u5b57\u6bb5\u5bf9\u7167">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u5b57\u6bb5\u5bf9\u7167</strong>
                        <span>${Format.html(comparison.personCode || "-")} | ${Format.html(Format.historyWriteStatusText(comparison.planStatus))}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <dl class="case-detail-grid">
                    ${[
                        ["\u529e\u7406\u7f16\u53f7", comparison.caseNo],
                        ["\u5199\u5165\u8ba1\u5212\u53f7", comparison.planNo],
                        ["\u4eba\u5458\u7f16\u7801", comparison.personCode],
                        ["\u5355\u4f4d\u7f16\u7801", comparison.orgCode],
                        ["\u6267\u884c\u5e74\u6708", comparison.year ? `${comparison.year}-${String(comparison.month || 1).padStart(2, "0")}` : "-"],
                        ["\u53d8\u52a8\u7c7b\u522b", comparison.businessType],
                        ["\u8ba1\u5212\u72b6\u6001", Format.historyWriteStatusText(comparison.planStatus)],
                        ["\u6267\u884c\u7ed3\u679c", Format.historyWriteStatusText(comparison.executionResult)],
                        ["\u5199\u5165\u5386\u53f2ID", comparison.insertedHistoryId || "-"],
                        ["\u9884\u671f\u5408\u8ba1", Format.optionalAmount(comparison.expectedTotal)],
                        ["hisbase\u5408\u8ba1", Format.optionalAmount(comparison.actualTotal)],
                        ["\u5408\u8ba1\u662f\u5426\u4e00\u81f4", comparison.totalMatched ? "\u4e00\u81f4" : "\u4e0d\u4e00\u81f4"]
                    ].map(([label, value]) => `
                        <div>
                            <dt>${Format.html(label)}</dt>
                            <dd>${Format.html(value || "-")}</dd>
                        </div>
                    `).join("")}
                </dl>
                <div class="case-history-status ${hasMismatch ? "blocked" : "ready"}">
                    <strong>${retested ? "\u5f53\u524d\u57fa\u7840\u590d\u6d4b" : (hasMismatch ? "\u5b58\u5728\u5dee\u5f02" : "\u5bf9\u7167\u4e00\u81f4")}</strong>
                    <span>\u5b57\u6bb5\u5dee\u5f02 ${Format.html(mismatchedFields.length)} | \u5408\u8ba1${comparison.totalMatched ? "\u4e00\u81f4" : "\u4e0d\u4e00\u81f4"}</span>
                </div>
                ${retestWorkflowHtml}
                ${reviewHtml}
                ${suggestionHtml}
                <div class="case-detail-section">
                    <span>\u5386\u53f2\u94fe</span>
                    <div class="case-change-list">
                        ${WorkbenchPanel.historyRowHtml("\u524d\u4e00\u6761", comparison.previousHistory)}
                        ${WorkbenchPanel.historyRowHtml("\u540e\u4e00\u6761", comparison.nextHistory)}
                    </div>
                </div>
                <div class="case-detail-section">
                    <span>\u5de5\u8d44\u9879\u5b57\u6bb5\u5bf9\u7167</span>
                    <div class="case-history-field-list">
                        ${fields.length ? fields.map((field) => `
                            <div class="case-history-field-row ${field.matched ? "matched" : "blocked"}">
                                <strong>${Format.html(field.itemCode || "-")} ${Format.html(field.itemName || "")}</strong>
                                <span>${Format.html(field.historyField || "-")}</span>
                                <small>\u5feb\u7167 ${Format.optionalAmount(field.expectedAmount)} | hisbase ${Format.optionalAmount(field.actualAmount)} | ${field.matched ? "\u4e00\u81f4" : "\u4e0d\u4e00\u81f4"}${field.issue ? ` | ${Format.html(field.issue)}` : ""}${field.matched ? "" : ` | \u5efa\u8bae\u68c0\u67e5\uff1a${Format.html(WorkbenchPanel.historyWriteFieldSuggestion(field, comparison).label)}`}</small>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u5b57\u6bb5\u5bf9\u7167</p>`}
                    </div>
                </div>
                <footer class="case-detail-actions">
                    <div class="case-error hidden" data-case-error></div>
                    <button type="button" class="case-snapshot-button" data-history-write-plan-case-no="${Format.html(comparison.caseNo)}">\u67e5\u770b\u5199\u5165\u8ba1\u5212</button>
                    <button type="button" class="case-snapshot-button" data-history-write-comparison-retest-case-no="${Format.html(comparison.caseNo)}">\u6309\u5f53\u524d\u57fa\u7840\u590d\u6d4b</button>
                    ${retested && !hasMismatch && !reviewed ? `<button type="button" class="case-review-button" data-history-write-retest-approve-case-no="${Format.html(comparison.caseNo)}">\u6807\u8bb0\u590d\u6d4b\u901a\u8fc7</button>` : ""}
                    <button type="button" class="case-snapshot-button" data-history-write-comparison-export-case-no="${Format.html(comparison.caseNo)}">\u5bfc\u51fa\u5bf9\u7167</button>
                    <button type="button" class="case-snapshot-button" data-history-write-mismatch-export ${hasMismatch ? "" : "disabled"}>\u5bfc\u51fa\u5f02\u5e38\u5b57\u6bb5</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="base" data-person-code="${Format.html(comparison.personCode || "")}" data-maintenance-case-no="${Format.html(comparison.caseNo || "")}">\u57fa\u672c\u4fe1\u606f</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="post" data-person-code="${Format.html(comparison.personCode || "")}" data-maintenance-case-no="${Format.html(comparison.caseNo || "")}">\u4efb\u804c</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="education" data-person-code="${Format.html(comparison.personCode || "")}" data-maintenance-case-no="${Format.html(comparison.caseNo || "")}">\u5b66\u5386</button>
                    <button type="button" class="case-snapshot-button" data-open-person-maintenance="assessment" data-person-code="${Format.html(comparison.personCode || "")}" data-maintenance-case-no="${Format.html(comparison.caseNo || "")}">\u8003\u6838</button>
                    ${hasMismatch && !reviewed ? `<button type="button" class="case-review-button" data-history-write-review-case-no="${Format.html(comparison.caseNo)}">\u6807\u8bb0\u5df2\u6838\u67e5</button>` : ""}
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const planButton = event.target.closest("button[data-history-write-plan-case-no]");
            if (planButton) {
                WorkbenchPanel.openHistoryWritePlan(planButton.dataset.historyWritePlanCaseNo);
                return;
            }
            const retestButton = event.target.closest("button[data-history-write-comparison-retest-case-no]");
            if (retestButton) {
                retestButton.disabled = true;
                try {
                    await WorkbenchPanel.retestHistoryWriteComparison(retestButton.dataset.historyWriteComparisonRetestCaseNo);
                } catch (error) {
                    retestButton.disabled = false;
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                    setStatus(error.message);
                }
                return;
            }
            const retestApproveButton = event.target.closest("button[data-history-write-retest-approve-case-no]");
            if (retestApproveButton) {
                retestApproveButton.disabled = true;
                try {
                    await WorkbenchPanel.approveRetestPassedHistoryWriteComparison(retestApproveButton.dataset.historyWriteRetestApproveCaseNo);
                } catch (error) {
                    retestApproveButton.disabled = false;
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                    setStatus(error.message);
                }
                return;
            }
            const exportButton = event.target.closest("button[data-history-write-comparison-export-case-no]");
            if (exportButton) {
                window.location.href = `/api/workbench/salary-cases/${encodeURIComponent(exportButton.dataset.historyWriteComparisonExportCaseNo || "")}/history-write-comparison.csv`;
                return;
            }
            const mismatchExportButton = event.target.closest("button[data-history-write-mismatch-export]");
            if (mismatchExportButton && !mismatchExportButton.disabled) {
                WorkbenchPanel.downloadCsv(overlay._mismatchExport.filename, overlay._mismatchExport.rows);
                return;
            }
            const reviewSuggestionButton = event.target.closest("button[data-history-review-suggestion-category]");
            if (reviewSuggestionButton) {
                const categoryInput = overlay.querySelector("[data-history-write-review-category]");
                const reasonInput = overlay.querySelector("[data-history-write-review-reason]");
                if (categoryInput) {
                    categoryInput.value = reviewSuggestionButton.dataset.historyReviewSuggestionCategory || "";
                    categoryInput.classList.remove("invalid");
                }
                if (reasonInput) {
                    reasonInput.value = reviewSuggestionButton.dataset.historyReviewSuggestionReason || "";
                    reasonInput.classList.remove("invalid");
                    reasonInput.focus();
                }
                WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), "");
                return;
            }
            const maintenanceButton = event.target.closest("button[data-open-person-maintenance]");
            if (maintenanceButton) {
                maintenanceButton.disabled = true;
                try {
                    await WorkbenchPanel.openPersonMaintenance(maintenanceButton.dataset.personCode, maintenanceButton.dataset.openPersonMaintenance, {
                        caseNo: maintenanceButton.dataset.maintenanceCaseNo,
                        label: maintenanceButton.dataset.maintenanceLabel,
                        reason: maintenanceButton.dataset.maintenanceReason,
                        fields: maintenanceButton.dataset.maintenanceFields
                    });
                } catch (error) {
                    maintenanceButton.disabled = false;
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                    setStatus(error.message);
                }
                return;
            }
            const reviewButton = event.target.closest("button[data-history-write-review-case-no]");
            if (reviewButton) {
                const errorBox = overlay.querySelector("[data-case-error]");
                const categoryInput = overlay.querySelector("[data-history-write-review-category]");
                const reasonInput = overlay.querySelector("[data-history-write-review-reason]");
                const category = (categoryInput?.value || "").trim();
                const reason = (reasonInput?.value || "").trim();
                if (!category) {
                    WorkbenchPanel.setCaseError(errorBox, "\u8bf7\u9009\u62e9\u6838\u67e5\u5206\u7c7b\u3002");
                    categoryInput?.classList.add("invalid");
                    categoryInput?.focus();
                    return;
                }
                if (!reason) {
                    WorkbenchPanel.setCaseError(errorBox, "\u8bf7\u586b\u5199\u6838\u67e5\u8bf4\u660e\u3002");
                    reasonInput?.classList.add("invalid");
                    reasonInput?.focus();
                    return;
                }
                categoryInput?.classList.remove("invalid");
                reasonInput?.classList.remove("invalid");
                reviewButton.disabled = true;
                try {
                    await WorkbenchPanel.reviewHistoryWriteComparison(reviewButton.dataset.historyWriteReviewCaseNo, category, reason);
                } catch (error) {
                    reviewButton.disabled = false;
                    WorkbenchPanel.setCaseError(errorBox, error.message);
                    setStatus(error.message);
                }
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    historyWriteMismatchCsvRows(comparison, fields) {
        const rows = [["\u529e\u7406\u7f16\u53f7", "\u5199\u5165\u8ba1\u5212\u53f7", "\u4eba\u5458\u7f16\u7801", "\u5de5\u8d44\u9879\u7f16\u7801", "\u5de5\u8d44\u9879\u540d\u79f0", "hisbase\u5b57\u6bb5", "\u5feb\u7167\u9884\u671f\u91d1\u989d", "hisbase\u5b9e\u9645\u91d1\u989d", "\u5efa\u8bae\u68c0\u67e5", "\u5efa\u8bae\u539f\u56e0", "\u95ee\u9898"]];
        for (const field of fields || []) {
            const suggestion = WorkbenchPanel.historyWriteFieldSuggestion(field, comparison);
            rows.push([
                comparison.caseNo,
                comparison.planNo,
                comparison.personCode,
                field.itemCode,
                field.itemName,
                field.historyField,
                field.expectedAmount,
                field.actualAmount,
                suggestion.label,
                suggestion.reason,
                field.issue || "\u91d1\u989d\u4e0d\u4e00\u81f4"
            ]);
        }
        if (!comparison.totalMatched) {
            rows.push([
                comparison.caseNo,
                comparison.planNo,
                comparison.personCode,
                "HJ2",
                "\u5408\u8ba1",
                "hj2",
                comparison.expectedTotal,
                comparison.actualTotal,
                "\u5176\u4ed6",
                "\u5408\u8ba1\u5dee\u5f02\u9700\u7ed3\u5408\u5b57\u6bb5\u5dee\u5f02\u548c\u624b\u5de5\u503c\u6838\u67e5",
                "\u5408\u8ba1\u4e0d\u4e00\u81f4"
            ]);
        }
        return rows;
    },
    showHistoryWriteExecuteResult(result) {
        document.querySelector(".case-detail-overlay")?.remove();
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u7ed3\u679c">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u7ed3\u679c</strong>
                        <span>${Format.html(result.personCode || "-")} | ${Format.html(result.status || "-")}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <dl class="case-detail-grid">
                    ${[
                        ["\u529e\u7406\u7f16\u53f7", result.caseNo],
                        ["\u5199\u5165\u8ba1\u5212\u53f7", result.writePlanId],
                        ["\u5386\u53f2\u884cID", result.historyId],
                        ["\u5199\u5165\u72b6\u6001", result.status],
                        ["\u4eba\u5458\u7f16\u7801", result.personCode],
                        ["\u5355\u4f4d\u7f16\u7801", result.orgCode],
                        ["sid\u662f\u5426\u8c03\u6574", result.sidUpdateRequired ? "\u662f" : "\u5426"]
                    ].map(([label, value]) => `
                        <div>
                            <dt>${Format.html(label)}</dt>
                            <dd>${Format.html(value || "-")}</dd>
                        </div>
                    `).join("")}
                </dl>
                <div class="case-detail-section">
                    <span>\u6267\u884c\u8bf4\u660e</span>
                    <p>${Format.html(result.message || "-")}</p>
                </div>
                <div class="case-error hidden" data-case-error></div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-history-write-plan-case-no="${Format.html(result.caseNo)}">\u67e5\u770b\u5199\u5165\u8ba1\u5212</button>
                    ${result.historyId ? `<button type="button" class="case-snapshot-button" data-history-write-comparison-case-no="${Format.html(result.caseNo)}">\u5b57\u6bb5\u5bf9\u7167</button>` : ""}
                    ${result.status === "EXECUTED" ? `<button type="button" class="case-cancel-button" data-history-write-rollback-case-no="${Format.html(result.caseNo)}">\u64a4\u9500\u5199\u5165</button>` : ""}
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const planButton = event.target.closest("button[data-history-write-plan-case-no]");
            if (planButton) {
                planButton.disabled = true;
                try {
                    await WorkbenchPanel.openHistoryWritePlan(planButton.dataset.historyWritePlanCaseNo);
                } catch (error) {
                    planButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const comparisonButton = event.target.closest("button[data-history-write-comparison-case-no]");
            if (comparisonButton) {
                comparisonButton.disabled = true;
                try {
                    await WorkbenchPanel.openHistoryWriteComparison(comparisonButton.dataset.historyWriteComparisonCaseNo);
                } catch (error) {
                    comparisonButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const rollbackButton = event.target.closest("button[data-history-write-rollback-case-no]");
            if (rollbackButton) {
                const caseNo = rollbackButton.dataset.historyWriteRollbackCaseNo;
                if (!window.confirm(Format.text(TEXT.historyWriteRollbackConfirm, { caseNo }))) {
                    return;
                }
                rollbackButton.disabled = true;
                try {
                    await WorkbenchPanel.rollbackHistoryWrite(caseNo);
                } catch (error) {
                    rollbackButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWriteBatchPreview(result) {
        document.querySelector(".case-detail-overlay")?.remove();
        const items = result.items || [];
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._batchExport = {
            filename: "history-write-batch-preview.csv",
            rows: WorkbenchPanel.batchPreviewCsvRows(result)
        };
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u6279\u91cf\u9884\u68c0">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u6279\u91cf\u9884\u68c0</strong>
                        <span>\u5171 ${Format.html(result.total || 0)} \u6761 | \u53ef\u5199 ${Format.html(result.ready || 0)} | \u963b\u65ad ${Format.html(result.blocked || 0)} | \u8b66\u544a ${Format.html(result.warning || 0)}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <div class="history-plan-summary batch-preview-summary">
                    <span>\u5f53\u524d ${Format.html(result.total || 0)} \u6761</span>
                    <span>\u53ef\u5199\u5165 ${Format.html(result.ready || 0)}</span>
                    <span>\u5df2\u963b\u65ad ${Format.html(result.blocked || 0)}</span>
                    <span>\u6709\u8b66\u544a ${Format.html(result.warning || 0)}</span>
                </div>
                <div class="case-detail-section">
                    <span>\u9884\u68c0\u660e\u7ec6</span>
                    <div class="case-history-field-list">
                        ${items.length ? items.map((item) => {
                            const period = item.year ? `${item.year}-${String(item.month || 1).padStart(2, "0")}` : "-";
                            const issues = item.issues || [];
                            return `
                                <div class="case-history-field ${item.writable ? "" : "blocked"}">
                                    <strong>${Format.html(item.personCode || "-")}</strong>
                                    <span>${Format.html(item.status || "-")} | ${item.writable ? "\u53ef\u5199" : "\u4e0d\u53ef\u5199"}</span>
                                    <small>${Format.html(item.caseNo || "-")} | ${Format.html(item.businessType || "-")} | ${Format.html(period)}</small>
                                    <small>${issues.length ? Format.html(issues.join(" | ")) : "\u672a\u53d1\u73b0\u963b\u65ad\u6216\u8b66\u544a"}</small>
                                </div>
                            `;
                        }).join("") : `<p class="muted">\u6682\u65e0\u9700\u9884\u68c0\u7684\u5199\u5165\u8ba1\u5212</p>`}
                    </div>
                </div>
                <div class="case-error hidden" data-case-error></div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-batch-result-export>\u5bfc\u51fa\u7ed3\u679c</button>
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            if (event.target.closest("button[data-batch-result-export]")) {
                WorkbenchPanel.downloadCsv(overlay._batchExport.filename, overlay._batchExport.rows);
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWriteBatchRetest(result) {
        document.querySelector(".case-detail-overlay")?.remove();
        const items = result.items || [];
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._batchExport = {
            filename: "history-write-batch-retest.csv",
            rows: WorkbenchPanel.batchRetestCsvRows(result)
        };
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u6279\u91cf\u590d\u6d4b\u9884\u68c0">
                <header class="case-detail-head">
                    <div>
                        <strong>\u6279\u91cf\u590d\u6d4b\u9884\u68c0</strong>
                        <span>\u5171 ${Format.html(result.total || 0)} \u6761 | \u4e00\u81f4 ${Format.html(result.matched || 0)} | \u4ecd\u6709\u5dee\u5f02 ${Format.html(result.mismatched || 0)} | \u5931\u8d25 ${Format.html(result.failed || 0)}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <div class="history-plan-summary batch-preview-summary">
                    <span>\u5f53\u524d ${Format.html(result.total || 0)} \u6761</span>
                    <span>\u590d\u6d4b\u4e00\u81f4 ${Format.html(result.matched || 0)}</span>
                    <span>\u4ecd\u6709\u5dee\u5f02 ${Format.html(result.mismatched || 0)}</span>
                    <span>\u5931\u8d25 ${Format.html(result.failed || 0)}</span>
                </div>
                <div class="case-detail-section">
                    <span>\u590d\u6d4b\u660e\u7ec6</span>
                    <div class="case-history-field-list">
                        ${items.length ? items.map((item) => `
                            <div class="case-history-field ${item.status === "MATCHED" ? "matched" : "blocked"}">
                                <strong>${Format.html(item.personCode || "-")}</strong>
                                <span>${Format.html(item.status || "-")} | \u5dee\u5f02 ${Format.html(item.mismatchCount || 0)}</span>
                                <small>${Format.html(item.caseNo || "-")} | ${Format.html(item.businessType || "-")} | ${Format.html(item.orgCode || "-")}</small>
                                <small>${Format.html(item.message || "-")}</small>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u9700\u590d\u6d4b\u7684\u5199\u5165\u8ba1\u5212</p>`}
                    </div>
                </div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-batch-result-export>\u5bfc\u51fa\u7ed3\u679c</button>
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            if (event.target.closest("button[data-batch-result-export]")) {
                WorkbenchPanel.downloadCsv(overlay._batchExport.filename, overlay._batchExport.rows);
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWriteBatchExecute(result) {
        document.querySelector(".case-detail-overlay")?.remove();
        const items = result.items || [];
        const stats = WorkbenchPanel.batchResultStats(items);
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._batchExport = {
            filename: "history-write-batch-execute.csv",
            rows: WorkbenchPanel.batchExecuteCsvRows(result)
        };
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u6279\u91cf\u6267\u884c">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u6279\u91cf\u6267\u884c</strong>
                        <span>\u5171 ${Format.html(result.total || 0)} \u6761 | \u6210\u529f ${Format.html(result.success || 0)} | \u5931\u8d25 ${Format.html(result.failed || 0)} | \u8df3\u8fc7 ${Format.html(result.skipped || 0)}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <div class="history-plan-summary batch-preview-summary">
                    <span>\u5f53\u524d ${Format.html(result.total || 0)} \u6761</span>
                    <span>\u6210\u529f ${Format.html(result.success || 0)}</span>
                    <span>\u5931\u8d25 ${Format.html(result.failed || 0)}</span>
                    <span>\u8df3\u8fc7 ${Format.html(result.skipped || 0)}</span>
                </div>
                <div class="case-detail-section compact">
                    <span>\u7ed3\u679c\u7b5b\u9009</span>
                    ${WorkbenchPanel.batchResultFilterHtml(stats)}
                </div>
                <div class="case-detail-section">
                    <span>\u6267\u884c\u660e\u7ec6</span>
                    <div class="case-history-field-list">
                        ${items.length ? items.map((item) => `
                            <div class="case-history-field ${Format.html(WorkbenchPanel.batchResultStatusClass(item.status))}" data-batch-result-status="${Format.html(item.status || "UNKNOWN")}">
                                <strong>${Format.html(item.personCode || "-")}</strong>
                                <span>${Format.html(WorkbenchPanel.batchResultStatusText(item.status))}</span>
                                <small>${Format.html(item.caseNo || "-")} | ${Format.html(item.writePlanId || "-")} | ${Format.html(item.historyId || "-")}</small>
                                <small>${Format.html(item.message || "-")}</small>
                                <button type="button" class="batch-result-locate" data-batch-result-locate data-person-code="${Format.html(item.personCode || "")}" data-case-no="${Format.html(item.caseNo || "")}" data-write-plan-id="${Format.html(item.writePlanId || "")}" data-result-status="${Format.html(item.status || "")}">\u5b9a\u4f4d</button>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u6267\u884c\u7ed3\u679c</p>`}
                    </div>
                </div>
                <div class="case-error hidden" data-case-error></div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-batch-result-export>\u5bfc\u51fa\u7ed3\u679c</button>
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const filterButton = event.target.closest("button[data-batch-result-filter]");
            if (filterButton) {
                const filter = filterButton.dataset.batchResultFilter || "ALL";
                overlay.querySelectorAll("[data-batch-result-filter]").forEach((button) => button.classList.toggle("active", button === filterButton));
                overlay.querySelectorAll("[data-batch-result-status]").forEach((row) => {
                    row.hidden = filter !== "ALL" && row.dataset.batchResultStatus !== filter;
                });
                return;
            }
            const locateButton = event.target.closest("button[data-batch-result-locate]");
            if (locateButton) {
                locateButton.disabled = true;
                try {
                    overlay.remove();
                    await WorkbenchPanel.locateBatchResult({
                        personCode: locateButton.dataset.personCode,
                        caseNo: locateButton.dataset.caseNo,
                        writePlanId: locateButton.dataset.writePlanId,
                        status: locateButton.dataset.resultStatus
                    });
                } catch (error) {
                    setStatus(error.message);
                }
                return;
            }
            if (event.target.closest("button[data-batch-result-export]")) {
                WorkbenchPanel.downloadCsv(overlay._batchExport.filename, overlay._batchExport.rows);
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWriteBatchRetestApprove(result) {
        document.querySelector(".case-detail-overlay")?.remove();
        const items = result.items || [];
        const stats = WorkbenchPanel.batchResultStats(items);
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._batchExport = {
            filename: "history-write-batch-retest-approve.csv",
            rows: WorkbenchPanel.batchExecuteCsvRows(result)
        };
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u6279\u91cf\u590d\u6d4b\u901a\u8fc7">
                <header class="case-detail-head">
                    <div>
                        <strong>\u6279\u91cf\u590d\u6d4b\u901a\u8fc7</strong>
                        <span>\u5171 ${Format.html(result.total || 0)} \u6761 | \u5df2\u6807\u8bb0 ${Format.html(result.success || 0)} | \u5931\u8d25 ${Format.html(result.failed || 0)} | \u8df3\u8fc7 ${Format.html(result.skipped || 0)}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <div class="history-plan-summary batch-preview-summary">
                    <span>\u5f53\u524d ${Format.html(result.total || 0)} \u6761</span>
                    <span>\u5df2\u6807\u8bb0 ${Format.html(result.success || 0)}</span>
                    <span>\u5931\u8d25 ${Format.html(result.failed || 0)}</span>
                    <span>\u4ecd\u6709\u5dee\u5f02/\u8df3\u8fc7 ${Format.html(result.skipped || 0)}</span>
                </div>
                <div class="case-detail-section compact">
                    <span>\u7ed3\u679c\u7b5b\u9009</span>
                    ${WorkbenchPanel.batchResultFilterHtml(stats)}
                </div>
                <div class="case-detail-section">
                    <span>\u590d\u6d4b\u901a\u8fc7\u660e\u7ec6</span>
                    <div class="case-history-field-list">
                        ${items.length ? items.map((item) => `
                            <div class="case-history-field ${Format.html(WorkbenchPanel.batchResultStatusClass(item.status))}" data-batch-result-status="${Format.html(item.status || "UNKNOWN")}">
                                <strong>${Format.html(item.personCode || "-")}</strong>
                                <span>${Format.html(WorkbenchPanel.batchResultStatusText(item.status))}</span>
                                <small>${Format.html(item.caseNo || "-")} | ${Format.html(item.writePlanId || "-")} | ${Format.html(item.historyId || "-")}</small>
                                <small>${Format.html(item.message || "-")}</small>
                                <button type="button" class="batch-result-locate" data-batch-result-locate data-person-code="${Format.html(item.personCode || "")}" data-case-no="${Format.html(item.caseNo || "")}" data-write-plan-id="${Format.html(item.writePlanId || "")}" data-result-status="${Format.html(item.status || "")}">\u5b9a\u4f4d</button>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u590d\u6d4b\u901a\u8fc7\u7ed3\u679c</p>`}
                    </div>
                </div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-batch-result-export>\u5bfc\u51fa\u7ed3\u679c</button>
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const filterButton = event.target.closest("button[data-batch-result-filter]");
            if (filterButton) {
                const filter = filterButton.dataset.batchResultFilter || "ALL";
                overlay.querySelectorAll("[data-batch-result-filter]").forEach((button) => button.classList.toggle("active", button === filterButton));
                overlay.querySelectorAll("[data-batch-result-status]").forEach((row) => {
                    row.hidden = filter !== "ALL" && row.dataset.batchResultStatus !== filter;
                });
                return;
            }
            const locateButton = event.target.closest("button[data-batch-result-locate]");
            if (locateButton) {
                locateButton.disabled = true;
                try {
                    overlay.remove();
                    await WorkbenchPanel.locateBatchResult({
                        personCode: locateButton.dataset.personCode,
                        caseNo: locateButton.dataset.caseNo,
                        writePlanId: locateButton.dataset.writePlanId,
                        status: locateButton.dataset.resultStatus
                    });
                } catch (error) {
                    setStatus(error.message);
                }
                return;
            }
            if (event.target.closest("button[data-batch-result-export]")) {
                WorkbenchPanel.downloadCsv(overlay._batchExport.filename, overlay._batchExport.rows);
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showHistoryWriteBatchRollback(result) {
        document.querySelector(".case-detail-overlay")?.remove();
        const items = result.items || [];
        const stats = WorkbenchPanel.batchResultStats(items);
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay._batchExport = {
            filename: "history-write-batch-rollback.csv",
            rows: WorkbenchPanel.batchExecuteCsvRows(result)
        };
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="\u5386\u53f2\u5199\u5165\u6279\u91cf\u64a4\u9500">
                <header class="case-detail-head">
                    <div>
                        <strong>\u5386\u53f2\u5199\u5165\u6279\u91cf\u64a4\u9500</strong>
                        <span>\u5171 ${Format.html(result.total || 0)} \u6761 | \u6210\u529f ${Format.html(result.success || 0)} | \u5931\u8d25 ${Format.html(result.failed || 0)} | \u8df3\u8fc7 ${Format.html(result.skipped || 0)}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <div class="history-plan-summary batch-preview-summary">
                    <span>\u5f53\u524d ${Format.html(result.total || 0)} \u6761</span>
                    <span>\u6210\u529f ${Format.html(result.success || 0)}</span>
                    <span>\u5931\u8d25 ${Format.html(result.failed || 0)}</span>
                    <span>\u8df3\u8fc7 ${Format.html(result.skipped || 0)}</span>
                </div>
                <div class="case-detail-section compact">
                    <span>\u7ed3\u679c\u7b5b\u9009</span>
                    ${WorkbenchPanel.batchResultFilterHtml(stats)}
                </div>
                <div class="case-detail-section">
                    <span>\u64a4\u9500\u660e\u7ec6</span>
                    <div class="case-history-field-list">
                        ${items.length ? items.map((item) => `
                            <div class="case-history-field ${Format.html(WorkbenchPanel.batchResultStatusClass(item.status))}" data-batch-result-status="${Format.html(item.status || "UNKNOWN")}">
                                <strong>${Format.html(item.personCode || "-")}</strong>
                                <span>${Format.html(WorkbenchPanel.batchResultStatusText(item.status))}</span>
                                <small>${Format.html(item.caseNo || "-")} | ${Format.html(item.writePlanId || "-")} | ${Format.html(item.historyId || "-")}</small>
                                <small>${Format.html(item.message || "-")}</small>
                                <button type="button" class="batch-result-locate" data-batch-result-locate data-person-code="${Format.html(item.personCode || "")}" data-case-no="${Format.html(item.caseNo || "")}" data-write-plan-id="${Format.html(item.writePlanId || "")}" data-result-status="${Format.html(item.status || "")}">\u5b9a\u4f4d</button>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u64a4\u9500\u7ed3\u679c</p>`}
                    </div>
                </div>
                <div class="case-error hidden" data-case-error></div>
                <footer class="case-detail-actions">
                    <button type="button" class="case-snapshot-button" data-batch-result-export>\u5bfc\u51fa\u7ed3\u679c</button>
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const filterButton = event.target.closest("button[data-batch-result-filter]");
            if (filterButton) {
                const filter = filterButton.dataset.batchResultFilter || "ALL";
                overlay.querySelectorAll("[data-batch-result-filter]").forEach((button) => button.classList.toggle("active", button === filterButton));
                overlay.querySelectorAll("[data-batch-result-status]").forEach((row) => {
                    row.hidden = filter !== "ALL" && row.dataset.batchResultStatus !== filter;
                });
                return;
            }
            const locateButton = event.target.closest("button[data-batch-result-locate]");
            if (locateButton) {
                locateButton.disabled = true;
                try {
                    overlay.remove();
                    await WorkbenchPanel.locateBatchResult({
                        personCode: locateButton.dataset.personCode,
                        caseNo: locateButton.dataset.caseNo,
                        writePlanId: locateButton.dataset.writePlanId,
                        status: locateButton.dataset.resultStatus
                    });
                } catch (error) {
                    setStatus(error.message);
                }
                return;
            }
            if (event.target.closest("button[data-batch-result-export]")) {
                WorkbenchPanel.downloadCsv(overlay._batchExport.filename, overlay._batchExport.rows);
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    showCasePreview(preview, request) {
        const confirmRequest = {
            ...request,
            force: preview.trialStatus === "ERROR"
        };
        const forceReasonHtml = preview.trialStatus === "ERROR"
            ? `<label class="case-force-reason"><span>\u5f3a\u5236\u529e\u7406\u8bf4\u660e</span><textarea data-force-reason rows="3" maxlength="1000" placeholder="\u8bf7\u8bf4\u660e\u8bd5\u7b97\u5f02\u5e38\u4ecd\u9700\u529e\u7406\u7684\u539f\u56e0"></textarea></label>`
            : "";
        const differenceReasonHtml = preview.trialStatus === "DIFFERENT"
            ? `<label class="case-difference-reason"><span>\u5dee\u5f02\u786e\u8ba4\u8bf4\u660e</span><textarea data-difference-reason rows="3" maxlength="1000" placeholder="\u8bf7\u8bf4\u660e\u8bd5\u7b97\u4e0e\u5386\u53f2\u6709\u5dee\u5f02\u4ecd\u9700\u529e\u7406\u7684\u539f\u56e0"></textarea></label>`
            : "";
        const warningHtml = preview.trialStatus === "ERROR"
            ? `<div class="case-warning">${TEXT.forceConfirmHint}</div>${forceReasonHtml}`
            : (preview.trialStatus === "DIFFERENT" ? `<div class="case-warning">${TEXT.differenceConfirmHint}</div>${differenceReasonHtml}` : "");
        const confirmText = preview.trialStatus === "ERROR"
            ? "\u5f3a\u5236\u786e\u8ba4\u529e\u7406"
            : (preview.trialStatus === "DIFFERENT" ? "\u786e\u8ba4\u5dee\u5f02\u5e76\u529e\u7406" : "\u786e\u8ba4\u529e\u7406");
        WorkbenchPanel.showCaseWindow({
            title: TEXT.casePreview,
            detail: preview,
            fields: WorkbenchPanel.caseFields(preview),
            warningHtml,
            actionsHtml: `<button type="button" class="case-confirm-button" data-confirm-case='${Format.html(JSON.stringify(confirmRequest))}'>${confirmText}</button>`
        });
    },
    caseFields(detail) {
        const fields = [
            ["\u4e1a\u52a1\u6765\u6e90", detail.source],
            ["\u4e1a\u52a1\u72b6\u6001", Format.businessStatusText(detail.status || "PREVIEW")],
            ["\u4e1a\u52a1\u7c7b\u578b", detail.businessType],
            ["\u4eba\u5458\u7f16\u7801", detail.personCode],
            ["\u59d3\u540d", detail.personName],
            ["\u5355\u4f4d\u7f16\u7801", detail.orgCode],
            ["\u6267\u884c\u5e74\u6708", detail.year ? `${detail.year}-${String(detail.month || 1).padStart(2, "0")}` : "-"],
            ["\u529e\u7406\u4eba", detail.handledBy],
            ["\u529e\u7406\u65f6\u95f4", detail.handledAt],
            ["\u5f85\u529e\u6807\u8bc6", detail.workItemId],
            ["\u6807\u9898", detail.title]
        ];
        if (detail.caseNo) {
            fields.unshift(["\u529e\u7406\u7f16\u53f7", detail.caseNo]);
        }
        if (detail.caseNo) {
            fields.push(["\u5feb\u7167\u72b6\u6001", detail.snapshotExists ? "\u5df2\u751f\u6210" : "\u672a\u751f\u6210"]);
        }
        if (detail.snapshotAt) {
            fields.push(["\u5feb\u7167\u65f6\u95f4", detail.snapshotAt]);
        }
        if (detail.snapshotBy) {
            fields.push(["\u5feb\u7167\u751f\u6210\u4eba", detail.snapshotBy]);
        }
        if (detail.forceReason) {
            fields.push(["\u5f3a\u5236\u529e\u7406\u8bf4\u660e", detail.forceReason]);
        }
        if (detail.differenceReason) {
            fields.push(["\u5dee\u5f02\u786e\u8ba4\u8bf4\u660e", detail.differenceReason]);
        }
        if (detail.cancelReason) {
            fields.push(["\u64a4\u56de\u529e\u7406\u8bf4\u660e", detail.cancelReason]);
        }
        if (detail.reviewStatus) {
            fields.push(["\u590d\u6838\u72b6\u6001", Format.reviewStatusText(detail.reviewStatus)]);
        }
        if (detail.reviewReason) {
            fields.push(["\u590d\u6838\u8bf4\u660e", detail.reviewReason]);
        }
        if (detail.reviewedBy) {
            fields.push(["\u590d\u6838\u4eba", detail.reviewedBy]);
        }
        if (detail.reviewedAt) {
            fields.push(["\u590d\u6838\u65f6\u95f4", detail.reviewedAt]);
        }
        return fields;
    },
    historyWriteSectionHtml(detail) {
        const plan = detail.historyWritePlan;
        const audits = detail.historyWriteAudits || [];
        if (!plan && !audits.length) {
            return "";
        }
        const latestAudit = audits[0];
        const planStatus = Format.historyWriteStatusText(plan?.planStatus);
        const executionResult = Format.historyWriteStatusText(plan?.executionResult);
        return `
            <div class="case-detail-section">
                <span>\u5386\u53f2\u5199\u5165\u8ddf\u8e2a</span>
                <div class="case-change-list">
                    ${plan ? `
                        <div class="case-change-row">
                            <strong>${Format.html(planStatus)}</strong>
                            <span>${Format.html(plan.insertedHistoryId || "-")}</span>
                            <small>\u8ba1\u5212 ${Format.html(plan.planNo || "-")} | \u6267\u884c\u7ed3\u679c ${Format.html(executionResult)} | \u524d ${Format.html(plan.previousHistoryId || "-")} | \u540e ${Format.html(plan.nextHistoryId || "-")}</small>
                        </div>
                        <div class="case-change-row">
                            <strong>\u6700\u8fd1\u8bf4\u660e</strong>
                            <span>${Format.html(plan.executedAt || plan.rolledBackAt || plan.preparedAt || "-")}</span>
                            <small>${Format.html(plan.rollbackMessage || plan.executionMessage || "-")}</small>
                        </div>
                    ` : `<p class="muted">\u5c1a\u672a\u751f\u6210\u5199\u5165\u8ba1\u5212</p>`}
                    ${latestAudit ? `
                        <div class="case-change-row">
                            <strong>\u6700\u8fd1\u5199\u5165\u6d41\u6c34</strong>
                            <span>${Format.html(latestAudit.operator || "-")}</span>
                            <small>${Format.html(Format.auditActionText(latestAudit.action))} | ${Format.html(latestAudit.createdAt || "-")} | ${Format.html(latestAudit.summary || "-")}</small>
                        </div>
                    ` : ""}
                </div>
                <div class="case-detail-actions inline-actions">
                    ${plan ? `<button type="button" class="case-snapshot-button" data-history-write-plan-case-no="${Format.html(detail.caseNo)}">\u67e5\u770b\u5199\u5165\u8ba1\u5212</button>` : ""}
                    ${plan ? `<button type="button" class="case-snapshot-button" data-history-write-comparison-case-no="${Format.html(detail.caseNo)}">\u5b57\u6bb5\u5bf9\u7167</button>` : ""}
                    ${audits.length ? `<button type="button" class="case-snapshot-button" data-history-write-audit-export-case-no="${Format.html(detail.caseNo)}">\u5bfc\u51fa\u5199\u5165\u6d41\u6c34</button>` : ""}
                </div>
            </div>
        `;
    },
    policyBasisHtml(detail) {
        const type = detail.businessType || "";
        const source = detail.source || "";
        const sourceText = source === "SALARY_EVENT"
            ? "\u81ea\u52a8\u63a8\u5bfc\u5de5\u8d44\u53d8\u52a8"
            : source;
        const basisMap = {
            "\u6b63\u5e38\u6863\u6b21": ["\u5e74\u5ea6\u8003\u6838", "\u7d2f\u8ba1\u5e74\u9650", "\u6b21\u5e741\u6708\u6267\u884c"],
            "\u6b63\u5e38\u7ea7\u522b": ["\u5e74\u5ea6\u8003\u6838", "\u7d2f\u8ba15\u5e74", "\u6b21\u5e741\u6708\u6267\u884c"],
            "\u804c\u52a1\u53d8\u5316": ["\u4efb\u804c\u4fe1\u606f", "\u804c\u52a1\u5de5\u8d44", "\u6d25\u8865\u8d34\u6807\u51c6"],
            "\u804c\u7ea7\u5957\u6539": ["\u4efb\u804c\u4fe1\u606f", "\u804c\u7ea7\u5de5\u8d44", "2019\u5e74\u53e3\u5f84"],
            "\u804c\u7ea7\u664b\u5347": ["\u4efb\u804c\u4fe1\u606f", "\u804c\u7ea7\u664b\u5347", "\u76ee\u6807\u72b6\u6001\u91cd\u7b97"],
            "\u8b66\u5458\u5957\u6539": ["\u4efb\u804c\u4fe1\u606f", "\u8b66\u5458\u804c\u52a1\u5e8f\u5217", "\u7b49\u7ea7\u5de5\u8d44"],
            "\u5b66\u5386\u53d8\u5316": ["\u5b66\u5386\u4fe1\u606f", "\u8f6c\u6b63\u5b9a\u7ea7\u6807\u51c6", "\u76ee\u6807\u72b6\u6001\u91cd\u7b97"],
            "\u964d\u8d44\u5904\u5206": ["\u5956\u60e9\u4fe1\u606f", "\u5904\u5206\u8bb0\u5f55", "\u76ee\u6807\u72b6\u6001\u91cd\u7b97"],
            "\u5956\u52b1\u664b\u5347": ["\u5956\u60e9\u4fe1\u606f", "\u5956\u52b1\u664b\u5347", "\u76ee\u6807\u72b6\u6001\u91cd\u7b97"],
            "2006\u5957\u6539": ["2006\u5957\u6539", "\u5957\u6539\u5e74\u9650", "\u4efb\u804c\u5e74\u9650"]
        };
        const chips = basisMap[type] || ["\u57fa\u7840\u4fe1\u606f", "\u5de5\u8d44\u653f\u7b56", "\u8bd5\u7b97\u590d\u6838"];
        return `
            <div class="case-detail-section compact">
                <span>\u653f\u7b56\u4f9d\u636e</span>
                <div class="case-policy-row">
                    <b>${Format.html(type || "-")}</b>
                    <div>${chips.map((chip) => `<em>${Format.html(chip)}</em>`).join("")}</div>
                    <small>${Format.html(sourceText || "-")} | ${Format.html(detail.workItemId || "-")}</small>
                </div>
            </div>
        `;
    },
    trialOverviewHtml(detail) {
        const changes = detail.trialChanges || [];
        const changedItems = changes.filter((change) => Number(change.difference || 0) !== 0);
        const topItems = changedItems.slice(0, 4);
        return `
            <div class="case-detail-section compact">
                <span>\u8bd5\u7b97\u603b\u89c8</span>
                <div class="case-trial-cards">
                    <div><small>\u57fa\u7ebf</small><strong>${Format.optionalAmount(detail.trialBaselineTotal)}</strong></div>
                    <div><small>\u8bd5\u7b97</small><strong>${Format.optionalAmount(detail.trialCalculatedTotal)}</strong></div>
                    <div><small>\u5386\u53f2</small><strong>${Format.optionalAmount(detail.trialExpectedTotal)}</strong></div>
                    <div><small>\u5dee\u989d</small><strong>${Format.optionalAmount(detail.trialDifference)}</strong></div>
                </div>
                <div class="case-key-change-row">
                    ${topItems.length ? topItems.map((change) => `
                        <span>
                            <b>${Format.html(change.itemName || change.itemCode || "-")}</b>
                            <em>${Format.optionalAmount(change.beforeAmount)} -> ${Format.optionalAmount(change.afterAmount)}</em>
                        </span>
                    `).join("") : `<span><b>\u5173\u952e\u9879</b><em>\u6682\u65e0\u91d1\u989d\u53d8\u52a8</em></span>`}
                </div>
            </div>
        `;
    },
    showCaseWindow({ title, detail, fields, warningHtml, actionsHtml }) {
        document.querySelector(".case-detail-overlay")?.remove();
        const statusClass = (detail.trialStatus || "").toLowerCase();
        const changes = detail.trialChanges || [];
        const audits = detail.audits || [];
        const overlay = document.createElement("div");
        overlay.className = "case-detail-overlay";
        overlay.innerHTML = `
            <section class="case-detail-dialog" role="dialog" aria-modal="true" aria-label="${Format.html(title)}">
                <header class="case-detail-head">
                    <div>
                        <strong>${Format.html(title)}</strong>
                        <span>${Format.html(detail.businessType || "-")} | ${Format.html(detail.personCode || "-")}</span>
                    </div>
                    <button type="button" class="case-detail-close" aria-label="\u5173\u95ed">\u00d7</button>
                </header>
                <dl class="case-detail-grid">
                    ${fields.map(([label, value]) => `
                        <div>
                            <dt>${Format.html(label)}</dt>
                            <dd>${Format.html(value || "-")}</dd>
                        </div>
                    `).join("")}
                </dl>
                <div class="case-detail-section">
                    <span>\u529e\u7406\u6458\u8981</span>
                    <p>${Format.html(detail.summary || "-")}</p>
                </div>
                ${WorkbenchPanel.policyBasisHtml(detail)}
                ${WorkbenchPanel.trialOverviewHtml(detail)}
                ${WorkbenchPanel.historyWriteSectionHtml(detail)}
                <div class="case-detail-section">
                    <span>\u529e\u7406\u6d41\u6c34</span>
                    <div class="case-audit-list">
                        ${audits.length ? audits.map((audit) => `
                            <div class="case-audit-row">
                                <strong>${Format.html(Format.auditActionText(audit.action))}</strong>
                                <small>${Format.html(audit.summary || "-")}</small>
                                <span>${Format.html(audit.operator || "-")} | ${Format.html(audit.createdAt || "-")}</span>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u529e\u7406\u6d41\u6c34</p>`}
                    </div>
                </div>
                <div class="case-trial ${Format.html(statusClass)}">
                    <div>
                        <span>\u529e\u7406\u65f6\u8bd5\u7b97</span>
                        <strong>${Format.html(Format.statusText(detail.trialStatus))}</strong>
                    </div>
                    <p>\u57fa\u7ebf ${Format.optionalAmount(detail.trialBaselineTotal)} | \u8bd5\u7b97 ${Format.optionalAmount(detail.trialCalculatedTotal)} | \u5386\u53f2 ${Format.optionalAmount(detail.trialExpectedTotal)} | \u5dee\u989d ${Format.optionalAmount(detail.trialDifference)}</p>
                    <p>${detail.trialMatched === null || detail.trialMatched === undefined ? "\u672a\u5224\u5b9a" : (detail.trialMatched ? "\u5339\u914d\u5386\u53f2" : "\u4e0e\u5386\u53f2\u6709\u5dee\u5f02")}</p>
                    <p>${Format.html(detail.trialSummary || "-")}</p>
                </div>
                ${warningHtml || ""}
                <div class="case-detail-section">
                    <span>\u5de5\u8d44\u9879\u53d8\u52a8</span>
                    <div class="case-change-list">
                        ${changes.length ? changes.map((change) => `
                            <div class="case-change-row">
                                <strong>${Format.html(change.itemName || change.itemCode || "-")}</strong>
                                <span class="amount ${Number(change.difference || 0) !== 0 ? "negative" : ""}">${Format.optionalAmount(change.difference)}</span>
                                <small>${Format.html(change.itemCode || "-")} | ${Format.html(change.beforeValue || "-")} -> ${Format.html(change.afterValue || "-")} | ${Format.optionalAmount(change.beforeAmount)} -> ${Format.optionalAmount(change.afterAmount)}${change.ruleNote ? ` | ${Format.html(change.ruleNote)}` : ""}</small>
                            </div>
                        `).join("") : `<p class="muted">\u6682\u65e0\u5de5\u8d44\u9879\u53d8\u52a8\u660e\u7ec6</p>`}
                    </div>
                </div>
                <footer class="case-detail-actions">
                    <div class="case-error hidden" data-case-error></div>
                    ${actionsHtml || ""}
                    <button type="button" class="case-detail-close">\u5173\u95ed</button>
                </footer>
            </section>
        `;
        overlay.addEventListener("click", async (event) => {
            const confirmButton = event.target.closest("button[data-confirm-case]");
            if (confirmButton) {
                const errorBox = overlay.querySelector("[data-case-error]");
                const request = JSON.parse(confirmButton.dataset.confirmCase);
                if (request.force) {
                    const reasonInput = overlay.querySelector("[data-force-reason]");
                    const reason = (reasonInput?.value || "").trim();
                    if (!reason) {
                        setStatus(TEXT.forceReasonRequired);
                        WorkbenchPanel.setCaseError(errorBox, TEXT.forceReasonRequired);
                        reasonInput?.classList.add("invalid");
                        reasonInput?.focus();
                        return;
                    }
                    reasonInput?.classList.remove("invalid");
                    request.forceReason = reason;
                }
                if (request.force !== true && detail.trialStatus === "DIFFERENT") {
                    const reasonInput = overlay.querySelector("[data-difference-reason]");
                    const reason = (reasonInput?.value || "").trim();
                    if (!reason) {
                        setStatus(TEXT.differenceReasonRequired);
                        WorkbenchPanel.setCaseError(errorBox, TEXT.differenceReasonRequired);
                        reasonInput?.classList.add("invalid");
                        reasonInput?.focus();
                        return;
                    }
                    reasonInput?.classList.remove("invalid");
                    request.differenceReason = reason;
                }
                WorkbenchPanel.setCaseError(errorBox, "");
                confirmButton.disabled = true;
                try {
                    await WorkbenchPanel.confirmCompleteCase(request);
                } catch (error) {
                    confirmButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(errorBox, error.message);
                }
                return;
            }
            const snapshotButton = event.target.closest("button[data-snapshot-case-no]");
            if (snapshotButton) {
                snapshotButton.disabled = true;
                try {
                    await WorkbenchPanel.openCaseSnapshot(snapshotButton.dataset.snapshotCaseNo);
                } catch (error) {
                    snapshotButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const historyPreviewButton = event.target.closest("button[data-history-write-preview-case-no]");
            if (historyPreviewButton) {
                historyPreviewButton.disabled = true;
                try {
                    await WorkbenchPanel.openHistoryWritePreview(historyPreviewButton.dataset.historyWritePreviewCaseNo);
                } catch (error) {
                    historyPreviewButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const historyPlanButton = event.target.closest("button[data-history-write-plan-case-no]");
            if (historyPlanButton) {
                historyPlanButton.disabled = true;
                try {
                    await WorkbenchPanel.openHistoryWritePlan(historyPlanButton.dataset.historyWritePlanCaseNo);
                } catch (error) {
                    historyPlanButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const historyAuditExportButton = event.target.closest("button[data-history-write-audit-export-case-no]");
            if (historyAuditExportButton) {
                window.location.href = `/api/workbench/salary-cases/${encodeURIComponent(historyAuditExportButton.dataset.historyWriteAuditExportCaseNo || "")}/history-write-audits.csv`;
                return;
            }
            const historyComparisonButton = event.target.closest("button[data-history-write-comparison-case-no]");
            if (historyComparisonButton) {
                historyComparisonButton.disabled = true;
                try {
                    await WorkbenchPanel.openHistoryWriteComparison(historyComparisonButton.dataset.historyWriteComparisonCaseNo);
                } catch (error) {
                    historyComparisonButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const historyExecuteButton = event.target.closest("button[data-history-write-execute-case-no]");
            if (historyExecuteButton) {
                const caseNo = historyExecuteButton.dataset.historyWriteExecuteCaseNo;
                if (!window.confirm(Format.text(TEXT.historyWriteExecuteConfirm, { caseNo }))) {
                    return;
                }
                historyExecuteButton.disabled = true;
                try {
                    await WorkbenchPanel.executeHistoryWrite(caseNo);
                } catch (error) {
                    historyExecuteButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const historyRollbackButton = event.target.closest("button[data-history-write-rollback-case-no]");
            if (historyRollbackButton) {
                const caseNo = historyRollbackButton.dataset.historyWriteRollbackCaseNo;
                if (!window.confirm(Format.text(TEXT.historyWriteRollbackConfirm, { caseNo }))) {
                    return;
                }
                historyRollbackButton.disabled = true;
                try {
                    await WorkbenchPanel.rollbackHistoryWrite(caseNo);
                } catch (error) {
                    historyRollbackButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(overlay.querySelector("[data-case-error]"), error.message);
                }
                return;
            }
            const cancelButton = event.target.closest("button[data-cancel-case-no]");
            if (cancelButton) {
                const errorBox = overlay.querySelector("[data-case-error]");
                const reasonInput = overlay.querySelector("[data-cancel-reason]");
                const reason = (reasonInput?.value || "").trim();
                if (!reason) {
                    setStatus(TEXT.cancelReasonRequired);
                    WorkbenchPanel.setCaseError(errorBox, TEXT.cancelReasonRequired);
                    reasonInput?.classList.add("invalid");
                    reasonInput?.focus();
                    return;
                }
                reasonInput?.classList.remove("invalid");
                WorkbenchPanel.setCaseError(errorBox, "");
                cancelButton.disabled = true;
                try {
                    await WorkbenchPanel.cancelCase(cancelButton.dataset.cancelCaseNo, reason);
                } catch (error) {
                    cancelButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(errorBox, error.message);
                }
                return;
            }
            const reviewButton = event.target.closest("button[data-review-case-no]");
            if (reviewButton) {
                const errorBox = overlay.querySelector("[data-case-error]");
                const reasonInput = overlay.querySelector("[data-review-reason]");
                const reason = (reasonInput?.value || "").trim();
                if (!reason) {
                    setStatus(TEXT.reviewReasonRequired);
                    WorkbenchPanel.setCaseError(errorBox, TEXT.reviewReasonRequired);
                    reasonInput?.classList.add("invalid");
                    reasonInput?.focus();
                    return;
                }
                reasonInput?.classList.remove("invalid");
                WorkbenchPanel.setCaseError(errorBox, "");
                reviewButton.disabled = true;
                try {
                    await WorkbenchPanel.reviewCase(reviewButton.dataset.reviewCaseNo, reason);
                } catch (error) {
                    reviewButton.disabled = false;
                    setStatus(error.message);
                    WorkbenchPanel.setCaseError(errorBox, error.message);
                }
                return;
            }
            if (event.target === overlay || event.target.closest(".case-detail-close")) {
                overlay.remove();
            }
        });
        document.body.appendChild(overlay);
        overlay.querySelector(".case-detail-close").focus();
    },
    setCaseError(errorBox, message) {
        if (!errorBox) {
            return;
        }
        const safeMessage = message || "";
        errorBox.textContent = safeMessage;
        errorBox.classList.toggle("hidden", !safeMessage);
    },
    maintenanceTargetSelector(target) {
        return {
            base: ".person-base-info-band",
            post: ".person-post-band",
            education: ".person-education-band",
            assessment: ".person-assessment-band"
        }[target] || ".person-base-info-band";
    },
    maintenanceTargetFocus(target) {
        return {
            base: "#personBaseInfoForm input, #personBaseInfoForm select, #personBaseInfoForm button",
            post: "#personPostForm input, #personPostForm select, #newPersonPostButton",
            education: "#educationForm input, #educationForm select, #newEducationButton",
            assessment: "#assessmentForm input, #assessmentForm select, #newAssessmentButton"
        }[target] || "#personBaseInfoForm input, #personBaseInfoForm select, #personBaseInfoForm button";
    },
    maintenanceTargetText(target) {
        return {
            base: "\u57fa\u672c\u4fe1\u606f",
            post: "\u4efb\u804c",
            education: "\u5b66\u5386",
            assessment: "\u8003\u6838"
        }[target] || "\u57fa\u672c\u4fe1\u606f";
    },
    renderMaintenanceReturnBar() {
        if (!els.maintenanceReturnBar) {
            return;
        }
        const info = state.maintenanceReturn;
        if (!info || !info.caseNo) {
            els.maintenanceReturnBar.classList.add("hidden");
            els.maintenanceReturnBar.innerHTML = "";
            return;
        }
        els.maintenanceReturnBar.classList.remove("hidden");
        const fieldText = Array.isArray(info.fields) ? info.fields.filter(Boolean).join("\u3001") : (info.fields || "");
        const reasonText = info.reason || "";
        const queue = Array.isArray(info.selectionQueue) ? info.selectionQueue : [];
        const currentIndex = queue.findIndex((item) => item.caseNo === info.caseNo);
        const queueText = queue.length ? ` | \u9009\u4e2d\u961f\u5217 ${currentIndex >= 0 ? currentIndex + 1 : 1}/${queue.length}` : "";
        const hasNextSelected = queue.length > 1 && currentIndex >= 0 && currentIndex < queue.length - 1;
        const lastRetest = info.lastRetest || null;
        const lastRetestText = lastRetest
            ? `${lastRetest.caseNo || "\u4e0a\u4e00\u6761"} ${lastRetest.matched ? "\u5df2\u4e00\u81f4" : `\u4ecd\u6709\u5dee\u5f02 ${lastRetest.mismatchCount || 0}`}`
            : "";
        const queueRetests = Array.isArray(info.queueRetests) ? info.queueRetests : [];
        const queueSummary = queueRetests.reduce((acc, item) => {
            acc.total += 1;
            acc.matched += item.matched ? 1 : 0;
            acc.mismatched += item.matched ? 0 : 1;
            return acc;
        }, { total: 0, matched: 0, mismatched: 0 });
        const queueTotal = queue.length || queueSummary.total;
        const queueSummaryText = queueSummary.total
            ? `\u672c\u8f6e\u590d\u6d4b\uff1a\u5df2\u590d\u6d4b ${queueSummary.total}/${queueTotal}\uff0c\u4e00\u81f4 ${queueSummary.matched}\uff0c\u4ecd\u6709\u5dee\u5f02 ${queueSummary.mismatched}`
            : "";
        const queueSummaryComplete = queueTotal > 0 && queueSummary.total >= queueTotal;
        els.maintenanceReturnBar.innerHTML = `
            <span>
                <strong>${info.dirty ? "\u5df2\u4fdd\u5b58\uff0c\u53ef\u56de\u5230\u5f02\u5e38\u8ba1\u5212\u590d\u6d4b" : "\u6765\u81ea\u5f02\u5e38\u6838\u67e5"}</strong>
                ${lastRetest ? `<small class="maintenance-retest-result ${lastRetest.matched ? "matched" : "mismatched"}">\u4e0a\u4e00\u6761\u590d\u6d4b\uff1a${Format.html(lastRetestText)}</small>` : ""}
                ${queueSummaryText ? `<small class="maintenance-queue-summary ${queueSummaryComplete ? "complete" : ""}">${Format.html(queueSummaryText)}</small>` : ""}
                <em>${Format.html(info.caseNo)} | ${Format.html(info.label || WorkbenchPanel.maintenanceTargetText(info.target))}${Format.html(queueText)}${fieldText ? ` | \u5efa\u8bae\u5b57\u6bb5\uff1a${Format.html(fieldText)}` : ""}${reasonText ? ` | ${Format.html(reasonText)}` : ""}</em>
            </span>
            <div>
                <button type="button" data-maintenance-return-refresh>\u5237\u65b0\u5f53\u524d\u4eba\u5458</button>
                ${info.dirty ? `<button type="button" data-maintenance-return-retest>\u590d\u6d4b\u5f53\u524d\u9879</button>` : ""}
                ${info.dirty && hasNextSelected ? `<button type="button" data-maintenance-return-retest-next>\u590d\u6d4b\u5e76\u4e0b\u4e00\u6761</button>` : ""}
                ${info.dirty && !hasNextSelected && queue.length > 1 ? `<button type="button" data-maintenance-return-retest-summary>\u590d\u6d4b\u5e76\u6c47\u603b</button>` : ""}
                ${queueSummaryComplete && queueSummary.mismatched ? `<button type="button" data-maintenance-return-filter-mismatched>\u7b5b\u51fa\u4ecd\u6709\u5dee\u5f02</button>` : ""}
                <button type="button" data-maintenance-return-case-no="${Format.html(info.caseNo)}">\u56de\u5230\u5b57\u6bb5\u5bf9\u7167</button>
                ${hasNextSelected ? `<button type="button" data-maintenance-return-next-selected>\u4e0b\u4e00\u6761\u9009\u4e2d</button>` : ""}
                <button type="button" data-maintenance-return-clear>\u5173\u95ed</button>
            </div>
        `;
    },
    markMaintenanceReturnDirty() {
        if (!state.maintenanceReturn) {
            return;
        }
        state.maintenanceReturn = { ...state.maintenanceReturn, dirty: true };
        WorkbenchPanel.renderMaintenanceReturnBar();
    },
    async returnToHistoryWriteComparison(caseNo) {
        const safeCaseNo = caseNo || state.maintenanceReturn?.caseNo || "";
        if (!safeCaseNo) {
            return;
        }
        const shouldRetest = Boolean(state.maintenanceReturn?.dirty);
        await SystemShell.selectView("workbench", "WORKBENCH", { silent: true });
        if (shouldRetest) {
            await WorkbenchPanel.retestHistoryWriteComparison(safeCaseNo);
            state.maintenanceReturn = { ...state.maintenanceReturn, dirty: false };
        } else {
            await WorkbenchPanel.openHistoryWriteComparison(safeCaseNo);
            await WorkbenchPanel.loadHistoryWritePlans();
        }
    },
    async retestMaintenanceReturnCurrent() {
        const safeCaseNo = state.maintenanceReturn?.caseNo || "";
        if (!safeCaseNo) {
            return;
        }
        await WorkbenchPanel.returnToHistoryWriteComparison(safeCaseNo);
    },
    maintenanceRetestResultFromComparison(comparison, fallback = {}) {
        const mismatchCount = (comparison.fields || []).filter((field) => !field.matched).length;
        return {
            caseNo: comparison.caseNo || fallback.caseNo || "",
            personCode: comparison.personCode || fallback.personCode || "",
            matched: Boolean(comparison.totalMatched) && mismatchCount === 0,
            mismatchCount,
            totalMatched: Boolean(comparison.totalMatched)
        };
    },
    mergeMaintenanceQueueRetests(results, item) {
        const merged = (Array.isArray(results) ? results : []).filter((result) => result.caseNo !== item.caseNo);
        merged.push(item);
        return merged;
    },
    async retestMaintenanceReturnAndNext() {
        const info = state.maintenanceReturn;
        const safeCaseNo = info?.caseNo || "";
        if (!safeCaseNo) {
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const comparison = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(safeCaseNo)}/history-write-comparison-retest`, {
            method: "POST"
        });
        const lastRetest = WorkbenchPanel.maintenanceRetestResultFromComparison(comparison, {
            caseNo: safeCaseNo,
            personCode: info.personCode || ""
        });
        const queueRetests = WorkbenchPanel.mergeMaintenanceQueueRetests(info.queueRetests, lastRetest);
        state.maintenanceReturn = { ...info, dirty: false, lastRetest, queueRetests };
        await WorkbenchPanel.loadHistoryWritePlans();
        await WorkbenchPanel.openNextSelectedMaintenance(lastRetest, queueRetests);
    },
    async retestMaintenanceReturnAndSummarize() {
        const info = state.maintenanceReturn;
        const safeCaseNo = info?.caseNo || "";
        if (!safeCaseNo) {
            return;
        }
        setStatus(TEXT.loadingCaseDetail);
        const comparison = await Api.request(`/api/workbench/salary-cases/${encodeURIComponent(safeCaseNo)}/history-write-comparison-retest`, {
            method: "POST"
        });
        const lastRetest = WorkbenchPanel.maintenanceRetestResultFromComparison(comparison, {
            caseNo: safeCaseNo,
            personCode: info.personCode || ""
        });
        const queueRetests = WorkbenchPanel.mergeMaintenanceQueueRetests(info.queueRetests, lastRetest);
        state.maintenanceReturn = { ...info, dirty: false, lastRetest, queueRetests };
        await WorkbenchPanel.loadHistoryWritePlans();
        WorkbenchPanel.renderMaintenanceReturnBar();
        setStatus(TEXT.caseDetail);
    },
    async filterMaintenanceQueueMismatches() {
        const info = state.maintenanceReturn;
        const queueRetests = Array.isArray(info?.queueRetests) ? info.queueRetests : [];
        const mismatchedCaseNos = queueRetests
            .filter((item) => !item.matched && item.caseNo)
            .map((item) => item.caseNo);
        if (!mismatchedCaseNos.length) {
            setStatus(TEXT.workbenchReady);
            return;
        }
        state.historyPlanQueueFilter = { caseNos: mismatchedCaseNos, autoSelect: true };
        state.historyPlanSelected.clear();
        persistHistoryPlanQueueState();
        state.historyPlanMismatchField = "";
        state.historyPlanRetestStatus = "RETEST_MISMATCHED";
        state.historyPlanLocate = null;
        if (els.workbenchKeywordInput) {
            els.workbenchKeywordInput.value = "";
        }
        if (els.historyPlanStatusSelect) {
            els.historyPlanStatusSelect.value = "EXECUTED";
        }
        if (els.historyPlanComparisonSelect) {
            els.historyPlanComparisonSelect.value = "MISMATCHED";
        }
        if (els.historyPlanReviewSelect) {
            els.historyPlanReviewSelect.value = "";
        }
        if (els.historyPlanRetestSelect) {
            els.historyPlanRetestSelect.value = "RETEST_MISMATCHED";
        }
        if (els.historyPlanMaintenanceSelect) {
            els.historyPlanMaintenanceSelect.value = "";
        }
        if (els.historyPlanPrioritySelect) {
            els.historyPlanPrioritySelect.value = "";
        }
        if (els.historyPlanActionSelect) {
            els.historyPlanActionSelect.value = "";
        }
        await SystemShell.selectView("workbench", "WORKBENCH", { silent: true });
        await WorkbenchPanel.loadHistoryWritePlans();
        els.historyReviewLedger?.scrollIntoView({ behavior: "smooth", block: "start" });
        setStatus(`\u5df2\u7b5b\u51fa\u672c\u8f6e\u4ecd\u6709\u5dee\u5f02 ${mismatchedCaseNos.length} \u6761`);
    },
    async openNextSelectedMaintenance(lastRetest = null, queueRetests = null) {
        const info = state.maintenanceReturn;
        const queue = Array.isArray(info?.selectionQueue) ? info.selectionQueue : [];
        if (!queue.length) {
            return;
        }
        const currentIndex = queue.findIndex((item) => item.caseNo === info.caseNo);
        const next = queue[currentIndex + 1];
        if (!next) {
            return;
        }
        const plan = (state.historyPlanCurrentItems || []).find((item) => item.caseNo === next.caseNo) || next;
        await WorkbenchPanel.openPersonMaintenance(next.personCode, info.target || "base", {
            ...WorkbenchPanel.selectedHistoryPlanMaintenanceSource(plan, info.target || "base", queue),
            lastRetest: lastRetest || info.lastRetest || null,
            queueRetests: queueRetests || info.queueRetests || [],
            dirty: false
        });
    },
    async openPersonMaintenance(personCode, target = "base", source = null) {
        const safePersonCode = personCode || "";
        if (!safePersonCode) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        if (source?.caseNo) {
            const fields = Array.isArray(source.fields)
                ? source.fields
                : String(source.fields || "").split("\u3001").map((item) => item.trim()).filter(Boolean);
            state.maintenanceReturn = {
                caseNo: source.caseNo,
                personCode: safePersonCode,
                target,
                label: source.label || "",
                reason: source.reason || "",
                fields,
                selectionQueue: Array.isArray(source.selectionQueue) ? source.selectionQueue : [],
                selectionTotal: Number(source.selectionTotal || 0),
                lastRetest: source.lastRetest || null,
                queueRetests: Array.isArray(source.queueRetests) ? source.queueRetests : [],
                dirty: false
            };
        }
        document.querySelector(".case-detail-overlay")?.remove();
        setStatus(TEXT.openingWorkItem);
        await SystemShell.selectView("salary", "SALARY_PERSON", { silent: true });
        state.selectedOrgCode = "";
        state.keyword = safePersonCode;
        state.page = 1;
        els.keywordInput.value = safePersonCode;
        if (!state.orgs.length) {
            await OrgPanel.load();
        }
        els.orgTree.innerHTML = OrgPanel.render(state.orgs);
        await PeoplePanel.load();
        await PersonDetail.selectPerson(safePersonCode);
        WorkbenchPanel.renderMaintenanceReturnBar();
        const section = document.querySelector(WorkbenchPanel.maintenanceTargetSelector(target));
        if (section) {
            section.scrollIntoView({ behavior: "smooth", block: "start" });
            section.classList.add("maintenance-focus");
            window.setTimeout(() => section.classList.remove("maintenance-focus"), 1800);
        }
        const focusTarget = document.querySelector(WorkbenchPanel.maintenanceTargetFocus(target));
        focusTarget?.focus?.();
    },
    async openWorkItem(button) {
        try {
            const item = button.closest(".work-item") || button;
            const personCode = item.dataset.personCode || "";
            if (!personCode || !["SALARY_EVENT", "SALARY_CASE"].includes(item.dataset.source || "")) {
                setStatus(TEXT.menuPlaceholder);
                return;
            }
            if (item.dataset.source === "SALARY_CASE" && ["DONE", "CANCELLED"].includes(item.dataset.status || "")) {
                await WorkbenchPanel.openCaseDetail(item.dataset.workId);
                return;
            }
            setStatus(TEXT.openingWorkItem);
            els.batchYearInput.value = item.dataset.year || els.batchYearInput.value;
            els.batchMonthInput.value = item.dataset.month || els.batchMonthInput.value;
            els.batchChangeTypeInput.value = item.dataset.changeType || "";
            await WorkbenchPanel.openPersonMaintenance(personCode, "base");
            if (button.dataset.status === "DONE" && button.dataset.workId) {
                await PersonDetail.selectHistory(button.dataset.workId);
                return;
            }
            await PersonDetail.trialCalculate();
        } catch (error) {
            setStatus(error.message);
            els.salaryTitle.textContent = TEXT.ruleTrialFailed;
            els.salaryTotal.textContent = error.message;
            els.salaryDetails.innerHTML = `<div class="error">${Format.html(error.message)}</div>`;
        }
    }
};

const SystemPanel = {
    menuItems: [],
    orgItems: [],
    async load(menuCode = state.activeMenuCode) {
        setStatus(TEXT.loadingSystem);
        if (menuCode === "SYSTEM_AUDIT") {
            await SystemPanel.loadAudits();
        } else if (menuCode === "SYSTEM_ROLE") {
            await SystemPanel.loadRoles();
        } else if (menuCode === "SYSTEM_USER") {
            await SystemPanel.loadUsers();
        } else {
            await SystemPanel.loadMenus();
        }
        setStatus(TEXT.systemReady);
    },
    async loadMenus() {
        els.systemViewTitle.textContent = "\u83dc\u5355\u7ba1\u7406";
        els.systemViewHint.textContent = "\u7ef4\u62a4\u5df2\u5165\u5e93\u7684\u83dc\u5355\u72b6\u6001\u3002\u505c\u7528\u540e\u4e0d\u518d\u51fa\u73b0\u5728\u5de6\u4fa7\u83dc\u5355\u3002";
        const items = await Api.request("/api/system/menu-admin");
        SystemPanel.menuItems = items;
        els.systemContent.innerHTML = `
            <table class="system-table">
                <thead>
                    <tr>
                        <th>\u7f16\u7801</th>
                        <th>\u4e0a\u7ea7</th>
                        <th>\u540d\u79f0</th>
                        <th>\u89c6\u56fe</th>
                        <th>\u56fe\u6807</th>
                        <th>\u6392\u5e8f</th>
                        <th>\u72b6\u6001</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.map((item) => `
                        <tr>
                            <td class="system-code">${Format.html(item.code)}</td>
                            <td>${Format.html(item.parentCode || "-")}</td>
                            <td>${Format.html(item.title)}</td>
                            <td>${Format.html(item.view || "-")}</td>
                            <td>${Format.html(item.icon || "-")}</td>
                            <td>${Format.html(item.sequence)}</td>
                            <td>
                                <select class="system-select" data-menu-status-code="${Format.html(item.code)}">
                                    <option value="ACTIVE" ${item.status === "ACTIVE" ? "selected" : ""}>\u542f\u7528</option>
                                    <option value="DISABLED" ${item.status === "DISABLED" ? "selected" : ""}>\u505c\u7528</option>
                                </select>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
        els.systemContent.querySelectorAll(".system-org-tree").forEach((tree) => SystemPanel.refreshOrgIndeterminate(tree));
    },
    async loadAudits() {
        els.systemViewTitle.textContent = "\u64cd\u4f5c\u5ba1\u8ba1";
        els.systemViewHint.textContent = "\u67e5\u770b\u7cfb\u7edf\u7ba1\u7406\u3001\u8d26\u6237\u5b89\u5168\u548c\u5de5\u8d44\u9879\u914d\u7f6e\u7684\u6700\u8fd1\u64cd\u4f5c\u8bb0\u5f55\u3002";
        const params = new URLSearchParams();
        Object.entries(state.auditFilters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && String(value).trim() !== "") {
                params.set(key, value);
            }
        });
        const items = await Api.request(`/api/system/audits?${params.toString()}`);
        els.systemContent.innerHTML = `
            <form class="system-editor audit-filter" data-audit-filter-form>
                <select name="module" aria-label="\u6a21\u5757">
                    <option value="" ${state.auditFilters.module === "" ? "selected" : ""}>\u5168\u90e8\u6a21\u5757</option>
                    <option value="system" ${state.auditFilters.module === "system" ? "selected" : ""}>\u7cfb\u7edf\u7ba1\u7406</option>
                    <option value="salary-config" ${state.auditFilters.module === "salary-config" ? "selected" : ""}>\u5de5\u8d44\u9879\u914d\u7f6e</option>
                    <option value="\u8d26\u6237\u5b89\u5168" ${state.auditFilters.module === "\u8d26\u6237\u5b89\u5168" ? "selected" : ""}>\u8d26\u6237\u5b89\u5168</option>
                </select>
                <input name="operator" type="search" placeholder="\u64cd\u4f5c\u4eba" value="${Format.html(state.auditFilters.operator)}">
                <input name="targetCode" type="search" placeholder="\u5bf9\u8c61\u7f16\u7801" value="${Format.html(state.auditFilters.targetCode)}">
                <input name="start" type="datetime-local" value="${Format.html(state.auditFilters.start)}" aria-label="\u5f00\u59cb\u65f6\u95f4">
                <input name="end" type="datetime-local" value="${Format.html(state.auditFilters.end)}" aria-label="\u7ed3\u675f\u65f6\u95f4">
                <input name="limit" type="number" min="1" max="200" value="${Format.html(state.auditFilters.limit)}" aria-label="\u6761\u6570">
                <button type="submit">\u7b5b\u9009</button>
                <button type="button" data-reset-audit-filter>\u91cd\u7f6e</button>
                <button type="button" data-export-audit>\u5bfc\u51fa</button>
            </form>
            <table class="system-table">
                <thead>
                    <tr>
                        <th>\u65f6\u95f4</th>
                        <th>\u64cd\u4f5c\u4eba</th>
                        <th>\u6a21\u5757</th>
                        <th>\u52a8\u4f5c</th>
                        <th>\u5bf9\u8c61</th>
                        <th>\u6458\u8981</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.length ? items.map((item) => `
                        <tr>
                            <td>${Format.html(item.createdAt || "-")}</td>
                            <td class="system-code">${Format.html(item.operator || "-")}</td>
                            <td>${Format.html(item.module || "-")}</td>
                            <td>${Format.html(item.action || "-")}</td>
                            <td>${Format.html([item.targetType, item.targetCode].filter(Boolean).join(":") || "-")}</td>
                            <td>${Format.html(item.summary || "-")}</td>
                        </tr>
                    `).join("") : `<tr><td colspan="6">${TEXT.noAudit}</td></tr>`}
                </tbody>
            </table>
        `;
    },
    async applyAuditFilters(form) {
        const values = Object.fromEntries(new FormData(form).entries());
        state.auditFilters = {
            module: values.module || "",
            operator: (values.operator || "").trim(),
            targetCode: (values.targetCode || "").trim(),
            start: values.start || "",
            end: values.end || "",
            limit: Number(values.limit || 100)
        };
        await SystemPanel.loadAudits();
        setStatus(TEXT.systemReady);
    },
    async resetAuditFilters() {
        state.auditFilters = {
            module: "",
            operator: "",
            targetCode: "",
            start: "",
            end: "",
            limit: 100
        };
        await SystemPanel.loadAudits();
        setStatus(TEXT.systemReady);
    },
    exportAudits() {
        const params = new URLSearchParams();
        Object.entries(state.auditFilters).forEach(([key, value]) => {
            if (value !== undefined && value !== null && String(value).trim() !== "") {
                params.set(key, value);
            }
        });
        params.set("limit", Math.max(Number(state.auditFilters.limit || 100), 1000));
        window.location.href = `/api/system/audits.csv?${params.toString()}`;
    },
    async loadRoles() {
        els.systemViewTitle.textContent = "\u89d2\u8272\u6743\u9650";
        els.systemViewHint.textContent = "\u7ef4\u62a4\u89d2\u8272\u53ef\u8bbf\u95ee\u7684\u83dc\u5355\u6743\u9650\u3002";
        const [items, menus, templates] = await Promise.all([
            Api.request("/api/system/roles"),
            Api.request("/api/system/menu-admin"),
            Api.request("/api/system/role-templates")
        ]);
        SystemPanel.menuItems = menus;
        SystemPanel.roleTemplates = templates;
        els.systemContent.innerHTML = `
            <form class="system-editor" data-create-role-form>
                <input name="code" type="text" placeholder="\u89d2\u8272\u7f16\u7801" autocomplete="off" required>
                <input name="name" type="text" placeholder="\u89d2\u8272\u540d\u79f0" autocomplete="off" required>
                <button type="submit">\u65b0\u589e\u89d2\u8272</button>
            </form>
            <table class="system-table">
                <thead>
                    <tr>
                        <th>\u89d2\u8272\u7f16\u7801</th>
                        <th>\u89d2\u8272\u540d\u79f0</th>
                        <th>\u72b6\u6001</th>
                        <th>\u83dc\u5355\u6743\u9650</th>
                        <th>\u64cd\u4f5c</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.map((item) => `
                        <tr>
                            <td class="system-code">${Format.html(item.code)}</td>
                            <td>${Format.html(item.name)}</td>
                            <td>
                                <select class="system-select" data-role-status-code="${Format.html(item.code)}">
                                    <option value="ACTIVE" ${item.status === "ACTIVE" ? "selected" : ""}>\u542f\u7528</option>
                                    <option value="DISABLED" ${item.status === "DISABLED" ? "selected" : ""}>\u505c\u7528</option>
                                </select>
                            </td>
                            <td>${SystemPanel.roleMenuEditor(item)}</td>
                            <td>
                                <div class="system-actions">
                                    <select class="system-select" data-template-role="${Format.html(item.code)}">
                                        <option value="">\u89d2\u8272\u6a21\u677f</option>
                                        ${templates.map((template) => `
                                            <option value="${Format.html(template.code)}" title="${Format.html(template.description)}">${Format.html(template.name)}</option>
                                        `).join("")}
                                    </select>
                                    <button type="button" class="system-action" data-apply-role-template="${Format.html(item.code)}">\u5957\u7528</button>
                                    <button type="button" class="system-action" data-save-role="${Format.html(item.code)}">\u4fdd\u5b58</button>
                                </div>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    },
    async loadUsers() {
        els.systemViewTitle.textContent = "\u7528\u6237\u7ba1\u7406";
        els.systemViewHint.textContent = "\u7ef4\u62a4\u7528\u6237\u72b6\u6001\u3001\u89d2\u8272\u548c\u53ef\u7ba1\u7406\u5355\u4f4d\u8303\u56f4\u3002";
        const [items, roles, orgs] = await Promise.all([
            Api.request("/api/system/users"),
            Api.request("/api/system/roles"),
            Api.request("/api/org/tree")
        ]);
        SystemPanel.orgItems = orgs;
        els.systemContent.innerHTML = `
            <form class="system-editor" data-create-user-form>
                <input name="username" type="text" placeholder="\u7528\u6237\u540d" autocomplete="off" required>
                <input name="displayName" type="text" placeholder="\u663e\u793a\u540d\u79f0" autocomplete="off" required>
                <div class="system-create-picks">
                    <div class="system-create-title">\u521d\u59cb\u89d2\u8272</div>
                    ${SystemPanel.createRoleChecks(roles)}
                </div>
                <div class="system-create-picks">
                    <div class="system-create-title">\u521d\u59cb\u5355\u4f4d\u6743\u9650</div>
                    <div class="system-org-tree compact" data-create-user-orgs>
                        ${SystemPanel.renderOrgChecks(orgs, new Set())}
                    </div>
                </div>
                <button type="submit">\u65b0\u589e\u7528\u6237</button>
            </form>
            <table class="system-table">
                <thead>
                    <tr>
                        <th>\u7528\u6237\u540d</th>
                        <th>\u663e\u793a\u540d\u79f0</th>
                        <th>\u72b6\u6001</th>
                        <th>\u89d2\u8272</th>
                        <th>\u5355\u4f4d\u6743\u9650</th>
                        <th>\u64cd\u4f5c</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.map((item) => `
                        <tr>
                            <td class="system-code">${Format.html(item.username)}</td>
                            <td>${Format.html(item.displayName)}</td>
                            <td>
                                <select class="system-select" data-user-status-username="${Format.html(item.username)}">
                                    <option value="ACTIVE" ${item.status === "ACTIVE" ? "selected" : ""}>\u542f\u7528</option>
                                    <option value="DISABLED" ${item.status === "DISABLED" ? "selected" : ""}>\u505c\u7528</option>
                                </select>
                            </td>
                            <td>${SystemPanel.userRoleEditor(item, roles)}</td>
                            <td>${SystemPanel.userOrgEditor(item)}</td>
                            <td>
                                <div class="system-actions">
                                    <button type="button" class="system-action" data-save-user="${Format.html(item.username)}">\u4fdd\u5b58</button>
                                    <button type="button" class="system-action" data-reset-password="${Format.html(item.username)}">\u91cd\u7f6e\u5bc6\u7801</button>
                                </div>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    },
    userRoleEditor(user, roles) {
        const selected = new Set(user.roleCodes || []);
        return `
            <div class="system-checks compact" data-username="${Format.html(user.username)}">
                ${roles.map((role) => `
                    <label class="system-check">
                        <input type="checkbox" value="${Format.html(role.code)}" ${selected.has(role.code) ? "checked" : ""}>
                        <span>${Format.html(role.name)} <small>${Format.html(role.code)}</small></span>
                    </label>
                `).join("")}
            </div>
        `;
    },
    createRoleChecks(roles) {
        return `
            <div class="system-checks compact" data-create-user-roles>
                ${roles.map((role) => `
                    <label class="system-check">
                        <input type="checkbox" value="${Format.html(role.code)}">
                        <span>${Format.html(role.name)} <small>${Format.html(role.code)}</small></span>
                    </label>
                `).join("")}
            </div>
        `;
    },
    userOrgEditor(user) {
        const selected = new Set(user.orgCodes || []);
        return `
            <div class="system-org-tree" data-user-orgs="${Format.html(user.username)}">
                ${SystemPanel.renderOrgChecks(SystemPanel.orgItems, selected)}
            </div>
        `;
    },
    renderOrgChecks(nodes, selected, depth = 0) {
        if (!nodes.length) {
            return depth === 0 ? `<div class="loading compact">${TEXT.noOrgs}</div>` : "";
        }
        return nodes.map((node) => `
            <div class="system-org-node">
                <label class="system-org-check" style="--depth: ${depth}">
                    <input type="checkbox" value="${Format.html(node.orgCode)}" data-org-check="${Format.html(node.orgCode)}" ${selected.has(node.orgCode) ? "checked" : ""}>
                    <span class="system-org-code">${Format.html(node.orgCode)}</span>
                    <span class="system-org-name">${Format.html(node.orgName)}</span>
                </label>
                ${node.children?.length ? `<div class="system-org-children">${SystemPanel.renderOrgChecks(node.children, selected, depth + 1)}</div>` : ""}
            </div>
        `).join("");
    },
    roleMenuEditor(role) {
        const selected = new Set(role.menuCodes || []);
        return `
            <div class="system-checks" data-role-code="${Format.html(role.code)}">
                ${SystemPanel.menuItems.map((menu) => `
                    <label class="system-check">
                        <input type="checkbox" value="${Format.html(menu.code)}" ${selected.has(menu.code) ? "checked" : ""}>
                        <span>${Format.html(menu.title)} <small>${Format.html(menu.code)}</small></span>
                    </label>
                `).join("")}
            </div>
        `;
    },
    async updateMenuStatus(select) {
        const code = select.dataset.menuStatusCode;
        const status = select.value;
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/menus/${encodeURIComponent(code)}/status`, {
            method: "PUT",
            body: JSON.stringify({ status })
        });
        await SystemShell.loadMenus();
        setStatus(TEXT.systemSaved);
    },
    async createRole(form) {
        const payload = Object.fromEntries(new FormData(form).entries());
        setStatus(TEXT.savingSystem);
        await Api.request("/api/system/roles", {
            method: "POST",
            body: JSON.stringify({
                code: payload.code,
                name: payload.name,
                menuCodes: []
            })
        });
        await SystemPanel.loadRoles();
        setStatus(TEXT.systemSaved);
    },
    async updateRoleStatus(select) {
        const code = select.dataset.roleStatusCode;
        const status = select.value;
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/roles/${encodeURIComponent(code)}/status`, {
            method: "PUT",
            body: JSON.stringify({ status })
        });
        await SystemShell.loadMenus();
        setStatus(TEXT.systemSaved);
    },
    async saveRoleMenus(roleCode) {
        const container = els.systemContent.querySelector(`[data-role-code="${CSS.escape(roleCode)}"]`);
        const menuCodes = Array.from(container.querySelectorAll("input:checked")).map((input) => input.value);
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/roles/${encodeURIComponent(roleCode)}/menus`, {
            method: "PUT",
            body: JSON.stringify({ menuCodes })
        });
        await SystemShell.loadMenus();
        await SystemPanel.loadRoles();
        setStatus(TEXT.systemSaved);
    },
    async applyRoleTemplate(roleCode) {
        const select = els.systemContent.querySelector(`[data-template-role="${CSS.escape(roleCode)}"]`);
        const templateCode = select?.value || "";
        if (!templateCode) {
            return;
        }
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/roles/${encodeURIComponent(roleCode)}/template/${encodeURIComponent(templateCode)}`, {
            method: "PUT"
        });
        await SystemShell.loadMenus();
        await SystemPanel.loadRoles();
        setStatus(TEXT.systemSaved);
    },
    async createUser(form) {
        const payload = Object.fromEntries(new FormData(form).entries());
        const roleCodes = Array.from(form.querySelectorAll("[data-create-user-roles] input:checked"))
            .map((input) => input.value);
        const orgCodes = SystemPanel.selectedOrgCodes(form.querySelector("[data-create-user-orgs]"));
        setStatus(TEXT.savingSystem);
        await Api.request("/api/system/users", {
            method: "POST",
            body: JSON.stringify({
                username: payload.username,
                displayName: payload.displayName,
                roleCodes,
                orgCodes
            })
        });
        await SystemPanel.loadUsers();
        setStatus(TEXT.systemSaved);
    },
    async updateUserStatus(select) {
        const username = select.dataset.userStatusUsername;
        const status = select.value;
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/users/${encodeURIComponent(username)}/status`, {
            method: "PUT",
            body: JSON.stringify({ status })
        });
        await SystemShell.loadMenus();
        setStatus(TEXT.systemSaved);
    },
    async resetPassword(username) {
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/users/${encodeURIComponent(username)}/password`, {
            method: "PUT",
            body: JSON.stringify({ newPassword: "123456" })
        });
        setStatus(TEXT.passwordReset);
    },
    async saveUserRoles(username) {
        const container = els.systemContent.querySelector(`[data-username="${CSS.escape(username)}"]`);
        const roleCodes = Array.from(container.querySelectorAll("input:checked")).map((input) => input.value);
        const orgContainer = els.systemContent.querySelector(`[data-user-orgs="${CSS.escape(username)}"]`);
        const orgCodes = SystemPanel.selectedOrgCodes(orgContainer);
        setStatus(TEXT.savingSystem);
        await Api.request(`/api/system/users/${encodeURIComponent(username)}/roles`, {
            method: "PUT",
            body: JSON.stringify({ roleCodes })
        });
        await Api.request(`/api/system/users/${encodeURIComponent(username)}/orgs`, {
            method: "PUT",
            body: JSON.stringify({ orgCodes })
        });
        await SystemShell.loadMenus();
        await SystemPanel.loadUsers();
        setStatus(TEXT.systemSaved);
    },
    handleOrgCheckChange(input) {
        const node = input.closest(".system-org-node");
        if (node) {
            node.querySelectorAll(".system-org-children input[data-org-check]").forEach((child) => {
                child.checked = input.checked;
                child.indeterminate = false;
            });
        }
        SystemPanel.refreshOrgIndeterminate(input.closest(".system-org-tree"));
    },
    refreshOrgIndeterminate(tree) {
        if (!tree) {
            return;
        }
        Array.from(tree.querySelectorAll(".system-org-node")).reverse().forEach((node) => {
            const own = node.querySelector(":scope > .system-org-check input[data-org-check]");
            const children = Array.from(node.querySelectorAll(":scope > .system-org-children input[data-org-check]"));
            if (!own || !children.length) {
                return;
            }
            const checked = children.filter((child) => child.checked).length;
            const indeterminate = children.some((child) => child.indeterminate);
            own.indeterminate = (checked > 0 && checked < children.length) || indeterminate;
        });
    },
    selectedOrgCodes(container) {
        if (!container) {
            return [];
        }
        return Array.from(container.querySelectorAll("input[data-org-check]:checked"))
            .map((input) => input.value)
            .filter((code, index, all) => all.findIndex((parent) => parent !== code && code.startsWith(parent)) === -1);
    }
};

const SystemShell = {
    icon(code) {
        if (code.startsWith("SALARY")) {
            return "\u5de5";
        }
        if (code.startsWith("APPLICATION")) {
            return "\u7533";
        }
        if (code.startsWith("SYSTEM")) {
            return "\u7cfb";
        }
        return "\u684c";
    },
    renderMenus(nodes) {
        return nodes.map((node) => {
            const view = node.view || "";
            const children = node.children || [];
            return `
                <div class="menu-group">
                    <button type="button" class="menu-button ${state.activeMenuCode === node.code ? "active" : ""}" data-menu-code="${Format.html(node.code)}" data-view="${Format.html(view)}">
                        <span class="menu-icon">${Format.html(SystemShell.icon(node.code || ""))}</span>
                        <span>${Format.html(node.title)}</span>
                    </button>
                    ${children.length ? `<div class="menu-children">${SystemShell.renderMenus(children)}</div>` : ""}
                </div>
            `;
        }).join("");
    },
    async loadCurrentUser() {
        const user = await Api.request("/api/auth/me");
        state.currentUsername = user.username || "";
        els.currentUserName.textContent = user.displayName || user.username || "System User";
        els.currentUserMeta.textContent = user.username || "\u7ba1\u7406\u5458";
    },
    async loadMenus() {
        state.menus = await Api.request("/api/system/menus");
        els.menuTree.innerHTML = SystemShell.renderMenus(state.menus);
    },
    async selectView(view, menuCode, options = {}) {
        const nextView = view || "workbench";
        if (nextView !== "workbench" && nextView !== "salary" && nextView !== "system") {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        state.activeView = nextView;
        state.activeMenuCode = menuCode || (nextView === "salary" ? "SALARY_PERSON" : "WORKBENCH");
        els.workbenchView.classList.toggle("hidden", nextView !== "workbench");
        els.salaryWorkspace.classList.toggle("hidden", nextView !== "salary");
        els.systemView.classList.toggle("hidden", nextView !== "system");
        els.menuTree.innerHTML = SystemShell.renderMenus(state.menus);
        if (nextView === "workbench") {
            await WorkbenchPanel.load();
        } else if (nextView === "system") {
            await SystemPanel.load(state.activeMenuCode);
        } else {
            Permissions.applySalary();
            if (!state.orgs.length) {
                await OrgPanel.load();
                await PeoplePanel.load();
                if (Permissions.has("SALARY_CONFIG")) {
                    await ConfigPanel.loadEffective();
                }
            }
            if (!options.silent) {
                setStatus(TEXT.salaryWorkspaceReady);
            }
        }
    },
    firstMenu(nodes) {
        for (const node of nodes || []) {
            if (node.view) {
                return node;
            }
            const child = SystemShell.firstMenu(node.children || []);
            if (child) {
                return child;
            }
        }
        return null;
    },
    hasMenuCode(code, nodes = state.menus) {
        return (nodes || []).some((node) => node.code === code || SystemShell.hasMenuCode(code, node.children || []));
    },
    async selectInitialView() {
        const first = SystemShell.firstMenu(state.menus);
        if (!first) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        await SystemShell.selectView(first.view, first.code);
    },
    async boot() {
        try {
            await SystemShell.loadCurrentUser();
            await restoreHistoryPlanQueueStateRemote();
            await Promise.all([
                SystemShell.loadMenus(),
                WorkbenchPanel.load()
            ]);
        } catch (error) {
            setStatus(error.message);
        }
    }
};

const OrgPanel = {
    count(nodes) {
        return nodes.reduce((total, node) => total + 1 + OrgPanel.count(node.children || []), 0);
    },
    render(nodes, depth = 0) {
        if (!nodes.length) {
            return depth === 0 ? `<div class="loading">${TEXT.noOrgs}</div>` : "";
        }
        return nodes.map((node) => `
            <div class="org-node">
                <button type="button" class="${state.selectedOrgCode === node.orgCode ? "active" : ""}" data-org-code="${Format.html(node.orgCode)}" title="${Format.html(node.orgName)}">
                    <span class="org-code">${Format.html(node.orgCode)}</span>
                    <span class="org-name">${Format.html(node.orgName)}</span>
                </button>
                ${node.children?.length ? `<div class="org-children">${OrgPanel.render(node.children, depth + 1)}</div>` : ""}
            </div>
        `).join("");
    },
    async load() {
        els.orgTree.innerHTML = `<div class="loading">${TEXT.loadingOrgs}</div>`;
        state.orgs = await Api.request("/api/org/tree");
        els.orgCount.textContent = OrgPanel.count(state.orgs);
        els.orgTree.innerHTML = OrgPanel.render(state.orgs);
    },
    async select(orgCode) {
        state.selectedOrgCode = orgCode;
        state.page = 1;
        els.orgTree.innerHTML = OrgPanel.render(state.orgs);
        await Promise.all([
            PeoplePanel.load(),
            SalaryPeriods.loadLatest(orgCode)
        ]);
    }
};

const SalaryPeriods = {
    async loadLatest(orgCode) {
        const periods = await Api.request(`/api/salary/periods?orgCode=${encodeURIComponent(orgCode)}&limit=1`);
        if (!periods.length) {
            setStatus(TEXT.noPeriods);
            return;
        }
        const latest = periods[0];
        els.batchYearInput.value = latest.year;
        els.batchMonthInput.value = latest.month;
        setStatus(TEXT.periodLoaded
            .replace("{year}", latest.year)
            .replace("{month}", String(latest.month).padStart(2, "0")));
    }
};

const PeoplePanel = {
    render(pageData) {
        state.people = pageData.records || [];
        els.peopleTotal.textContent = pageData.total;
        els.pageText.textContent = Format.text(TEXT.page, { page: pageData.page });
        els.prevPageButton.disabled = pageData.page <= 1;
        els.nextPageButton.disabled = pageData.page * pageData.size >= pageData.total;

        if (!state.people.length) {
            els.peopleList.innerHTML = `<div class="loading">${TEXT.noPeople}</div>`;
            return;
        }
        els.peopleList.innerHTML = state.people.map((person) => `
            <button type="button" class="person-row ${state.selectedPersonCode === person.personCode ? "active" : ""}" data-person-code="${Format.html(person.personCode)}">
                <span class="person-main">
                    <strong>${Format.html(person.personName)}</strong>
                    <span>${Format.html(person.personCode)}</span>
                </span>
                <span class="person-sub">${Format.html(person.orgName || person.orgCode)}</span>
            </button>
        `).join("");
    },
    async load() {
        els.peopleList.innerHTML = `<div class="loading">${TEXT.loadingPeople}</div>`;
        const params = new URLSearchParams({ page: state.page, size: state.size });
        if (state.keyword) {
            params.set("keyword", state.keyword);
        }
        if (state.selectedOrgCode) {
            params.set("orgCode", state.selectedOrgCode);
        }
        PeoplePanel.render(await Api.request(`/api/persons?${params.toString()}`));
    },
    async search() {
        state.keyword = els.keywordInput.value.trim();
        state.page = 1;
        await PeoplePanel.load();
    },
    async previousPage() {
        state.page = Math.max(1, state.page - 1);
        await PeoplePanel.load();
    },
    async nextPage() {
        state.page += 1;
        await PeoplePanel.load();
    }
};

const AcceptanceSamples = {
    init() {
        els.acceptanceSampleSelect.insertAdjacentHTML("beforeend", ACCEPTANCE_SAMPLES.map((sample, index) => `
            <option value="${index}">${Format.html(sample.label)}</option>
        `).join(""));
    },
    async loadSelected() {
        const selectedIndex = Number(els.acceptanceSampleSelect.value);
        const sample = ACCEPTANCE_SAMPLES[selectedIndex];
        if (!sample) {
            setStatus(TEXT.chooseAcceptanceSample);
            return;
        }
        setStatus(TEXT.loadingAcceptanceSample);
        state.selectedOrgCode = "";
        state.keyword = sample.personCode;
        state.page = 1;
        els.keywordInput.value = sample.personCode;
        els.batchYearInput.value = sample.year;
        els.batchMonthInput.value = sample.month;
        els.batchChangeTypeInput.value = sample.changeType;
        els.orgTree.innerHTML = OrgPanel.render(state.orgs);
        await PeoplePanel.load();
        await PersonDetail.selectPerson(sample.personCode);
        await PersonDetail.trialCalculate();
        setStatus(Format.text(TEXT.acceptanceSampleLoaded, {
            personCode: sample.personCode,
            year: sample.year,
            month: String(sample.month).padStart(2, "0")
        }));
    }
};

const PersonDetail = {
    renderProfile(person) {
        const fields = [
            [TEXT.fields.org, `${person.orgName || ""} ${person.orgCode || ""}`],
            [TEXT.fields.idCard, person.idCard],
            [TEXT.fields.gender, person.gender],
            [TEXT.fields.birthDate, person.birthDate],
            [TEXT.fields.personCategory, person.personCategory],
            [TEXT.fields.organizationType, person.organizationType],
            [TEXT.fields.postCategory, person.postCategory],
            [TEXT.fields.workStartDate, person.workStartDate],
            [TEXT.fields.joinOrgDate, person.joinOrgDate],
            [TEXT.fields.currentPost, person.currentPost],
            [TEXT.fields.postLevel, person.postLevel],
            [TEXT.fields.postStartDate, person.postStartDate],
            [TEXT.fields.workYears, person.workYears],
            [TEXT.fields.education, person.education],
            [TEXT.fields.politicalStatus, person.politicalStatus],
            [TEXT.fields.nation, person.nation],
            [TEXT.fields.bankAccount, person.bankAccount]
        ];
        els.personName.textContent = person.personName || "-";
        els.personMeta.textContent = `${person.personCode} | ${person.orgName || person.orgCode || "-"}`;
        els.profileGrid.innerHTML = fields.map(([label, value]) => `
            <div>
                <dt>${Format.html(label)}</dt>
                <dd>${Format.html(value || "-")}</dd>
            </div>
        `).join("");
    },
    renderBaseInfo(info) {
        els.basePersonCategoryInput.value = info?.personCategory || "";
        els.baseOrganizationTypeInput.value = info?.organizationType || "";
        els.basePostCategoryInput.value = info?.postCategory || "";
        els.baseWorkStartInput.value = info?.workStartDate || "";
        els.baseJoinOrgInput.value = info?.joinOrgDate || "";
        els.baseTeacherNurseStartInput.value = info?.teacherNurseStartDate || "";
        els.baseTeacherNurseFixedInput.value = info?.teacherNurseFixedYears ?? 0;
        els.baseEducationCodeInput.value = info?.educationCode || "";
        els.baseEducationInput.value = info?.education || "";
        els.baseRankCodeInput.value = info?.rankCode || "";
        els.baseCurrentPostInput.value = info?.currentPost || "";
        els.basePostLevelInput.value = info?.postLevel || "";
        els.basePostStartInput.value = info?.postStartDate || "";
        els.baseInfoSummaryInput.value = "";
    },
    renderBaseStatus(status) {
        if (!status) {
            els.baseStatusSummary.innerHTML = `<div class="loading compact">${TEXT.loadingBaseStatus}</div>`;
            return;
        }
        const dirty = String(status.todoCacheStatus || "").toUpperCase() === "DIRTY";
        const canRefreshTodoCache = dirty && Permissions.has("SALARY_TODO");
        els.baseStatusSummary.innerHTML = `
            <div class="base-status-item">
                <strong>${Format.html(status.postCount ?? 0)}</strong>
                <span>\u4efb\u804c</span>
            </div>
            <div class="base-status-item">
                <strong>${Format.html(status.educationCount ?? 0)}</strong>
                <span>\u5b66\u5386</span>
            </div>
            <div class="base-status-item">
                <strong>${Format.html(status.assessmentCount ?? 0)}</strong>
                <span>\u8003\u6838</span>
            </div>
            <div class="base-status-item wide ${dirty ? "dirty" : ""}">
                <strong>${dirty ? "\u5f85\u5237\u65b0" : Format.html(status.todoCacheStatus || "-")}</strong>
                <span>${Format.html(status.todoCacheDirtyAt || status.todoCacheRefreshedAt || "-")}</span>
            </div>
            <div class="base-status-item wide">
                <strong>${Format.html(baseChangeTypeLabel(status.latestChangeType || "") || "-")}</strong>
                <span>${Format.html(status.latestChangeSummary || "-")}</span>
            </div>
            ${canRefreshTodoCache ? `
                <button type="button" class="base-status-action" data-refresh-base-status-cache>
                    ${TEXT.refreshTodoCacheAction}
                </button>
            ` : ""}
        `;
    },
    async loadBaseInfo() {
        if (!state.selectedPersonCode) {
            return;
        }
        PersonDetail.renderBaseInfo(await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/base-info`));
    },
    async loadBaseStatus() {
        if (!state.selectedPersonCode) {
            return;
        }
        PersonDetail.renderBaseStatus(await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/base-status`));
    },
    async refreshTodoCacheFromBaseStatus(button) {
        if (!Permissions.has("SALARY_TODO")) {
            setStatus(TEXT.menuPlaceholder);
            return;
        }
        button.disabled = true;
        setStatus(TEXT.refreshingTodoCache);
        try {
            const metric = await Api.request("/api/workbench/salary-todo-cache/refresh", {
                method: "POST"
            });
            if (state.workbench?.metrics) {
                WorkbenchPanel.updateMetric(metric);
                state.workbenchTodoLoaded = 0;
                await WorkbenchPanel.loadPage("TODO", true);
            }
            await PersonDetail.loadBaseStatus();
            setStatus(TEXT.todoCacheRefreshed);
        } catch (error) {
            setStatus(error.message);
        } finally {
            button.disabled = false;
        }
    },
    renderBaseChanges(records) {
        const items = records || [];
        if (!items.length) {
            els.baseChangeList.innerHTML = `<div class="loading">${TEXT.noBaseChanges}</div>`;
            return;
        }
        els.baseChangeList.innerHTML = items.map((item) => {
            const period = item.changeYear
                ? `${item.changeYear}${item.changeMonth ? `-${String(item.changeMonth).padStart(2, "0")}` : ""}`
                : "-";
            return `
                <div class="base-change-row">
                    <span class="base-change-main">
                        <strong>${Format.html(baseChangeTypeLabel(item.dataType))}</strong>
                        <span>${Format.html(period)}</span>
                    </span>
                    <span class="base-change-sub">
                        ${Format.html(item.summary || "-")} | ${Format.html(item.createdBy || "-")} ${Format.html(item.createdAt || "")}
                    </span>
                    ${item.sourceId ? `<span class="base-change-sub">${Format.html(item.sourceTable || item.dataType || "-")}#${Format.html(item.sourceId)}</span>` : ""}
                </div>
            `;
        }).join("");
    },
    async loadBaseChanges() {
        if (!state.selectedPersonCode) {
            return;
        }
        els.baseChangeList.innerHTML = `<div class="loading">${TEXT.loadingBaseChanges}</div>`;
        els.baseStatusSummary.innerHTML = `<div class="loading compact">${TEXT.loadingBaseStatus}</div>`;
        const records = await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/base-changes?limit=20`);
        PersonDetail.renderBaseChanges(records);
    },
    renderPosts(records) {
        const items = records || [];
        state.personPosts = items;
        if (!items.length) {
            els.personPostList.innerHTML = `<div class="loading">${TEXT.noPersonPosts}</div>`;
            return;
        }
        els.personPostList.innerHTML = items.map((item) => `
            <button type="button" class="person-post-row ${String(item.id) === els.personPostIdInput.value ? "active" : ""}" data-post-id="${Format.html(item.id)}">
                <span class="person-post-main">
                    <strong>${Format.html(item.startDate || "-")} ${Format.html(item.postName || item.postCode || "-")}</strong>
                    <span>${Format.html(item.postCode || "-")}</span>
                </span>
                <span class="person-post-sub">
                    ${Format.html(item.postLevel || "-")} | \u73b0\u4efb ${Format.html(item.currentPostFlag || "-")} | \u6263\u51cf ${Format.html(item.excludedYears ?? 0)} | ${Format.html(item.payrollFlag || "-")}
                </span>
            </button>
        `).join("");
    },
    async loadPosts() {
        if (!state.selectedPersonCode) {
            return;
        }
        els.personPostList.innerHTML = `<div class="loading">${TEXT.loadingPersonPosts}</div>`;
        PersonDetail.renderPosts(await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/posts`));
    },
    clearPostEditor() {
        els.personPostIdInput.value = "";
        els.personPostCodeInput.value = "";
        els.personPostNameInput.value = "";
        els.personPostLevelInput.value = "";
        els.personPostStartInput.value = "";
        els.personPostRankInput.value = "";
        els.personPostCurrentCodeInput.value = "";
        els.personPostExcludedYearsInput.value = "0";
        els.personPostCurrentFlagInput.value = "";
        els.personPostPayrollFlagInput.value = "";
        els.personPostSummaryInput.value = "";
        document.querySelectorAll(".person-post-row").forEach((button) => button.classList.remove("active"));
    },
    editPost(button) {
        const post = state.personPosts?.find((item) => String(item.id) === String(button.dataset.postId));
        if (!post) {
            return;
        }
        els.personPostIdInput.value = post.id || "";
        els.personPostCodeInput.value = post.postCode || "";
        els.personPostNameInput.value = post.postName || "";
        els.personPostLevelInput.value = post.postLevel || "";
        els.personPostStartInput.value = post.startDate || "";
        els.personPostRankInput.value = post.rankCode || "";
        els.personPostCurrentCodeInput.value = post.currentPostCode || "";
        els.personPostExcludedYearsInput.value = post.excludedYears ?? 0;
        els.personPostCurrentFlagInput.value = post.currentPostFlag || "";
        els.personPostPayrollFlagInput.value = post.payrollFlag || "";
        els.personPostSummaryInput.value = "";
        document.querySelectorAll(".person-post-row").forEach((row) => row.classList.toggle("active", row === button));
    },
    renderEducations(records) {
        const items = records || [];
        state.educations = items;
        if (!items.length) {
            els.educationList.innerHTML = `<div class="loading">${TEXT.noEducations}</div>`;
            return;
        }
        els.educationList.innerHTML = items.map((item) => `
            <button type="button" class="education-row ${String(item.id) === els.educationIdInput.value ? "active" : ""}" data-education-id="${Format.html(item.id)}">
                <span class="education-main">
                    <strong>${Format.html(item.graduationDate || "-")} ${Format.html(item.educationName || item.educationCode || "-")}</strong>
                    <span>${Format.html(item.educationCode || "-")}</span>
                </span>
                <span class="education-sub">
                    ${Format.html(item.educationType || "-")} | ${Format.html(item.school || "-")} | ${Format.html(item.note || "-")}
                </span>
            </button>
        `).join("");
    },
    async loadEducations() {
        if (!state.selectedPersonCode) {
            return;
        }
        els.educationList.innerHTML = `<div class="loading">${TEXT.loadingEducations}</div>`;
        PersonDetail.renderEducations(await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/educations`));
    },
    clearEducationEditor() {
        els.educationIdInput.value = "";
        els.educationCodeInput.value = "";
        els.educationNameInput.value = "";
        els.educationSchoolInput.value = "";
        els.educationEnrollInput.value = "";
        els.educationGraduationInput.value = "";
        els.educationYearsInput.value = "0";
        els.educationTypeInput.value = "";
        els.educationNoteInput.value = "";
        els.educationSummaryInput.value = "";
        document.querySelectorAll(".education-row").forEach((button) => button.classList.remove("active"));
    },
    editEducation(button) {
        const education = state.educations?.find((item) => String(item.id) === String(button.dataset.educationId));
        if (!education) {
            return;
        }
        els.educationIdInput.value = education.id || "";
        els.educationCodeInput.value = education.educationCode || "";
        els.educationNameInput.value = education.educationName || "";
        els.educationSchoolInput.value = education.school || "";
        els.educationEnrollInput.value = education.enrollDate || "";
        els.educationGraduationInput.value = education.graduationDate || "";
        els.educationYearsInput.value = education.studyYears ?? 0;
        els.educationTypeInput.value = education.educationType || "";
        els.educationNoteInput.value = education.note || "";
        els.educationSummaryInput.value = "";
        document.querySelectorAll(".education-row").forEach((row) => row.classList.toggle("active", row === button));
    },
    renderAssessments(records) {
        const items = records || [];
        state.assessments = items;
        if (!items.length) {
            els.assessmentList.innerHTML = `<div class="loading">${TEXT.noAssessments}</div>`;
            return;
        }
        els.assessmentList.innerHTML = items.map((item) => `
            <button type="button" class="assessment-row ${String(item.id) === els.assessmentIdInput.value ? "active" : ""}" data-assessment-id="${Format.html(item.id)}">
                <span class="assessment-main">
                    <strong>${Format.html(item.year || "-")}</strong>
                    <span>${Format.html(item.result || "-")}</span>
                </span>
                <span class="assessment-sub">${Format.html(item.personCode || "")} | ${Format.html(item.orgCode || "")}</span>
            </button>
        `).join("");
    },
    async loadAssessments() {
        if (!state.selectedPersonCode) {
            return;
        }
        els.assessmentList.innerHTML = `<div class="loading">${TEXT.loadingAssessments}</div>`;
        PersonDetail.renderAssessments(await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/assessments`));
    },
    clearAssessmentEditor() {
        els.assessmentIdInput.value = "";
        els.assessmentYearInput.value = "";
        els.assessmentResultInput.value = "\u5408\u683c";
        els.assessmentSummaryInput.value = "";
        document.querySelectorAll(".assessment-row").forEach((button) => button.classList.remove("active"));
    },
    editAssessment(button) {
        const assessment = state.assessments?.find((item) => String(item.id) === String(button.dataset.assessmentId));
        if (!assessment) {
            return;
        }
        els.assessmentIdInput.value = assessment.id || "";
        els.assessmentYearInput.value = assessment.year || "";
        els.assessmentResultInput.value = assessment.result || "\u5408\u683c";
        els.assessmentSummaryInput.value = "";
        document.querySelectorAll(".assessment-row").forEach((row) => row.classList.toggle("active", row === button));
    },
    renderHistory(records) {
        state.salaryHistoryRecords = records || [];
        if (!records.length) {
            els.salaryHistory.innerHTML = `<div class="loading">${TEXT.noHistory}</div>`;
            return;
        }
        els.salaryHistory.innerHTML = records.map((item) => `
            <button type="button" class="history-row ${state.selectedHistoryId === item.id ? "active" : ""}" data-history-id="${Format.html(item.id)}">
                <span class="history-main">
                    <strong>${item.year}-${String(item.month).padStart(2, "0")}</strong>
                    <span class="amount">${Format.amount(item.totalAmount)}</span>
                </span>
                <span class="history-sub">${Format.html(item.changeType || TEXT.salaryRecord)}</span>
            </button>
        `).join("");
    },
    renderSalary(result, title) {
        els.salaryTitle.textContent = title;
        els.salaryTotal.textContent = `${result.year}-${String(result.month).padStart(2, "0")} | ${TEXT.total} ${Format.amount(result.totalAmount)}`;
        if (!result.details?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noSalary}</div>`;
            return;
        }
        els.salaryDetails.innerHTML = result.details.map((item) => {
            const amount = Number(item.amount || 0);
            return `
                <div class="salary-row">
                    <span class="salary-main">
                        <strong>${Format.html(item.itemName)}</strong>
                        <span class="amount ${amount < 0 ? "negative" : ""}">${Format.amount(amount)}</span>
                    </span>
                    <span class="salary-sub">${Format.html(item.itemCode)}${item.ruleNote ? ` | ${Format.html(item.ruleNote)}` : ""}</span>
                </div>
            `;
        }).join("");
    },
    renderReconcile(result) {
        els.salaryTitle.textContent = TEXT.reconcileResult;
        els.salaryTotal.textContent = `${result.year}-${String(result.month).padStart(2, "0")} | ${result.passed ? TEXT.reconcilePassed : TEXT.reconcileFailed} | \u5dee\u989d ${Format.amount(result.difference)}`;
        if (!result.details?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noSalary}</div>`;
            return;
        }
        els.salaryDetails.innerHTML = result.details.map((item) => `
            <div class="salary-row reconcile-row ${item.passed ? "passed" : "failed"}">
                <span class="salary-main">
                    <strong>${Format.html(item.itemName)}</strong>
                    <span class="amount ${Number(item.difference || 0) !== 0 ? "negative" : ""}">\u5dee\u989d ${Format.amount(item.difference)}</span>
                </span>
                <span class="salary-sub">
                    ${Format.html(item.itemCode)} | \u8001\u7cfb\u7edf ${Format.amount(item.legacyAmount)} | \u8bd5\u7b97 ${Format.amount(item.calculatedAmount)}
                </span>
            </div>
        `).join("");
    },
    renderNormalGradeTrial(result) {
        els.salaryTitle.textContent = TEXT.ruleTrial;
        const trialStatus = result.expectedHistoryId ? (result.matchedExpected ? TEXT.ruleTrialPassed : TEXT.ruleTrialFailed) : TEXT.ruleTrialNoExpected;
        els.salaryTotal.textContent = `${result.year}-${String(result.month).padStart(2, "0")} | ${trialStatus} | \u8bd5\u7b97 ${Format.amount(result.calculatedTotalAmount)} | \u5386\u53f2 ${Format.amount(result.expectedTotalAmount)} | \u5dee\u989d ${Format.amount(result.differenceWithExpected)}`;
        if (!result.changes?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noSalary}</div>`;
            return;
        }
        els.salaryDetails.innerHTML = result.changes.map((item) => `
            <div class="salary-row reconcile-row ${Number(item.difference || 0) === 0 ? "passed" : "failed"}">
                <span class="salary-main">
                    <strong>${Format.html(item.itemName || item.itemCode)}</strong>
                    <span class="amount ${Number(item.difference || 0) !== 0 ? "negative" : ""}">\u5dee\u989d ${Format.amount(item.difference)}</span>
                </span>
                <span class="salary-sub">
                    ${Format.html(item.itemCode)} | ${Format.html(item.beforeValue || "-")} -> ${Format.html(item.afterValue || "-")} | ${Format.amount(item.beforeAmount)} -> ${Format.amount(item.afterAmount)}${item.ruleNote ? ` | ${Format.html(item.ruleNote)}` : ""}
                </span>
            </div>
        `).join("");
    },
    renderTimeline(result) {
        const summary = TEXT.timelineSummary
            .replace("{checked}", result.checkedCount)
            .replace("{matched}", result.matchedCount)
            .replace("{different}", result.differentCount)
            .replace("{errors}", result.errorCount);
        els.salaryTitle.textContent = TEXT.timeline;
        els.salaryTotal.textContent = summary;
        if (!result.items?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noHistory}</div>`;
            return;
        }
        els.salaryDetails.innerHTML = result.items.map((item) => {
            const failed = item.status !== "MATCH";
            const firstChange = item.changes?.[0];
            const note = firstChange
                ? `${Format.html(firstChange.itemName || firstChange.itemCode)} | ${Format.html(firstChange.beforeValue || "-")} -> ${Format.html(firstChange.afterValue || "-")} | ${Format.html(firstChange.ruleNote || "")}`
                : Format.html(item.message || "");
            return `
                <div class="salary-row reconcile-row ${failed ? "failed" : "passed"}">
                    <span class="salary-main">
                        <strong>${item.year}-${String(item.month).padStart(2, "0")} ${Format.html(item.changeType || TEXT.salaryRecord)}</strong>
                        <span class="amount ${Number(item.differenceWithExpected || 0) !== 0 ? "negative" : ""}">${Format.html(item.status)} | ${Format.amount(item.differenceWithExpected)}</span>
                    </span>
                    <span class="salary-sub">
                        \u5386\u53f2 ${Format.amount(item.historyTotalAmount)} | \u91cd\u7b97 ${Format.amount(item.calculatedTotalAmount)} | ${note}
                    </span>
                </div>
            `;
        }).join("");
    },
    renderGeneratedTimeline(result) {
        const summary = TEXT.generatedTimelineSummary
            .replace("{expected}", result.expectedCount)
            .replace("{matched}", result.matchedCount)
            .replace("{different}", result.differentCount)
            .replace("{missing}", result.missingHistoryCount)
            .replace("{errors}", result.errorCount)
            .replace("{unsupported}", result.unsupportedHistoryCount);
        els.salaryTitle.textContent = TEXT.generatedTimeline;
        els.salaryTotal.textContent = summary;
        if (!result.items?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noHistory}</div>`;
            return;
        }
        const coverageHtml = result.coverage?.length
            ? `<div class="salary-section-title">\u751f\u6210\u8986\u76d6</div>${result.coverage.map((item) => {
                const hasIssue = Number(item.missingHistoryCount || 0) || Number(item.unsupportedHistoryCount || 0);
                return `<div class="salary-row reconcile-row ${hasIssue ? "failed" : "passed"}">
                    <span class="salary-main">
                        <strong>${Format.html(item.changeType || "-")}</strong>
                        <span>${hasIssue ? "\u9700\u5173\u6ce8" : "\u5df2\u8986\u76d6"}</span>
                    </span>
                    <span class="salary-sub">
                        \u5e94\u53d1 ${Format.html(item.expectedCount || 0)} | \u5339\u914d\u5386\u53f2 ${Format.html(item.matchedHistoryCount || 0)} | \u7f3a\u5386\u53f2 ${Format.html(item.missingHistoryCount || 0)} | \u975e\u751f\u6210\u5386\u53f2 ${Format.html(item.unsupportedHistoryCount || 0)}
                    </span>
                </div>`;
            }).join("")}<div class="salary-section-title">\u660e\u7ec6</div>`
            : "";
        els.salaryDetails.innerHTML = coverageHtml + result.items.map((item) => {
            const failed = item.status !== "MATCH";
            const firstChange = item.changes?.[0];
            const note = firstChange
                ? `${Format.html(firstChange.itemName || firstChange.itemCode)} | ${Format.html(firstChange.beforeValue || "-")} -> ${Format.html(firstChange.afterValue || "-")} | ${Format.html(firstChange.ruleNote || "")}`
                : Format.html(item.message || item.note || "");
            return `
                <div class="salary-row reconcile-row ${failed ? "failed" : "passed"}">
                    <span class="salary-main">
                        <strong>${item.year}-${String(item.month).padStart(2, "0")} ${Format.html(item.changeType || TEXT.salaryRecord)}</strong>
                        <span class="amount ${Number(item.differenceWithExpected || 0) !== 0 ? "negative" : ""}">${Format.html(item.status)} | ${Format.amount(item.differenceWithExpected)}</span>
                    </span>
                    <span class="salary-sub">
                        ${Format.html(item.source || "-")}#${Format.html(item.sourceId || "-")} | \u5386\u53f2 ${Format.amount(item.historyTotalAmount)} | \u91cd\u7b97 ${Format.amount(item.calculatedTotalAmount)} | ${note}
                    </span>
                </div>
            `;
        }).join("");
    },
    renderBatchReconcile(result) {
        els.emptyState.classList.add("hidden");
        els.detailContent.classList.remove("hidden");
        els.personName.textContent = TEXT.batchReconcile;
        els.personMeta.textContent = `${result.orgCode} | ${result.year}-${String(result.month).padStart(2, "0")}`;
        els.profileGrid.innerHTML = "";
        els.salaryHistory.innerHTML = `<div class="loading">${TEXT.batchReconcileSummary
            .replace("{checked}", result.checkedCount)
            .replace("{passed}", result.passedCount)
            .replace("{failed}", result.failedCount)
            .replace("{skipped}", result.skippedCount)
            .replace("{diff}", Format.amount(result.totalDifference))}</div>`;
        els.salaryTitle.textContent = TEXT.batchReconcile;
        els.salaryTotal.textContent = TEXT.batchReconcileSummary
            .replace("{checked}", result.checkedCount)
            .replace("{passed}", result.passedCount)
            .replace("{failed}", result.failedCount)
            .replace("{skipped}", result.skippedCount)
            .replace("{diff}", Format.amount(result.totalDifference));
        if (!result.items?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noPeople}</div>`;
            return;
        }
        els.salaryDetails.innerHTML = result.items.map((item) => `
            <div class="salary-row reconcile-row ${item.status === "PASSED" ? "passed" : "failed"}">
                <span class="salary-main">
                    <strong>${Format.html(item.personName)} ${Format.html(item.personCode)}</strong>
                    <span class="amount ${Number(item.difference || 0) !== 0 ? "negative" : ""}">${Format.html(item.status)} | ${Format.amount(item.difference)}</span>
                </span>
                <span class="salary-sub">
                    ${Format.html(item.orgName || item.orgCode)} | \u8001\u7cfb\u7edf ${Format.amount(item.legacyTotalAmount)} | \u8bd5\u7b97 ${Format.amount(item.calculatedTotalAmount)} | ${Format.html(item.message || "")}
                </span>
            </div>
        `).join("");
    },
    renderNormalGradeBatch(result) {
        const summary = TEXT.normalGradeBatchSummary
            .replace("{checked}", result.checkedCount)
            .replace("{matched}", result.matchedCount)
            .replace("{different}", result.differentCount)
            .replace("{noExpected}", result.noExpectedCount)
            .replace("{skipped}", result.skippedCount)
            .replace("{levelPromotion}", result.levelPromotionCount)
            .replace("{notEligible}", result.notEligibleCount)
            .replace("{reverseStep}", result.reverseStepCount)
            .replace("{diff}", Format.amount(result.totalDifference));
        els.emptyState.classList.add("hidden");
        els.detailContent.classList.remove("hidden");
        els.personName.textContent = TEXT.normalGradeBatch;
        els.personMeta.textContent = `${result.orgCode} | ${result.year}-${String(result.month).padStart(2, "0")}`;
        els.profileGrid.innerHTML = "";
        els.salaryHistory.innerHTML = `<div class="loading">${summary}</div>`;
        els.salaryTitle.textContent = TEXT.normalGradeBatch;
        els.salaryTotal.textContent = summary;
        if (!result.items?.length) {
            els.salaryDetails.innerHTML = `<div class="loading">${TEXT.noPeople}</div>`;
            return;
        }
        els.salaryDetails.innerHTML = result.items.map((item) => {
            const failed = item.status !== "MATCHED" && item.status !== "NO_EXPECTED";
            return `
                <div class="salary-row reconcile-row ${failed ? "failed" : "passed"}">
                    <span class="salary-main">
                        <strong>${Format.html(item.personName)} ${Format.html(item.personCode)}</strong>
                        <span class="amount ${Number(item.differenceWithExpected || 0) !== 0 ? "negative" : ""}">${Format.html(item.status)} | ${Format.amount(item.differenceWithExpected)}</span>
                    </span>
                    <span class="salary-sub">
                        ${Format.html(item.orgName || item.orgCode)} | ${Format.html(item.ruleType || "-")} | ${Format.html(item.beforeValue || "-")} -> ${Format.html(item.afterValue || "-")} | \u589e\u989d ${Format.amount(item.changeAmount)} | \u8bd5\u7b97 ${Format.amount(item.calculatedTotalAmount)} | \u5386\u53f2 ${Format.amount(item.expectedTotalAmount)} | ${Format.html(item.ruleNote || item.message || "")}
                    </span>
                </div>
            `;
        }).join("");
    },
    async selectPerson(personCode) {
        state.selectedPersonCode = personCode;
        state.selectedHistoryId = "";
        els.emptyState.classList.add("hidden");
        els.detailContent.classList.remove("hidden");
        els.salaryDetails.innerHTML = `<div class="loading">${TEXT.chooseSalary}</div>`;
        els.salaryTitle.textContent = TEXT.salaryDetail;
        els.salaryTotal.textContent = "-";
        PeoplePanel.render({ records: state.people, total: els.peopleTotal.textContent, page: state.page, size: state.size });

        els.baseChangeList.innerHTML = `<div class="loading">${TEXT.loadingBaseChanges}</div>`;
        els.personPostList.innerHTML = `<div class="loading">${TEXT.loadingPersonPosts}</div>`;
        els.educationList.innerHTML = `<div class="loading">${TEXT.loadingEducations}</div>`;
        els.assessmentList.innerHTML = `<div class="loading">${TEXT.loadingAssessments}</div>`;
        PersonDetail.clearPostEditor();
        PersonDetail.clearEducationEditor();
        PersonDetail.clearAssessmentEditor();

        const [person, baseInfo, baseStatus, history, baseChanges, posts, educations, assessments] = await Promise.all([
            Api.request(`/api/persons/${encodeURIComponent(personCode)}`),
            Api.request(`/api/persons/${encodeURIComponent(personCode)}/base-info`),
            Api.request(`/api/persons/${encodeURIComponent(personCode)}/base-status`),
            Api.request(`/api/salary/history/${encodeURIComponent(personCode)}`),
            Api.request(`/api/persons/${encodeURIComponent(personCode)}/base-changes?limit=20`),
            Api.request(`/api/persons/${encodeURIComponent(personCode)}/posts`),
            Api.request(`/api/persons/${encodeURIComponent(personCode)}/educations`),
            Api.request(`/api/persons/${encodeURIComponent(personCode)}/assessments`)
        ]);
        PersonDetail.renderProfile(person);
        PersonDetail.renderBaseInfo(baseInfo);
        PersonDetail.renderBaseStatus(baseStatus);
        PersonDetail.renderHistory(history);
        PersonDetail.renderBaseChanges(baseChanges);
        PersonDetail.renderPosts(posts);
        PersonDetail.renderEducations(educations);
        PersonDetail.renderAssessments(assessments);
        setStatus(Format.text(TEXT.selected, { name: person.personName }));
    },
    async saveBaseInfo(event) {
        event.preventDefault();
        if (!state.selectedPersonCode) {
            return;
        }
        const payload = {
            personCategory: els.basePersonCategoryInput.value.trim(),
            organizationType: els.baseOrganizationTypeInput.value.trim(),
            postCategory: els.basePostCategoryInput.value.trim(),
            workStartDate: els.baseWorkStartInput.value.trim(),
            joinOrgDate: els.baseJoinOrgInput.value.trim(),
            teacherNurseStartDate: els.baseTeacherNurseStartInput.value.trim(),
            teacherNurseFixedYears: Number(els.baseTeacherNurseFixedInput.value || 0),
            educationCode: els.baseEducationCodeInput.value.trim(),
            education: els.baseEducationInput.value.trim(),
            rankCode: els.baseRankCodeInput.value.trim(),
            currentPost: els.baseCurrentPostInput.value.trim(),
            postLevel: els.basePostLevelInput.value.trim(),
            postStartDate: els.basePostStartInput.value.trim(),
            summary: els.baseInfoSummaryInput.value.trim()
        };
        try {
            els.saveBaseInfoButton.disabled = true;
            setStatus(TEXT.savingBaseInfo);
            const saved = await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/base-info`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });
            PersonDetail.renderBaseInfo(saved);
            PersonDetail.renderProfile(await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}`));
            await Promise.all([PersonDetail.loadBaseChanges(), PersonDetail.loadBaseStatus()]);
            if (Permissions.has("SALARY_TODO") && state.workbench?.metrics) {
                const metric = await Api.request("/api/workbench/metrics/salary-todo");
                WorkbenchPanel.updateMetric(metric);
            }
            WorkbenchPanel.markMaintenanceReturnDirty();
            setStatus(TEXT.baseInfoSaved);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.saveBaseInfoButton.disabled = false;
        }
    },
    async saveBaseChange(event) {
        event.preventDefault();
        if (!state.selectedPersonCode) {
            return;
        }
        const summary = els.baseChangeSummaryInput.value.trim();
        if (!summary) {
            setStatus(TEXT.baseChangeSummaryRequired);
            els.baseChangeSummaryInput.focus();
            return;
        }
        const dataType = els.baseChangeTypeInput.value;
        const payload = {
            dataType,
            sourceTable: dataType,
            sourceId: els.baseChangeSourceIdInput.value.trim(),
            summary
        };
        const year = Number(els.baseChangeYearInput.value || 0);
        const month = Number(els.baseChangeMonthInput.value || 0);
        if (year) {
            payload.changeYear = year;
        }
        if (month) {
            payload.changeMonth = month;
        }
        try {
            els.saveBaseChangeButton.disabled = true;
            setStatus(TEXT.savingBaseChange);
            await Api.request(`/api/persons/${encodeURIComponent(state.selectedPersonCode)}/base-changes`, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            els.baseChangeSummaryInput.value = "";
            els.baseChangeSourceIdInput.value = "";
            await Promise.all([PersonDetail.loadBaseChanges(), PersonDetail.loadBaseStatus()]);
            if (Permissions.has("SALARY_TODO") && state.workbench?.metrics) {
                const metric = await Api.request("/api/workbench/metrics/salary-todo");
                WorkbenchPanel.updateMetric(metric);
            }
            WorkbenchPanel.markMaintenanceReturnDirty();
            setStatus(TEXT.baseChangeSaved);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.saveBaseChangeButton.disabled = false;
        }
    },
    async savePost(event) {
        event.preventDefault();
        if (!state.selectedPersonCode) {
            return;
        }
        const postCode = els.personPostCodeInput.value.trim();
        const startDate = els.personPostStartInput.value.trim();
        if (!postCode || !startDate) {
            setStatus(TEXT.personPostRequired);
            return;
        }
        const payload = {
            postCode,
            postName: els.personPostNameInput.value.trim(),
            postLevel: els.personPostLevelInput.value.trim(),
            rankCode: els.personPostRankInput.value.trim(),
            currentPostCode: els.personPostCurrentCodeInput.value.trim(),
            startDate,
            excludedYears: Number(els.personPostExcludedYearsInput.value || 0),
            currentPostFlag: els.personPostCurrentFlagInput.value.trim(),
            payrollFlag: els.personPostPayrollFlagInput.value.trim(),
            summary: els.personPostSummaryInput.value.trim()
        };
        const id = els.personPostIdInput.value.trim();
        try {
            els.savePersonPostButton.disabled = true;
            setStatus(TEXT.savingPersonPost);
            await Api.request(id
                    ? `/api/persons/posts/${encodeURIComponent(id)}`
                    : `/api/persons/${encodeURIComponent(state.selectedPersonCode)}/posts`,
                {
                    method: id ? "PUT" : "POST",
                    body: JSON.stringify(payload)
                });
            PersonDetail.clearPostEditor();
            await Promise.all([PersonDetail.loadPosts(), PersonDetail.loadBaseChanges(), PersonDetail.loadBaseStatus()]);
            if (Permissions.has("SALARY_TODO") && state.workbench?.metrics) {
                const metric = await Api.request("/api/workbench/metrics/salary-todo");
                WorkbenchPanel.updateMetric(metric);
            }
            WorkbenchPanel.markMaintenanceReturnDirty();
            setStatus(TEXT.personPostSaved);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.savePersonPostButton.disabled = false;
        }
    },
    async saveEducation(event) {
        event.preventDefault();
        if (!state.selectedPersonCode) {
            return;
        }
        const educationCode = els.educationCodeInput.value.trim();
        const graduationDate = els.educationGraduationInput.value.trim();
        if (!educationCode || !graduationDate) {
            setStatus(TEXT.educationRequired);
            return;
        }
        const payload = {
            educationCode,
            educationName: els.educationNameInput.value.trim(),
            school: els.educationSchoolInput.value.trim(),
            enrollDate: els.educationEnrollInput.value.trim(),
            graduationDate,
            studyYears: Number(els.educationYearsInput.value || 0),
            educationType: els.educationTypeInput.value.trim(),
            note: els.educationNoteInput.value.trim(),
            summary: els.educationSummaryInput.value.trim()
        };
        const id = els.educationIdInput.value.trim();
        try {
            els.saveEducationButton.disabled = true;
            setStatus(TEXT.savingEducation);
            await Api.request(id
                    ? `/api/persons/educations/${encodeURIComponent(id)}`
                    : `/api/persons/${encodeURIComponent(state.selectedPersonCode)}/educations`,
                {
                    method: id ? "PUT" : "POST",
                    body: JSON.stringify(payload)
                });
            PersonDetail.clearEducationEditor();
            await Promise.all([PersonDetail.loadEducations(), PersonDetail.loadBaseChanges(), PersonDetail.loadBaseStatus()]);
            if (Permissions.has("SALARY_TODO") && state.workbench?.metrics) {
                const metric = await Api.request("/api/workbench/metrics/salary-todo");
                WorkbenchPanel.updateMetric(metric);
            }
            WorkbenchPanel.markMaintenanceReturnDirty();
            setStatus(TEXT.educationSaved);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.saveEducationButton.disabled = false;
        }
    },
    async saveAssessment(event) {
        event.preventDefault();
        if (!state.selectedPersonCode) {
            return;
        }
        const year = els.assessmentYearInput.value.trim();
        const result = els.assessmentResultInput.value.trim();
        if (!year || !result) {
            setStatus(TEXT.assessmentRequired);
            return;
        }
        const payload = {
            year,
            result,
            summary: els.assessmentSummaryInput.value.trim()
        };
        const id = els.assessmentIdInput.value.trim();
        try {
            els.saveAssessmentButton.disabled = true;
            setStatus(TEXT.savingAssessment);
            await Api.request(id
                    ? `/api/persons/assessments/${encodeURIComponent(id)}`
                    : `/api/persons/${encodeURIComponent(state.selectedPersonCode)}/assessments`,
                {
                    method: id ? "PUT" : "POST",
                    body: JSON.stringify(payload)
                });
            PersonDetail.clearAssessmentEditor();
            await Promise.all([PersonDetail.loadAssessments(), PersonDetail.loadBaseChanges(), PersonDetail.loadBaseStatus()]);
            if (Permissions.has("SALARY_TODO") && state.workbench?.metrics) {
                const metric = await Api.request("/api/workbench/metrics/salary-todo");
                WorkbenchPanel.updateMetric(metric);
            }
            WorkbenchPanel.markMaintenanceReturnDirty();
            setStatus(TEXT.assessmentSaved);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.saveAssessmentButton.disabled = false;
        }
    },
    async selectHistory(historyId) {
        state.selectedHistoryId = historyId;
        const detail = await Api.request(`/api/salary/history-records/${encodeURIComponent(historyId)}`);
        document.querySelectorAll(".history-row").forEach((button) => {
            button.classList.toggle("active", button.dataset.historyId === state.selectedHistoryId);
        });
        PersonDetail.renderSalary(detail, TEXT.salaryDetail);
    },
    async trialCalculate() {
        if (!Permissions.guard("SALARY_TRIAL")) {
            return;
        }
        if (!state.selectedPersonCode) {
            return;
        }
        const history = state.salaryHistoryRecords.find((item) => item.id === state.selectedHistoryId);
        const payload = {
            personCode: state.selectedPersonCode,
            orgCode: state.selectedPersonCode.split("-")[0],
            year: history?.year || Number(els.batchYearInput.value || 2024),
            month: history?.month || Number(els.batchMonthInput.value || 7)
        };
        const changeType = history?.changeType || batchChangeType();
        if (changeType) {
            payload.changeType = changeType;
        }
        setStatus(TEXT.loadingRuleTrial);
        const result = await Api.request("/api/salary/rule-trial/normal-grade", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        PersonDetail.renderNormalGradeTrial(result);
        const trialStatus = result.expectedHistoryId ? (result.matchedExpected ? TEXT.ruleTrialPassed : TEXT.ruleTrialFailed) : TEXT.ruleTrialNoExpected;
        setStatus(`${TEXT.ruleTrial} | ${trialStatus} | \u5dee\u989d ${Format.amount(result.differenceWithExpected)}`);
    },
    async reconcileSelectedHistory() {
        if (!Permissions.guard("SALARY_RECONCILE")) {
            return;
        }
        if (!state.selectedPersonCode || !state.selectedHistoryId) {
            setStatus(TEXT.chooseHistoryForReconcile);
            return;
        }
        const history = state.salaryHistoryRecords.find((item) => item.id === state.selectedHistoryId);
        if (!history) {
            setStatus(TEXT.chooseHistoryForReconcile);
            return;
        }
        const result = await Api.request("/api/salary/reconcile", {
            method: "POST",
            body: JSON.stringify({
                personCode: state.selectedPersonCode,
                orgCode: state.selectedPersonCode.split("-")[0],
                year: history.year,
                month: history.month,
                changeType: history.changeType
            })
        });
        PersonDetail.renderReconcile(result);
    },
    async replayTimeline() {
        if (!state.selectedPersonCode) {
            return;
        }
        setStatus(TEXT.loadingTimeline);
        const result = await Api.request(`/api/salary/timeline/${encodeURIComponent(state.selectedPersonCode)}?limit=120`);
        PersonDetail.renderTimeline(result);
        setStatus(TEXT.timelineSummary
            .replace("{checked}", result.checkedCount)
            .replace("{matched}", result.matchedCount)
            .replace("{different}", result.differentCount)
            .replace("{errors}", result.errorCount));
    },
    async generatedTimeline() {
        if (!state.selectedPersonCode) {
            return;
        }
        setStatus(TEXT.loadingGeneratedTimeline);
        const result = await Api.request(`/api/salary/timeline-generated/${encodeURIComponent(state.selectedPersonCode)}?limit=160`);
        PersonDetail.renderGeneratedTimeline(result);
        setStatus(TEXT.generatedTimelineSummary
            .replace("{expected}", result.expectedCount)
            .replace("{matched}", result.matchedCount)
            .replace("{different}", result.differentCount)
            .replace("{missing}", result.missingHistoryCount)
            .replace("{errors}", result.errorCount)
            .replace("{unsupported}", result.unsupportedHistoryCount));
    },
    async reconcileBatch() {
        if (!Permissions.guard("SALARY_RECONCILE")) {
            return;
        }
        if (!state.selectedOrgCode) {
            setStatus(TEXT.chooseOrgForBatch);
            return;
        }
        const result = await Api.request("/api/salary/reconcile-batch", {
            method: "POST",
            body: JSON.stringify({
                orgCode: state.selectedOrgCode,
                year: Number(els.batchYearInput.value || 2024),
                month: Number(els.batchMonthInput.value || 7),
                limit: Number(els.batchLimitInput.value || 100),
                changeType: batchChangeType() || "BATCH"
            })
        });
        PersonDetail.renderBatchReconcile(result);
        setStatus(TEXT.batchReconcileSummary
            .replace("{checked}", result.checkedCount)
            .replace("{passed}", result.passedCount)
            .replace("{failed}", result.failedCount)
            .replace("{skipped}", result.skippedCount)
            .replace("{diff}", Format.amount(result.totalDifference)));
    },
    async normalGradeBatch() {
        if (!Permissions.guard("SALARY_TRIAL")) {
            return;
        }
        if (!state.selectedOrgCode) {
            setStatus(TEXT.chooseOrgForBatch);
            return;
        }
        const changeType = batchChangeType();
        const payload = {
            orgCode: state.selectedOrgCode,
            year: Number(els.batchYearInput.value || 2024),
            month: Number(els.batchMonthInput.value || 7),
            limit: Number(els.batchLimitInput.value || 100)
        };
        if (changeType) {
            payload.changeType = changeType;
        }
        setStatus(TEXT.loadingNormalGradeBatch);
        const result = await Api.request("/api/salary/rule-trial/normal-grade-batch", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        PersonDetail.renderNormalGradeBatch(result);
        setStatus(TEXT.normalGradeBatchSummary
            .replace("{checked}", result.checkedCount)
            .replace("{matched}", result.matchedCount)
            .replace("{different}", result.differentCount)
            .replace("{noExpected}", result.noExpectedCount)
            .replace("{skipped}", result.skippedCount)
            .replace("{levelPromotion}", result.levelPromotionCount)
            .replace("{notEligible}", result.notEligibleCount)
            .replace("{reverseStep}", result.reverseStepCount)
            .replace("{diff}", Format.amount(result.totalDifference)));
    },
    exportBatchReconcile() {
        if (!Permissions.guard("SALARY_EXPORT")) {
            return;
        }
        if (!state.selectedOrgCode) {
            setStatus(TEXT.chooseOrgForBatch);
            return;
        }
        const params = new URLSearchParams({
            orgCode: state.selectedOrgCode,
            year: Number(els.batchYearInput.value || 2024),
            month: Number(els.batchMonthInput.value || 7),
            limit: Number(els.batchLimitInput.value || 100),
            changeType: batchChangeType() || "BATCH"
        });
        setStatus(TEXT.exportStarted);
        window.location.href = `/api/salary/reconcile-batch.csv?${params.toString()}`;
    },
    exportNormalGradeBatch() {
        if (!Permissions.guard("SALARY_EXPORT")) {
            return;
        }
        if (!state.selectedOrgCode) {
            setStatus(TEXT.chooseOrgForBatch);
            return;
        }
        const params = new URLSearchParams({
            orgCode: state.selectedOrgCode,
            year: Number(els.batchYearInput.value || 2024),
            month: Number(els.batchMonthInput.value || 7),
            limit: Number(els.batchLimitInput.value || 100)
        });
        const changeType = batchChangeType();
        if (changeType) {
            params.set("changeType", changeType);
        }
        setStatus(TEXT.exportNormalGradeStarted);
        window.location.href = `/api/salary/rule-trial/normal-grade-batch.csv?${params.toString()}`;
    }
};

const ConfigPanel = {
    render(items) {
        const selectedButton = state.configMode === "admin"
            ? els.allConfigButton
            : state.configCategory === "10" ? els.institutionConfigButton : els.civilConfigButton;
        [els.civilConfigButton, els.institutionConfigButton, els.allConfigButton].forEach((button) => {
            button.classList.toggle("active", button === selectedButton);
        });

        if (!items.length) {
            els.fieldConfigList.innerHTML = `<div class="loading">${TEXT.noConfig}</div>`;
            return;
        }

        const issueMap = new Map(state.configIssues.map((issue) => [issue.itemCode, issue]));
        els.fieldConfigList.innerHTML = items.map((item) => {
            const issue = issueMap.get(item.itemCode);
            const displayName = item.itemName || `${item.fieldCap || item.itemCode} / ${item.fieldCaps || item.itemCode}`;
            const categoryText = item.category6 ? `${item.category}/${item.category6}` : item.category;
            const activeText = item.activeFlag2006 ? `${item.activeFlag || "-"}/${item.activeFlag2006 || "-"}` : item.activeFlag;
            const auditText = item.auditCount
                ? ` | ${Format.text(TEXT.auditBadge, { count: item.auditCount })}${item.lastChangedAt ? ` ${Format.html(item.lastChangedAt)}` : ""}`
                : "";
            return `
                <div class="field-config-row ${issue ? "needs-review" : ""}">
                    <span class="field-config-main">
                        <strong>${Format.html(displayName)}</strong>
                        <span>${Format.html(item.itemCode)}${issue ? ` | ${Format.html(issue.message)}` : ""}</span>
                    </span>
                    <span class="field-config-sub">
                        category=${Format.html(categoryText || "-")} | sfsy=${Format.html(activeText || "-")} | #${Format.html(item.sequence)}${auditText}
                    </span>
                    <button class="config-edit-button" type="button" data-edit-code="${Format.html(item.itemCode)}">${TEXT.edit}</button>
                </div>
            `;
        }).join("");
    },
    async loadEffective(category = state.configCategory, dwsx = state.configDwsx) {
        if (!Permissions.guard("SALARY_CONFIG")) {
            return;
        }
        state.configMode = "effective";
        state.configCategory = category;
        state.configDwsx = dwsx;
        const year = Number(els.configYearInput.value || new Date().getFullYear());
        els.fieldConfigList.innerHTML = `<div class="loading">${TEXT.loadingConfig}</div>`;
        const params = new URLSearchParams({ category, dwsx, year });
        const [items, issues] = await Promise.all([
            Api.request(`/api/salary/field-configs?${params.toString()}`),
            Api.request(`/api/salary/field-config-issues?year=${encodeURIComponent(year)}`)
        ]);
        state.configIssues = issues;
        ConfigPanel.render(items);
    },
    async loadAll() {
        if (!Permissions.guard("SALARY_CONFIG")) {
            return;
        }
        state.configMode = "admin";
        const year = Number(els.configYearInput.value || new Date().getFullYear());
        els.fieldConfigList.innerHTML = `<div class="loading">${TEXT.loadingAllConfig}</div>`;
        const [items, issues] = await Promise.all([
            Api.request("/api/salary/field-config-admin"),
            Api.request(`/api/salary/field-config-issues?year=${encodeURIComponent(year)}`)
        ]);
        state.configIssues = issues;
        ConfigPanel.render(items);
    },
    hideEditor() {
        state.editingConfigCode = "";
        els.fieldConfigEditor.classList.add("hidden");
    },
    renderEditor(config) {
        state.editingConfigCode = config.itemCode;
        els.configEditorTitle.textContent = Format.text(TEXT.editorTitle, { code: config.itemCode });
        els.configFieldCapInput.value = config.fieldCap || "";
        els.configFieldCapsInput.value = config.fieldCaps || "";
        els.configCategoryInput.value = config.category || "00";
        els.configCategory6Input.value = config.category6 || "00";
        els.configActiveInput.value = config.activeFlag || "";
        els.configActive2006Input.value = config.activeFlag2006 || "";
        els.configSequenceInput.value = config.sequence ?? 0;
        els.fieldConfigEditor.classList.remove("hidden");
    },
    renderAudit(records) {
        if (!records.length) {
            els.configAuditList.innerHTML = `<div class="loading compact">${TEXT.noAudit}</div>`;
            return;
        }
        els.configAuditList.innerHTML = records.map((record) => `
            <div class="audit-row">
                <span class="audit-main">
                    <strong>${Format.html(record.fieldName)}</strong>
                    <span>${Format.html(record.changedBy || "-")}</span>
                </span>
                <span class="audit-sub">${Format.html(record.oldValue || "-")} -> ${Format.html(record.newValue || "-")}</span>
                <span class="audit-time">${Format.html(record.changedAt || "-")}</span>
            </div>
        `).join("");
    },
    async edit(itemCode) {
        if (!Permissions.guard("SALARY_CONFIG")) {
            return;
        }
        setStatus(TEXT.loadingConfigItem);
        els.configAuditList.innerHTML = `<div class="loading compact">${TEXT.loadingAudit}</div>`;
        const [config, audit] = await Promise.all([
            Api.request(`/api/salary/field-configs/${encodeURIComponent(itemCode)}`),
            Api.request(`/api/salary/field-configs/${encodeURIComponent(itemCode)}/audit`)
        ]);
        ConfigPanel.renderEditor(config);
        ConfigPanel.renderAudit(audit);
        setStatus(Format.text(TEXT.editing, { code: itemCode }));
    },
    async save(event) {
        event.preventDefault();
        if (!Permissions.guard("SALARY_CONFIG")) {
            return;
        }
        if (!state.editingConfigCode) {
            return;
        }
        els.saveConfigButton.disabled = true;
        setStatus(TEXT.savingConfig);
        const itemCode = state.editingConfigCode;
        const payload = {
            fieldCap: els.configFieldCapInput.value.trim(),
            fieldCaps: els.configFieldCapsInput.value.trim(),
            category: els.configCategoryInput.value,
            category6: els.configCategory6Input.value,
            activeFlag: els.configActiveInput.value.trim(),
            activeFlag2006: els.configActive2006Input.value.trim(),
            sequence: Number(els.configSequenceInput.value || 0)
        };
        try {
            const savedConfig = await Api.request(`/api/salary/field-configs/${encodeURIComponent(itemCode)}`, {
                method: "PATCH",
                body: JSON.stringify(payload)
            });
            if (state.configMode === "admin") {
                await ConfigPanel.loadAll();
            } else {
                await ConfigPanel.loadEffective();
            }
            ConfigPanel.renderEditor(savedConfig);
            const audit = await Api.request(`/api/salary/field-configs/${encodeURIComponent(itemCode)}/audit`);
            ConfigPanel.renderAudit(audit);
            setStatus(TEXT.savedConfig);
        } catch (error) {
            setStatus(error.message);
        } finally {
            els.saveConfigButton.disabled = false;
        }
    }
};

async function refreshAll() {
    try {
        setStatus(TEXT.refresh);
        await OrgPanel.load();
        await PeoplePanel.load();
        setStatus(TEXT.serviceOk);
    } catch (error) {
        setStatus(TEXT.serviceFail);
        els.peopleList.innerHTML = `<div class="error">${Format.html(error.message)}</div>`;
    }
}

function bindEvents() {
    els.loginForm.addEventListener("submit", (event) => AuthPanel.login(event));
    els.logoutButton.addEventListener("click", () => AuthPanel.logout());
    els.changePasswordToggleButton.addEventListener("click", () => AuthPanel.togglePasswordForm(true));
    els.cancelPasswordButton.addEventListener("click", () => AuthPanel.togglePasswordForm(false));
    els.changePasswordForm.addEventListener("submit", (event) => AuthPanel.changePassword(event));
    els.menuTree.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-menu-code]");
        if (button) {
            await SystemShell.selectView(button.dataset.view, button.dataset.menuCode);
        }
    });
    els.todoWorkItems.addEventListener("click", async (event) => {
        const completeButton = event.target.closest("button[data-complete-work-id]");
        if (completeButton) {
            await WorkbenchPanel.completeWorkItem(completeButton);
            return;
        }
        const button = event.target.closest("button[data-work-id]");
        if (button) {
            await WorkbenchPanel.openWorkItem(button);
        }
    });
    els.doneWorkItems.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-work-id]");
        if (button) {
            await WorkbenchPanel.openWorkItem(button);
        }
    });
    els.historyWritePlans?.addEventListener("click", async (event) => {
        const selectInput = event.target.closest("input[data-history-plan-select-case-no]");
        if (selectInput) {
            const caseNo = selectInput.dataset.historyPlanSelectCaseNo || "";
            if (caseNo && selectInput.checked) {
                state.historyPlanSelected.set(caseNo, {
                    caseNo,
                    personCode: selectInput.dataset.historyPlanSelectPersonCode || "",
                    actionCode: selectInput.dataset.historyPlanSelectActionCode || ""
                });
            } else if (caseNo) {
                state.historyPlanSelected.delete(caseNo);
            }
            persistHistoryPlanQueueState();
            WorkbenchPanel.renderHistoryPlanSummary(state.historyPlanCurrentItems || []);
            return;
        }
        const maintenanceButton = event.target.closest("button[data-open-person-maintenance]");
        if (maintenanceButton) {
            maintenanceButton.disabled = true;
            try {
                await WorkbenchPanel.openPersonMaintenance(maintenanceButton.dataset.personCode, maintenanceButton.dataset.openPersonMaintenance, {
                    caseNo: maintenanceButton.dataset.maintenanceCaseNo,
                    label: maintenanceButton.dataset.maintenanceLabel,
                    reason: maintenanceButton.dataset.maintenanceReason,
                    fields: maintenanceButton.dataset.maintenanceFields
                });
            } catch (error) {
                maintenanceButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const retestButton = event.target.closest("button[data-history-write-comparison-retest-case-no]");
        if (retestButton) {
            retestButton.disabled = true;
            try {
                await WorkbenchPanel.retestHistoryWriteComparison(retestButton.dataset.historyWriteComparisonRetestCaseNo);
            } catch (error) {
                retestButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const retestApproveButton = event.target.closest("button[data-history-write-retest-approve-case-no]");
        if (retestApproveButton) {
            retestApproveButton.disabled = true;
            try {
                await WorkbenchPanel.approveRetestPassedHistoryWriteComparison(retestApproveButton.dataset.historyWriteRetestApproveCaseNo);
            } catch (error) {
                retestApproveButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const comparisonButton = event.target.closest("button[data-history-write-comparison-case-no]");
        if (comparisonButton) {
            await WorkbenchPanel.openHistoryWriteComparison(comparisonButton.dataset.historyWriteComparisonCaseNo);
            return;
        }
        const button = event.target.closest("button[data-history-write-plan-case-no]");
        if (button) {
            await WorkbenchPanel.openHistoryWritePlan(button.dataset.historyWritePlanCaseNo);
        }
    });
    els.orgTree.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-org-code]");
        if (button) {
            await OrgPanel.select(button.dataset.orgCode);
        }
    });
    els.peopleList.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-person-code]");
        if (button) {
            await PersonDetail.selectPerson(button.dataset.personCode);
        }
    });
    els.maintenanceReturnBar?.addEventListener("click", async (event) => {
        const refreshButton = event.target.closest("[data-maintenance-return-refresh]");
        if (refreshButton) {
            refreshButton.disabled = true;
            try {
                await PersonDetail.selectPerson(state.selectedPersonCode);
                WorkbenchPanel.renderMaintenanceReturnBar();
            } finally {
                refreshButton.disabled = false;
            }
            return;
        }
        const returnButton = event.target.closest("[data-maintenance-return-case-no]");
        if (returnButton) {
            returnButton.disabled = true;
            try {
                await WorkbenchPanel.returnToHistoryWriteComparison(returnButton.dataset.maintenanceReturnCaseNo);
            } catch (error) {
                returnButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const retestButton = event.target.closest("[data-maintenance-return-retest]");
        if (retestButton) {
            retestButton.disabled = true;
            try {
                await WorkbenchPanel.retestMaintenanceReturnCurrent();
            } catch (error) {
                retestButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const retestNextButton = event.target.closest("[data-maintenance-return-retest-next]");
        if (retestNextButton) {
            retestNextButton.disabled = true;
            try {
                await WorkbenchPanel.retestMaintenanceReturnAndNext();
            } catch (error) {
                retestNextButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const retestSummaryButton = event.target.closest("[data-maintenance-return-retest-summary]");
        if (retestSummaryButton) {
            retestSummaryButton.disabled = true;
            try {
                await WorkbenchPanel.retestMaintenanceReturnAndSummarize();
            } catch (error) {
                retestSummaryButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const filterMismatchedButton = event.target.closest("[data-maintenance-return-filter-mismatched]");
        if (filterMismatchedButton) {
            filterMismatchedButton.disabled = true;
            try {
                await WorkbenchPanel.filterMaintenanceQueueMismatches();
            } catch (error) {
                filterMismatchedButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const nextSelectedButton = event.target.closest("[data-maintenance-return-next-selected]");
        if (nextSelectedButton) {
            nextSelectedButton.disabled = true;
            try {
                await WorkbenchPanel.openNextSelectedMaintenance();
            } catch (error) {
                nextSelectedButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const clearButton = event.target.closest("[data-maintenance-return-clear]");
        if (clearButton) {
            state.maintenanceReturn = null;
            WorkbenchPanel.renderMaintenanceReturnBar();
        }
    });
    els.salaryHistory.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-history-id]");
        if (button) {
            await PersonDetail.selectHistory(button.dataset.historyId);
        }
    });
    els.personPostList.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-post-id]");
        if (button) {
            PersonDetail.editPost(button);
        }
    });
    els.educationList.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-education-id]");
        if (button) {
            PersonDetail.editEducation(button);
        }
    });
    els.assessmentList.addEventListener("click", (event) => {
        const button = event.target.closest("button[data-assessment-id]");
        if (button) {
            PersonDetail.editAssessment(button);
        }
    });
    els.fieldConfigList.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-edit-code]");
        if (button) {
            await ConfigPanel.edit(button.dataset.editCode);
        }
    });
    els.searchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        await PeoplePanel.search();
    });
    els.personBaseInfoForm.addEventListener("submit", (event) => PersonDetail.saveBaseInfo(event));
    els.baseStatusSummary.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-refresh-base-status-cache]");
        if (button) {
            await PersonDetail.refreshTodoCacheFromBaseStatus(button);
        }
    });
    els.baseChangeForm.addEventListener("submit", (event) => PersonDetail.saveBaseChange(event));
    els.personPostForm.addEventListener("submit", (event) => PersonDetail.savePost(event));
    els.newPersonPostButton.addEventListener("click", () => PersonDetail.clearPostEditor());
    els.cancelPersonPostEditButton.addEventListener("click", () => PersonDetail.clearPostEditor());
    els.educationForm.addEventListener("submit", (event) => PersonDetail.saveEducation(event));
    els.newEducationButton.addEventListener("click", () => PersonDetail.clearEducationEditor());
    els.cancelEducationEditButton.addEventListener("click", () => PersonDetail.clearEducationEditor());
    els.assessmentForm.addEventListener("submit", (event) => PersonDetail.saveAssessment(event));
    els.newAssessmentButton.addEventListener("click", () => PersonDetail.clearAssessmentEditor());
    els.cancelAssessmentEditButton.addEventListener("click", () => PersonDetail.clearAssessmentEditor());
    els.prevPageButton.addEventListener("click", () => PeoplePanel.previousPage());
    els.nextPageButton.addEventListener("click", () => PeoplePanel.nextPage());
    els.refreshButton.addEventListener("click", refreshAll);
    els.refreshTodoCacheButton.addEventListener("click", () => WorkbenchPanel.refreshTodoCache());
    els.workbenchRefreshButton.addEventListener("click", () => WorkbenchPanel.load());
    els.historyPlanRefreshButton?.addEventListener("click", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyPlanClearFiltersButton?.addEventListener("click", () => WorkbenchPanel.clearHistoryPlanFilters());
    els.historyPlanSummary?.addEventListener("click", async (event) => {
        const selectionClearButton = event.target.closest("button[data-history-plan-selection-clear]");
        if (selectionClearButton) {
            state.historyPlanSelected.clear();
            persistHistoryPlanQueueState();
            await WorkbenchPanel.loadHistoryWritePlans();
            return;
        }
        const queueAutoSelectButton = event.target.closest("button[data-history-plan-queue-autoselect]");
        if (queueAutoSelectButton) {
            await WorkbenchPanel.autoSelectHistoryPlanQueue();
            return;
        }
        const queueClearButton = event.target.closest("button[data-history-ledger-clear-queue-filter]");
        if (queueClearButton) {
            await WorkbenchPanel.clearHistoryPlanQueueFilter();
            return;
        }
        const selectionFilterButton = event.target.closest("button[data-history-plan-selection-filter]");
        if (selectionFilterButton) {
            if (els.historyPlanActionSelect) {
                els.historyPlanActionSelect.value = selectionFilterButton.dataset.historyPlanSelectionFilter || "";
            }
            state.historyPlanSelected.clear();
            persistHistoryPlanQueueState();
            await WorkbenchPanel.loadHistoryWritePlans();
            return;
        }
        const selectionRetestButton = event.target.closest("button[data-history-plan-selection-retest]");
        if (selectionRetestButton) {
            await WorkbenchPanel.batchRetestSelectedHistoryWritePlans();
            return;
        }
        const selectionApproveButton = event.target.closest("button[data-history-plan-selection-approve]");
        if (selectionApproveButton) {
            await WorkbenchPanel.batchApproveSelectedRetestHistoryWritePlans();
            return;
        }
        const selectionMaintenanceButton = event.target.closest("button[data-history-plan-selection-maintenance]");
        if (selectionMaintenanceButton) {
            selectionMaintenanceButton.disabled = true;
            try {
                await WorkbenchPanel.openSelectedHistoryPlanMaintenance(selectionMaintenanceButton.dataset.historyPlanSelectionMaintenance || "base");
            } catch (error) {
                selectionMaintenanceButton.disabled = false;
                setStatus(error.message);
            }
            return;
        }
        const button = event.target.closest("button[data-history-plan-exception-filter]");
        if (button) {
            await WorkbenchPanel.showPendingHistoryWriteReviews();
            return;
        }
        const retestButton = event.target.closest("button[data-history-plan-retest-mismatch-filter]");
        if (retestButton) {
            await WorkbenchPanel.showRetestMismatchedHistoryWritePlans();
            return;
        }
        const priorityButton = event.target.closest("button[data-history-plan-priority-filter]");
        if (priorityButton) {
            await WorkbenchPanel.showPriorityHistoryWritePlans(priorityButton.dataset.historyPlanPriorityFilter);
        }
    });
    els.historyPlanStatusSelect?.addEventListener("change", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyPlanComparisonSelect?.addEventListener("change", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyPlanReviewSelect?.addEventListener("change", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyPlanRetestSelect?.addEventListener("change", () => {
        state.historyPlanRetestStatus = els.historyPlanRetestSelect.value || "";
        WorkbenchPanel.loadHistoryWritePlans();
    });
    els.historyPlanMaintenanceSelect?.addEventListener("change", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyPlanPrioritySelect?.addEventListener("change", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyPlanActionSelect?.addEventListener("change", () => WorkbenchPanel.loadHistoryWritePlans());
    els.historyReviewLedger?.addEventListener("click", async (event) => {
        const clearAllButton = event.target.closest("[data-history-ledger-clear-all]");
        if (clearAllButton) {
            await WorkbenchPanel.clearHistoryPlanFilters();
            return;
        }
        const clearLocateButton = event.target.closest("[data-history-ledger-clear-locate]");
        if (clearLocateButton) {
            await WorkbenchPanel.clearHistoryPlanLocate();
            return;
        }
        const clearQueueFilterButton = event.target.closest("[data-history-ledger-clear-queue-filter]");
        if (clearQueueFilterButton) {
            await WorkbenchPanel.clearHistoryPlanQueueFilter();
            return;
        }
        const clearFilterButton = event.target.closest("[data-history-ledger-clear-filter]");
        if (clearFilterButton) {
            await WorkbenchPanel.clearHistoryPlanFilter(clearFilterButton.dataset.historyLedgerClearFilter);
            return;
        }
        const clearButton = event.target.closest("[data-history-ledger-clear-field]");
        if (clearButton) {
            await WorkbenchPanel.clearHistoryLedgerFieldFilter();
            return;
        }
        const clearRetestButton = event.target.closest("[data-history-ledger-clear-retest]");
        if (clearRetestButton) {
            await WorkbenchPanel.clearHistoryLedgerRetestFilter();
            return;
        }
        const clearMaintenanceButton = event.target.closest("[data-history-ledger-clear-maintenance]");
        if (clearMaintenanceButton) {
            await WorkbenchPanel.clearHistoryLedgerMaintenanceFilter();
            return;
        }
        const batchRetestApproveButton = event.target.closest("[data-history-ledger-batch-retest-approve]");
        if (batchRetestApproveButton) {
            batchRetestApproveButton.disabled = true;
            try {
                await WorkbenchPanel.batchApproveRetestFromLedger();
            } catch (error) {
                setStatus(error.message);
            } finally {
                WorkbenchPanel.updateHistoryPlanActionState();
            }
            return;
        }
        const filterButton = event.target.closest("[data-history-ledger-filter]");
        if (filterButton) {
            await WorkbenchPanel.applyHistoryLedgerFilter(filterButton.dataset.historyLedgerFilter, filterButton.dataset.historyLedgerValue);
        }
    });
    els.historyPlanBatchPreviewButton?.addEventListener("click", async () => {
        els.historyPlanBatchPreviewButton.disabled = true;
        try {
            await WorkbenchPanel.batchPreviewHistoryWritePlans();
        } catch (error) {
            setStatus(error.message);
        } finally {
            WorkbenchPanel.updateHistoryPlanActionState();
        }
    });
    els.historyPlanBatchRetestButton?.addEventListener("click", async () => {
        els.historyPlanBatchRetestButton.disabled = true;
        try {
            await WorkbenchPanel.batchRetestHistoryWritePlans();
        } catch (error) {
            setStatus(error.message);
        } finally {
            WorkbenchPanel.updateHistoryPlanActionState();
        }
    });
    els.historyPlanBatchRetestApproveButton?.addEventListener("click", async () => {
        els.historyPlanBatchRetestApproveButton.disabled = true;
        try {
            await WorkbenchPanel.batchApproveRetestHistoryWritePlans();
        } catch (error) {
            setStatus(error.message);
        } finally {
            WorkbenchPanel.updateHistoryPlanActionState();
        }
    });
    els.historyPlanBatchExecuteButton?.addEventListener("click", async () => {
        els.historyPlanBatchExecuteButton.disabled = true;
        try {
            await WorkbenchPanel.batchExecuteHistoryWritePlans();
        } catch (error) {
            setStatus(error.message);
        } finally {
            WorkbenchPanel.updateHistoryPlanActionState();
        }
    });
    els.historyPlanBatchRollbackButton?.addEventListener("click", async () => {
        els.historyPlanBatchRollbackButton.disabled = true;
        try {
            await WorkbenchPanel.batchRollbackHistoryWritePlans();
        } catch (error) {
            setStatus(error.message);
        } finally {
            WorkbenchPanel.updateHistoryPlanActionState();
        }
    });
    els.historyPlanExportButton?.addEventListener("click", () => WorkbenchPanel.exportHistoryWritePlans());
    els.systemRefreshButton.addEventListener("click", () => SystemPanel.load(state.activeMenuCode));
    els.systemContent.addEventListener("change", async (event) => {
        const select = event.target.closest("select[data-menu-status-code]");
        if (select) {
            await SystemPanel.updateMenuStatus(select);
        }
        const roleStatus = event.target.closest("select[data-role-status-code]");
        if (roleStatus) {
            await SystemPanel.updateRoleStatus(roleStatus);
        }
        const userStatus = event.target.closest("select[data-user-status-username]");
        if (userStatus) {
            await SystemPanel.updateUserStatus(userStatus);
        }
        const orgCheck = event.target.closest("input[data-org-check]");
        if (orgCheck) {
            SystemPanel.handleOrgCheckChange(orgCheck);
        }
    });
    els.systemContent.addEventListener("submit", async (event) => {
        const auditForm = event.target.closest("form[data-audit-filter-form]");
        const roleForm = event.target.closest("form[data-create-role-form]");
        const userForm = event.target.closest("form[data-create-user-form]");
        if (auditForm) {
            event.preventDefault();
            await SystemPanel.applyAuditFilters(auditForm);
        }
        if (roleForm) {
            event.preventDefault();
            await SystemPanel.createRole(roleForm);
        }
        if (userForm) {
            event.preventDefault();
            await SystemPanel.createUser(userForm);
        }
    });
    els.systemContent.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-save-role]");
        if (button) {
            await SystemPanel.saveRoleMenus(button.dataset.saveRole);
        }
        const templateButton = event.target.closest("button[data-apply-role-template]");
        if (templateButton) {
            await SystemPanel.applyRoleTemplate(templateButton.dataset.applyRoleTemplate);
        }
        const userButton = event.target.closest("button[data-save-user]");
        if (userButton) {
            await SystemPanel.saveUserRoles(userButton.dataset.saveUser);
        }
        const resetButton = event.target.closest("button[data-reset-password]");
        if (resetButton) {
            await SystemPanel.resetPassword(resetButton.dataset.resetPassword);
        }
        const auditResetButton = event.target.closest("button[data-reset-audit-filter]");
        if (auditResetButton) {
            await SystemPanel.resetAuditFilters();
        }
        const auditExportButton = event.target.closest("button[data-export-audit]");
        if (auditExportButton) {
            SystemPanel.exportAudits();
        }
    });
    els.workbenchFilterForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        await WorkbenchPanel.load();
    });
    els.workbenchMetrics.addEventListener("click", async (event) => {
        const exportButton = event.target.closest("[data-metric-export]");
        if (exportButton) {
            event.stopPropagation();
            WorkbenchPanel.exportMetric(exportButton.dataset.metricExport);
            return;
        }
        const viewButton = event.target.closest("[data-metric-view]");
        if (viewButton) {
            event.stopPropagation();
            await WorkbenchPanel.openMetric(viewButton.dataset.metricView);
            return;
        }
        const metric = event.target.closest("[data-metric-code]");
        if (metric) {
            await WorkbenchPanel.openMetric(metric.dataset.metricCode);
        }
    });
    els.exportTodoButton.addEventListener("click", () => WorkbenchPanel.exportItems("TODO"));
    els.exportDoneButton.addEventListener("click", () => WorkbenchPanel.exportItems("DONE"));
    els.loadMoreTodoButton.addEventListener("click", () => WorkbenchPanel.loadMore("TODO"));
    els.loadMoreDoneButton.addEventListener("click", () => WorkbenchPanel.loadMore("DONE"));
    els.batchReconcileButton.addEventListener("click", () => PersonDetail.reconcileBatch());
    els.normalGradeBatchButton.addEventListener("click", () => PersonDetail.normalGradeBatch());
    els.exportBatchReconcileButton.addEventListener("click", () => PersonDetail.exportBatchReconcile());
    els.exportNormalGradeButton.addEventListener("click", () => PersonDetail.exportNormalGradeBatch());
    els.loadAcceptanceSampleButton.addEventListener("click", () => AcceptanceSamples.loadSelected());
    els.trialCalcButton.addEventListener("click", () => PersonDetail.trialCalculate());
    els.reconcileButton.addEventListener("click", () => PersonDetail.reconcileSelectedHistory());
    els.timelineButton.addEventListener("click", () => PersonDetail.replayTimeline());
    els.generatedTimelineButton.addEventListener("click", () => PersonDetail.generatedTimeline());
    els.civilConfigButton.addEventListener("click", () => ConfigPanel.loadEffective("01", "01"));
    els.institutionConfigButton.addEventListener("click", () => ConfigPanel.loadEffective("10", "07"));
    els.allConfigButton.addEventListener("click", () => ConfigPanel.loadAll());
    els.configYearInput.addEventListener("change", () => {
        if (state.configMode === "admin") {
            return ConfigPanel.loadAll();
        }
        return ConfigPanel.loadEffective();
    });
    els.fieldConfigEditor.addEventListener("submit", (event) => ConfigPanel.save(event));
    els.cancelConfigEditButton.addEventListener("click", () => ConfigPanel.hideEditor());
}

AcceptanceSamples.init();
bindEvents();
AuthPanel.boot();
