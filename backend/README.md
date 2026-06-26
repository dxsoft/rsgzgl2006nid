# rsgzgl-backend

Spring Boot + MySQL backend scaffold for the Visual FoxPro salary management system rewrite.

## Stack

- Java 21
- Spring Boot 3
- MySQL 8
- MyBatis-Plus
- Flyway
- EasyExcel

## Run

Set the local MySQL credentials with environment variables, then run:

```powershell
$env:DB_PASSWORD='your-local-password'
mvn spring-boot:run
```

Default URL: `http://localhost:18080`.

From the repository root, use the managed dev helper when running online verification scripts:

```powershell
$env:DB_PASSWORD='your-local-password'
powershell -ExecutionPolicy Bypass -File scripts\start-backend-dev.ps1
powershell -ExecutionPolicy Bypass -File scripts\verify-report-csv-exports.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-report-print-pages.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\stop-backend-dev.ps1
```

The helper writes PID and log files under `backend\target`, waits for `GET /api/health`, and stops only the process recorded in its managed PID file.

To run the packaged jar in the background on Windows:

```powershell
Start-Process -FilePath 'C:\Program Files\Java\jdk-21.0.10\bin\java.exe' -ArgumentList @('-jar','D:\rsgzgl2006nid\backend\target\rsgzgl-backend-0.1.0-SNAPSHOT.jar') -WorkingDirectory 'D:\rsgzgl2006nid\backend' -WindowStyle Hidden
```

To stop it:

```powershell
$pid = (netstat -ano | Select-String ':18080').ToString().Trim().Split()[-1]
Stop-Process -Id $pid
```

Flyway is disabled by default so the new backend does not modify the existing legacy `gzjsgl` schema accidentally. Enable it after the modern schema plan is confirmed.

## Web UI

The Spring Boot app serves a lightweight management workspace at `http://localhost:18080/`.

Current screens:

- Organization tree from `GET /api/org/tree`
- Personnel list, organization filtering, and search from `GET /api/persons`
- Personnel profile from `GET /api/persons/{personCode}`
- Salary history from `GET /api/salary/history/{personCode}`
- Salary item details from `GET /api/salary/history-records/{historyId}`
- Baseline trial calculation from `POST /api/salary/trial-calc`
- Single-person salary reconciliation from `POST /api/salary/reconcile`
- Organization batch salary reconciliation from `POST /api/salary/reconcile-batch`
- CSV export for batch reconciliation from `GET /api/salary/reconcile-batch.csv`
- Available salary periods from `GET /api/salary/periods`
- Normal grade-increment rule trial from `POST /api/salary/rule-trial/normal-grade`
- Batch normal grade-increment rule trial from `POST /api/salary/rule-trial/normal-grade-batch`
- CSV export for batch normal grade trial from `GET /api/salary/rule-trial/normal-grade-batch.csv`
- Person salary timeline replay from `GET /api/salary/timeline/{personCode}`
- Base-info generated salary timeline comparison from `GET /api/salary/timeline-generated/{personCode}`
- Business acceptance sample picker for loading representative people, dates, change types, and running a single rule trial

## API Surface

- `GET /api/health`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/org/tree`
- `GET /api/persons`
- `GET /api/persons/{personCode}`
- `GET /api/salary/history/{personCode}`
- `GET /api/salary/history-records/{historyId}`
- `GET /api/salary/periods`
- `POST /api/salary/trial-calc`
- `POST /api/salary/reconcile`
- `POST /api/salary/reconcile-batch`
- `GET /api/salary/reconcile-batch.csv`
- `POST /api/salary/rule-trial/normal-grade`
- `POST /api/salary/rule-trial/normal-grade-batch`
- `GET /api/salary/rule-trial/normal-grade-batch.csv`
- `GET /api/salary/timeline/{personCode}`
- `GET /api/salary/timeline-generated/{personCode}`

## Salary Rule Regression

Run the launch readiness gate before production history-write batches:

```powershell
powershell -ExecutionPolicy Bypass -File ..\scripts\verify-launch-readiness.ps1
```

The report is written to `target\launch-readiness-report.txt`.

Additional launch artifacts can be produced from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-history-write-rehearsal.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-real-history-write-precheck.ps1 -ScanLimit 300 -Take 5
powershell -ExecutionPolicy Bypass -File scripts\scan-real-writable-candidates.ps1 -Start 2025-01 -End 2026-06 -OrgLimit 80 -PerPreviewLimit 100 -Take 20
powershell -ExecutionPolicy Bypass -File scripts\run-real-writable-candidate-precheck.ps1 -Take 3 -ReviewBeforeConfirm
powershell -ExecutionPolicy Bypass -File scripts\run-real-writable-candidate-pilot.ps1 -Take 1
powershell -ExecutionPolicy Bypass -File scripts\run-real-history-write-pilot.ps1
powershell -ExecutionPolicy Bypass -File scripts\export-production-permission-snapshot.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-production-data-governance-ledger.ps1
```

