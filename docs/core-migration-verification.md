# Core Migration Verification

This document is the executable gate for the active salary migration line.
Retirement salary and year-end bonus are intentionally out of scope.

## Main Gate

For launch preparation, run the higher-level readiness gate first:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-launch-readiness.ps1
```

It writes `backend\target\launch-readiness-report.txt` for archive.
Use `-MavenTimeoutSec <seconds>` when Maven dependency resolution or Surefire
startup is slow and you want the readiness report to fail with a bounded timeout.
The launch readiness gate runs Maven regression groups separately before the
service/sample gates. In constrained shells, make sure the caller timeout is
longer than the total grouped Maven budget, or run the readiness gate in stages
with `-SkipCoreMigration` / `-SkipPackage`. `-SkipMavenRegression` is only for
local staged checks after the Maven regression gate has already been run
separately; do not use it as a production launch pass.

## Database Migration

Flyway migrations are kept under `backend/src/main/resources/db/migration`.
Runtime auto-migration is disabled by default and is enabled only for an
explicit migration run:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\apply-db-migrations.ps1 -DbPassword <password>
```

The command sets `DB_MIGRATION_ENABLED=true`, runs Spring Boot with
`spring.main.web-application-type=none`, applies pending migrations, and exits.
Current formal schema version:

```text
V4__salary_report_print_batch.sql
```

Normal service startup still uses `DB_MIGRATION_ENABLED=false` unless an
operator deliberately enables migrations.

For the controlled history-write rehearsal and real pilot precheck:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-history-write-rehearsal.ps1
powershell -ExecutionPolicy Bypass -File scripts\build-real-history-write-precheck.ps1 -ScanLimit 300 -Take 5
powershell -ExecutionPolicy Bypass -File scripts\scan-real-writable-candidates.ps1 -Start 2025-01 -End 2026-06 -OrgLimit 80 -PerPreviewLimit 100 -Take 20
powershell -ExecutionPolicy Bypass -File scripts\run-real-writable-candidate-precheck.ps1 -Take 3 -ReviewBeforeConfirm
powershell -ExecutionPolicy Bypass -File scripts\run-real-writable-candidate-pilot.ps1 -Take 1
powershell -ExecutionPolicy Bypass -File scripts\run-real-history-write-pilot.ps1
```

Run from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-core-migration.ps1 -FailOnUnexpected
```

It writes the consolidated step report to:

```text
backend\target\core-migration-verification-results.tsv
```

The script runs and summarizes:

- `mvn test -Dtest=SystemPermissionRegressionTests`
- `mvn test -Dtest=NormalGradeTrialRegressionTests,SalaryTimelineRegressionTests`
- `scripts\verify-salary-samples.ps1 -FailOnUnexpected`
- `scripts\verify-business-acceptance-samples.ps1 -FailOnUnexpected`
- `scripts\verify-generated-timeline-samples.ps1 -FailOnUnexpected`
- `scripts\verify-generated-timeline-level-contract.ps1 -FailOnUnexpected`
- `scripts\verify-report-print-archive-samples.ps1 -FailOnUnexpected`
- `scripts\verify-report-print-archive-ledger.ps1 -FailOnUnexpected`
- `scripts\verify-report-print-pages.ps1 -FailOnUnexpected`
- `scripts\verify-report-csv-exports.ps1 -FailOnUnexpected`
- `scripts\verify-case-report-ui-contract.ps1 -FailOnUnexpected`
- `scripts\verify-report-history-queue-closure.ps1 -FailOnUnexpected`

The core script records `PASS`, `FAIL`, and `SKIP` for every step, then fails
at the end if any step failed. Use `-OutputPath <path>` to archive a custom TSV
and `-MaxSummaryMilliseconds <ms>` to tune the workbench summary response budget.
Use `-MavenTimeoutSec <seconds>` to bound Maven regression steps.

The sample gates require the backend service to be running, normally at:

```text
http://127.0.0.1:18080
```

Before a real history-write batch, also follow:

```text
docs/上线前历史写入操作与数据保护清单.md
```

