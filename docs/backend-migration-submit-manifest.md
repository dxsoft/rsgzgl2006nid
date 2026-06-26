# 后端迁移提交清单

生成时间：2026-06-26

## 目的

这份清单用于对当前 Spring Boot + MySQL 迁移成果做提交审阅。集成分支已经纳管后端源码、数据库脚本、静态页面、回归脚本、上线预检脚本和迁移文档；后续审阅重点是确认范围完整、没有构建产物或本地敏感信息混入，并且干净检出后可以按同一套命令复现验证。

## 首批纳管范围

- 后端工程骨架：`backend/pom.xml`、`backend/README.md`、`backend/src/main/java/com/dx/rsgzgl/RsgzglBackendApplication.java`
- 公共能力：`backend/src/main/java/com/dx/rsgzgl/common/`、`backend/src/main/java/com/dx/rsgzgl/config/`
- 组织与人员基础信息：`backend/src/main/java/com/dx/rsgzgl/org/`、`backend/src/main/java/com/dx/rsgzgl/person/`
- 工资核心业务：`backend/src/main/java/com/dx/rsgzgl/salary/`
- 系统、权限、工作台、报表打印、历史写入闭环：`backend/src/main/java/com/dx/rsgzgl/system/`
- 配置与数据库迁移：`backend/src/main/resources/application.yml`、`backend/src/main/resources/db/`
- 桌面软件风格 UI：`backend/src/main/resources/static/index.html`、`backend/src/main/resources/static/app.js`、`backend/src/main/resources/static/styles.css`
- 后端回归测试：`backend/src/test/java/com/dx/rsgzgl/`
- 后端业务校验脚本和样本：`backend/scripts/`
- 迁移版本管理核查：`scripts/backend-first-batch-paths.txt`、`scripts/check-backend-version-control.ps1`、`scripts/build-backend-submit-review.ps1`、`scripts/prepare-backend-first-batch-stage.ps1`
- 上线预检与业务闭环：`scripts/start-backend-dev.ps1`、`scripts/stop-backend-dev.ps1`、`scripts/verify-launch-readiness.ps1`、`scripts/verify-online-business-closure.ps1`
- 根目录回归入口：工资样本、业务验收、时间线、报表打印、历史队列、归档台账等 `scripts/verify-*.ps1`
- 迁移版本管理说明：`docs/backend-migration-version-control-checklist.md`、`docs/backend-migration-submit-manifest.md`

首批纳管路径以 `scripts/backend-first-batch-paths.txt` 为单一清单来源。版本控制检查、提交审查报告和首批暂存预览都读取该文件。

## 暂不建议混入首批提交的范围

- 构建产物：`backend/target/`
- 解包运行产物：`backend/BOOT-INF/`
- 运行日志和进程文件：`backend/*.log`、`backend/*.pid`
- Spring Boot 临时输出：`backend/spring-run*.out`、`backend/spring-run*.err`
- Office 临时文件：`~$*`
- 本地真实数据库密码字面量，例如个人机器上的 `DB_PASSWORD` 实际值

这些已由 `.gitignore` 过滤，提交前仍建议用脚本复核。
版本控制核查脚本会额外扫描可提交文本文件，发现本地真实数据库密码字面量时阻断。

## 当前文件规模

- `backend/src/main/java`：172 个 Java 文件
- `backend/src/main/resources`：16 个资源/迁移文件
- `backend/src/test/java`：7 个测试文件
- `backend/scripts`：24 个后端业务校验脚本/样本
- `scripts`：45 个根目录运维/核查脚本
- `docs`：17 个迁移文档或政策依据文件

根目录上线预检、在线业务闭环和核心回归入口已纳入首批路径清单；生产试点、真实写入候选扫描、数据治理和权限快照脚本也已随迁移支撑资产纳管，但真实写入类脚本只能在完成清单复核、权限确认和备份策略后执行。

## 提交前复核命令

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\build-backend-submit-review.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\prepare-backend-first-batch-stage.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1 -FailOnUntracked
git status --short -- backend docs scripts .gitignore
git diff --check -- .gitignore docs scripts backend
```

严格模式 `-FailOnUntracked` 应在集成分支上通过；失败时会生成报告：

```text
backend/target/backend-version-control-check.md
```

提交批次审阅脚本会生成报告：

```text
backend/target/backend-submit-review.md
```

首批纳管预备脚本默认只生成 staging 清单，不会改暂存区：

```text
backend/target/backend-first-batch-stage-files.txt
```

当前集成分支已完成首批纳管，`prepare-backend-first-batch-stage.ps1` 主要用于后续分支复核或重新生成 manifest。需要重新暂存时，确认清单无误后再使用 `-Apply` 执行 `git add`。

## 建议提交节奏

1. 当前集成分支作为迁移工程闭环提交：`backend/src`、数据库迁移、静态 UI、后端测试、业务校验脚本、上线预检和版本管理工具链。
2. 合并前运行离线闸口：版本控制检查、提交审查报告、首批暂存预览、后端编译和前端脚本语法检查。
3. 设置本地 `DB_PASSWORD` 后运行在线闸口：`scripts/verify-launch-readiness.ps1 -StartBackend -StopBackendAfter`。
4. 真实历史写入、生产候选扫描和回滚演练只在数据备份、权限确认、业务复核通过后执行。

如果需要合并成一次提交，必须先确认 `git status --short` 中没有构建产物、日志、PID、临时 Office 文件。
