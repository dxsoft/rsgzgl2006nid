# 后端迁移提交清单

生成时间：2026-06-26

## 目的

这份清单用于把当前 Spring Boot + MySQL 迁移成果纳入 Git 管理前做最后审阅。当前 Git 只跟踪了少量工作台文件，完整迁移工程中的源码、数据库脚本、静态页面、回归脚本和文档大多仍是未跟踪状态。提交前必须先确认纳管范围，否则干净检出后无法复现当前系统。

## 建议首批纳管范围

- 后端工程骨架：`backend/pom.xml`、`backend/README.md`、`backend/src/main/java/com/dx/rsgzgl/RsgzglBackendApplication.java`
- 公共能力：`backend/src/main/java/com/dx/rsgzgl/common/`、`backend/src/main/java/com/dx/rsgzgl/config/`
- 组织与人员基础信息：`backend/src/main/java/com/dx/rsgzgl/org/`、`backend/src/main/java/com/dx/rsgzgl/person/`
- 工资核心业务：`backend/src/main/java/com/dx/rsgzgl/salary/`
- 系统、权限、工作台、报表打印、历史写入闭环：`backend/src/main/java/com/dx/rsgzgl/system/`
- 配置与数据库迁移：`backend/src/main/resources/application.yml`、`backend/src/main/resources/db/`
- 桌面软件风格 UI：`backend/src/main/resources/static/index.html`、`backend/src/main/resources/static/app.js`、`backend/src/main/resources/static/styles.css`
- 后端回归测试：`backend/src/test/java/com/dx/rsgzgl/`
- 后端业务校验脚本和样本：`backend/scripts/`
- 迁移版本管理核查：`scripts/check-backend-version-control.ps1`、`scripts/build-backend-submit-review.ps1`、`scripts/prepare-backend-first-batch-stage.ps1`
- 迁移版本管理说明：`docs/backend-migration-version-control-checklist.md`、`docs/backend-migration-submit-manifest.md`

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
- `scripts`：39 个根目录运维/核查脚本
- `docs`：16 个迁移文档或政策依据文件

首批提交不必一次纳入全部根目录 `scripts` 和 `docs`，但至少要纳入版本管理核查脚本和本清单引用的迁移说明。其余生产预检、上线回滚、数据治理脚本建议作为第二批“上线支撑资产”统一审阅。

## 提交前复核命令

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\build-backend-submit-review.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\prepare-backend-first-batch-stage.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-backend-version-control.ps1 -FailOnUntracked
git status --short -- backend docs scripts .gitignore
git diff --check -- .gitignore docs scripts backend
```

严格模式 `-FailOnUntracked` 在首批必需路径全部纳管前会失败，这是预期行为。失败前会生成报告：

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

确认清单无误后，才使用 `-Apply` 执行首批 `git add`。

## 建议提交节奏

1. 首批提交迁移工程闭环：`backend/src`、`backend/pom.xml`、`backend/README.md`、数据库迁移、静态 UI、后端测试、`backend/scripts`、版本管理核查文档和脚本。
2. 第二批提交上线支撑资产：根目录生产预检脚本、上线回滚方案、冻结归档清单、数据治理和权限快照脚本。
3. 第三批提交政策依据和验收资料：`docs` 下的政策文件、阶段性交付说明、验收清单、抽查样本。

如果需要合并成一次提交，必须先确认 `git status --short` 中没有构建产物、日志、PID、临时 Office 文件。