Run the main regression gate after rule changes:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-salary-samples.ps1 -FailOnUnexpected
```

For production history-write preparation, use the checklist in:

```text
..\docs\上线前历史写入操作与数据保护清单.md
```

The main gate also verifies the business acceptance samples. To run only those fixed walkthrough samples:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-business-acceptance-samples.ps1 -FailOnUnexpected
```

The gate verifies these sample sets when their TSV files exist:

| Sample set | Build script | Result file | Coverage |
| --- | --- | --- | --- |
| `cross-type` | maintained TSV | `target/cross-type-results.tsv` | mixed high-value cross-type samples |
| `normal-grade-expanded` | maintained TSV | `target/normal-grade-expanded-results.tsv` | normal grade/level/salary-grade samples |
| `target-state` | `scripts\build-target-state-samples.ps1` | `target/target-state-results.tsv` | demotion, reward promotion, other target-state changes |
| `rank-judicial` | `scripts\build-rank-judicial-samples.ps1` | `target/rank-judicial-results.tsv` | police rank, civil rank, judicial rank/allowance |
| `core-flow` | `scripts\build-core-flow-samples.ps1` | `target/core-flow-results.tsv` | standard/allowance changes, post changes, entrance salary |
| `special-flow` | `scripts\build-special-flow-samples.ps1` | `target/special-flow-results.tsv` | education change, teacher/nurse allowance, 2006 conversion, normal increment |
| `business-acceptance` | maintained TSV | `target/business-acceptance-results.tsv` | fixed representative samples for UI/business walkthroughs |
| `report-print-archive` | runtime API samples | `target/report-print-archive-results.tsv` | approval-report archive metadata, unprinted write blocking, batch executable counts |

To rebuild generated sample files against the current legacy database:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-target-state-samples.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-rank-judicial-samples.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-core-flow-samples.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-special-flow-samples.ps1
```

Known allowed sample issues live in `scripts\known-sample-issues.tsv`. Keep data-cleanup cases there instead of hard-coding person codes in `verify-salary-samples.ps1`.

`GET /api/salary/timeline/{personCode}` replays one person's legacy salary change rows in historical chain order and compares each recalculated result with the corresponding `hisbase` row. The chain uses `hisbase.id -> hisbase.sid`, where `sid` points to the next change row. If a broken chain is encountered, the service falls back to year/month and amount ordering only for the disconnected rows. This first version is an event replay against existing history rows; a fully independent state machine derived only from base personnel data is the next layer.

`GET /api/salary/timeline-generated/{personCode}` generates expected salary events from base tables first, then compares them with the `hisbase` chain. The first version generates events that can be identified directly from base information: `dryjbxx` 2006 conversion, `dryzwbh` post changes after an existing prior post row, civil rank conversion/promotion (`职级套改`/`职级晋升`), `dxl` education changes from the month after `bysj` only when the person already has non-probation salary before that month, and `dndkh` assessment-driven normal adjustments, including same-month history-derived `正常级别` and `级别滚动` rows before `正常档次`. For probationary/trainee rows, `2006套改` recalculates probationary salary from the 2006 standard instead of using the conversion tables. As a transitional compatibility rule, post events from `dryzwbh` are aligned to a same-type `hisbase` row in the previous/next month when the exact generated month has no same-type history row. Standard/allowance changes and manual special items are counted as history-only unsupported rows rather than unexpected errors. `scripts\verify-generated-timeline-samples.ps1` audits generated timelines for sample people and writes `target/generated-timeline-results.tsv`.

Additional audit scripts:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-jslb-coverage.ps1
powershell -ExecutionPolicy Bypass -File scripts\classify-tg2006-tgb.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-probationary-data-issues.ps1
```