By default the scripts log in with `admin/admin`. Override it when needed:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-core-migration.ps1 -Username admin -Password admin -FailOnUnexpected
```

## Useful Short Runs

Only run the Maven regression gates:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-core-migration.ps1 -SkipSamples
```

This short run is the required Maven-only gate when launch readiness is run
with `-SkipMavenRegression`. The history-write safety regression can take more
than five minutes on the shared MySQL dataset; keep the caller timeout above
the script's Maven timeout budget.

Run launch readiness after Maven has already been verified separately:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-launch-readiness.ps1 -SkipMavenRegression -SkipPackage
```

Only run the service and sample gates:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-core-migration.ps1 -SkipMaven -FailOnUnexpected
```

Run a specific sample gate:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-auto-regression-samples.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-salary-samples.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-business-acceptance-samples.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-generated-timeline-samples.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-generated-timeline-level-contract.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-report-print-archive-samples.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-report-print-archive-ledger.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-report-print-pages.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-report-csv-exports.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-case-report-ui-contract.ps1 -FailOnUnexpected
powershell -ExecutionPolicy Bypass -File scripts\verify-report-history-queue-closure.ps1 -FailOnUnexpected
```

Run the person maintenance UI and code-option regression suite:

```powershell
powershell -ExecutionPolicy Bypass -File backend\scripts\verify-person-maintenance-suite.ps1
```

Include the stateful base-change to salary-todo-cache refresh closure check:

```powershell
powershell -ExecutionPolicy Bypass -File backend\scripts\verify-person-maintenance-suite.ps1 -IncludeCacheClosure
```

This option registers a `VERIFY-*` base-change row, checks that the salary todo
cache becomes `DIRTY`, refreshes the cache, and then verifies both the
`salary-todo` metric and TODO workbench page can be read after refresh. The
refresh count, metric count, and TODO page total must match.

`verify-auto-regression-samples.ps1` is the sample package gate. It can
optionally rebuild the dynamic sample sets first:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-auto-regression-samples.ps1 -RebuildSamples -FailOnUnexpected
```

It writes an archive under:

```text
backend\target\auto-regression
```

The report print archive sample gate checks the approval-report side of the
salary business loop: printed cases expose archive metadata, unprinted cases are
blocked at history-write confirmation, and batch preview `executable` excludes
unprinted approval-report warnings.

The report print page gate also checks the report catalog itself: every catalog
item with a migrated `printUrl` is opened after substituting runtime parameters
such as `orgCode`, `year`, `month`, and `caseNo`. Legacy FRX entries discovered
from `REPORTS` without a migrated URL remain visible as the backlog and are not
counted as failures.

The report CSV export gate verifies the same catalog contract through JSON:
the catalog must expose migrated rows, pending legacy rows, and every migrated
row must have a non-empty `printUrl`. The PowerShell check reads the JSON
response as UTF-8 bytes before parsing, so Chinese status labels are compared
without shell code-page ambiguity.

## Root Script Wrappers

The root `scripts` directory is a wrapper layer over `backend/scripts`.
It changes into `backend` before invoking the underlying script so existing
relative paths such as `target/*.tsv` keep working.

Keep new sample scripts in `backend/scripts`, then add a small wrapper in
`scripts` when the command should be available from the repository root.

## Pass Criteria

The core migration gate is considered passed when:

- the workbench/system regression tests pass;
- salary rule and timeline regression tests pass;
- sample gates report no unexpected non-matching rows;
- report print archive samples verify printed-case metadata, unprinted-case write blocking,
  and batch-preview executable counts;
- report/history queue closure verifies history-write queue actions and the workbench
  `HISTORY_QUEUE_*` summary metrics return within the configured response budget;
- any remaining non-matching row is either in `backend/scripts/known-sample-issues.tsv`
  or has a clear data-maintenance task;
- generated timeline issues can be deposited to the workbench and reviewed/retested.

For `verify-generated-timeline-samples.ps1`, `DIFF` and `ERROR` rows are
business diagnostics produced by the generated timeline checker. They are
written to `backend/target/generated-timeline-results.tsv` for follow-up, but
they do not fail the gate. The gate fails only on `REQUEST_ERROR`, which means
the checker could not call the backend API successfully.
