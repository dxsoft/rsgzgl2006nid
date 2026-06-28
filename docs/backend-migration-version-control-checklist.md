# 后端迁移版本管理清单

生成时间：2026-06-26

## 背景

当前 Spring Boot 迁移代码已经可以编译和通过关键回归，但 Git 只跟踪了少量后端文件。大量后端源码、DTO、数据库迁移脚本、前端静态页和测试仍处于未跟踪状态。如果直接提交当前少量 tracked diff，干净检出后会缺少依赖文件，导致项目无法完整构建。

## 必须纳入版本管理的范围

- `backend/pom.xml`
- `backend/README.md`
- `backend/src/main/java/com/dx/rsgzgl/RsgzglBackendApplication.java`
- `backend/src/main/java/com/dx/rsgzgl/common/`
- `backend/src/main/java/com/dx/rsgzgl/config/`
- `backend/src/main/java/com/dx/rsgzgl/org/`
- `backend/src/main/java/com/dx/rsgzgl/person/`
- `backend/src/main/java/com/dx/rsgzgl/salary/`
- `backend/src/main/java/com/dx/rsgzgl/system/`
- `backend/scripts/`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/`
- `backend/src/main/resources/static/index.html`
- `backend/src/main/resources/static/app.js`
- `backend/src/main/resources/static/styles.css`
- `backend/src/test/java/com/dx/rsgzgl/`
- `docs/backend-migration-version-control-checklist.md`
- `docs/backend-migration-submit-manifest.md`
- `scripts/build-backend-submit-review.ps1`
- `scripts/backend-first-batch-paths.txt`
- `scripts/check-backend-version-control.ps1`
- `scripts/prepare-backend-first-batch-stage.ps1`
- `scripts/start-backend-dev.ps1`
- `scripts/stop-backend-dev.ps1`
- `scripts/verify-auto-regression-samples.ps1`
- `scripts/verify-business-acceptance-samples.ps1`
- `scripts/verify-case-detail-ui-contract.ps1`
- `scripts/verify-case-report-ui-contract.ps1`
- `scripts/verify-core-migration.ps1`
- `scripts/verify-generated-timeline-level-contract.ps1`
- `scripts/verify-generated-timeline-samples.ps1`
- `scripts/verify-history-write-batch-safety-contract.ps1`
- `scripts/verify-history-write-rehearsal.ps1`
- `scripts/verify-launch-readiness.ps1`
- `scripts/verify-online-business-closure.ps1`
- `backend/scripts/verify-person-maintenance-suite.ps1`
- `backend/scripts/verify-person-maintenance-ui-contract.ps1`
- `backend/scripts/verify-person-code-options.ps1`
- `backend/scripts/verify-person-maintenance-cache-closure.ps1`
- `scripts/verify-report-csv-exports.ps1`
- `scripts/verify-report-entry-matrix.ps1`
- `scripts/verify-report-history-queue-closure.ps1`
- `scripts/verify-report-print-archive-ledger.ps1`
- `scripts/verify-report-print-archive-samples.ps1`
- `scripts/verify-report-print-pages.ps1`
- `scripts/verify-salary-samples.ps1`

`scripts/backend-first-batch-paths.txt` 是首批纳管路径的单一清单来源，版本控制检查、提交审查和首批暂存脚本都从该文件读取路径。

## 不应纳入版本管理的范围

- `backend/target/`
- `backend/BOOT-INF/`
- `backend/*.log`
- `backend/*.pid`
- `backend/spring-run*.out`
- `backend/spring-run*.err`
- Office 临时文件：`~$*`
- 本地真实数据库密码字面量，例如个人机器上的 `DB_PASSWORD` 实际值

这些已通过根目录 `.gitignore` 补充过滤。
版本控制核查脚本还会扫描可提交文本文件，发现本地真实数据库密码字面量时直接阻断。

## 当前重点变更

- 工作台已办/待办支持按来源筛选。
- 数据治理、报表样本治理、交付包治理已接入已办闭环。
- 已办详情、列表、CSV 导出已补齐治理追溯字段：工作项 ID、核查说明、核查人、核查时间、复测状态、复测摘要、复测时间。
- `WorkbenchItemResponse` 已扩展治理追溯字段，并保留旧构造器兼容现有调用。
- `SystemPermissionRegressionTests` 已覆盖来源筛选、治理已办详情、CSV 导出和静态 UI 接线。

## 提交前验证命令

```powershell
powershell -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1
cd backend
$env:DB_PASSWORD='<your-local-password>'; mvn -DskipTests compile
$env:DB_PASSWORD='<your-local-password>'; mvn -Dtest=SystemPermissionRegressionTests#historyDeliveryExportButtonsArePermissionGatedInStaticUi test
$env:DB_PASSWORD='<your-local-password>'; mvn -Dtest=SystemPermissionRegressionTests#migrationSupportEndpointsCoverFormsApplicationGovernanceReportsAndAcceptance test
cd ..
node --check backend\src\main\resources\static\app.js
git diff --check -- backend/src/main/java/com/dx/rsgzgl/system/controller/WorkbenchController.java backend/src/main/java/com/dx/rsgzgl/system/service/WorkbenchService.java backend/src/main/resources/static/app.js backend/src/main/resources/static/styles.css backend/src/test/java/com/dx/rsgzgl/system/SystemPermissionRegressionTests.java
```

## 提交建议

建议后续提交拆成两类：

1. 基础迁移代码首批纳管：把尚未跟踪的 `backend/src`、`backend/pom.xml`、数据库迁移脚本、静态资源、后端校验脚本和测试整体纳入。
2. 本轮治理闭环增强：工作台来源筛选、治理已办详情/列表/CSV 追溯字段、相关回归测试。

如果需要一次性提交，也必须确认没有把 `target`、日志、PID、运行包等产物加入暂存区。

可使用严格模式检查是否仍有必备迁移路径未被 Git 跟踪：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1 -FailOnUntracked
```