`build-jslb-coverage.ps1` writes `target/jslb-coverage.tsv` and checks whether any in-service `jslb` is still unmapped. `classify-tg2006-tgb.ps1` writes 2006 civil TGB classification files for non-amount validation review. `build-probationary-data-issues.ps1` writes `target/probationary-data-issues.tsv` for zero-total probationary placeholder records.

`verify-business-acceptance-samples.ps1` verifies the fixed representative samples in `scripts\business-acceptance-samples.tsv`; it requires both `matchedExpected=true` and the calculated total to equal the documented target amount.

`verify-report-print-archive-samples.ps1` verifies the report-printing side of the formal salary workflow: printed cases expose archive metadata, unprinted cases are blocked at history-write confirmation, and batch preview executable counts exclude unprinted approval-report warnings.

`GET /api/org/tree` now reads the legacy `dwbm` table and builds a hierarchy from organization-code prefixes. `GET /api/persons` reads from `dryjbxx` and joins `dwbm` for organization names; `GET /api/persons/{personCode}` returns the broader legacy personnel profile. `GET /api/salary/history/{personCode}` reads `hisbase` and returns salary totals by calculation year/month. Salary details are generated from `fldgz` configuration: applicable fields, titles, ordering, and civil-service versus institution category differences come from configuration, with values read from `hisbase`. `POST /api/salary/trial-calc` currently returns a historical baseline calculation by using the latest `hisbase` row at or before the requested year/month. `POST /api/salary/reconcile` compares an exact legacy `hisbase` month with the current calculation result and returns total/detail differences. `POST /api/salary/rule-trial/normal-grade` is the first rule-based trial PoC: it reads the previous `hisbase` baseline and branches by `zwbm2` prefix, not by `dwsx`. Prefixes `01/02/03/04/23/24/25/26/27/28` follow grade increment against `bz06_jbgz`; prefixes `21/22` follow rank-grade increment against `bz06_djgz`; prefixes `05/06` follow worker post-grade increment against `bz06_zwgz_gr` and update `ZWGZSE2`; prefixes `07/08/09/10/11` follow institution salary-grade increment against `bz06_xjgz`. Assessment records come from `dndkh`; only `优秀/称职/合格` count. Prefixes `01/02/04/21/22/23/24/25/26/27/28` first check 5 qualified years from `xckhndjb` for one-level promotion. When the person has reached the highest level allowed by the current post, the same 5-year rule is converted into a grade increment within the current level. If level promotion and grade increment happen in the same month, the rule applies level promotion first and then grade increment. If level promotion does not apply, grade prefixes and worker prefixes require 2 qualified years from `xckhndzw`, while salary-grade prefixes require 1 qualified year from `xckhndzw`. Worker technical-grade promotion is detected from exact target-month `职务变化` records for `05/06`: it updates `ZWGZSE2`, `JSDJGZ2`, and derived `DFBT2`/`SDBT`, with post-grade placement against `bz06_zwgz_gr`. Standard lookup tries `zwgzdc2 + 1` first when the next standard exists and is not zero; otherwise it keeps `zwgzdc2`, increments `djc2`, and calculates the amount by grade difference. The result is compared with the exact target-month legacy record when present. `POST /api/salary/rule-trial/normal-grade-batch` applies the same rule to people under an organization and summarizes matched, different, no-target-record, skipped, reverse-step, level-promotion, and not-eligible counts. Each batch item includes `ruleType` and `ruleNote` for review. `GET /api/salary/rule-trial/normal-grade-batch.csv` exports the same batch result with a UTF-8 BOM for spreadsheet review. Auth controllers still return stable placeholder data while their legacy table mappings are confirmed.
