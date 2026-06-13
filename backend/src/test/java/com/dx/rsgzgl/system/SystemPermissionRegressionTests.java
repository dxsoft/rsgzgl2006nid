package com.dx.rsgzgl.system;

import com.dx.rsgzgl.salary.dto.NormalGradeTrialCommand;
import com.dx.rsgzgl.salary.dto.NormalGradeTrialResult;
import com.dx.rsgzgl.salary.service.NormalGradeTrialService;
import com.dx.rsgzgl.system.service.AuthSessionService;
import com.dx.rsgzgl.system.service.WorkbenchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class SystemPermissionRegressionTests {

    private static final String ADMIN_USER = "admin";
    private static final String WORKBENCH_USER = "tmp_test_workbench_only";
    private static final String WORKBENCH_ROLE = "TMP_TEST_WORKBENCH_ONLY";
    private static final String TODO_USER = "tmp_test_workbench_todo";
    private static final String TODO_ROLE = "TMP_TEST_WORKBENCH_TODO";
    private static final String SCOPED_WORKBENCH_USER = "tmp_test_workbench_scoped";
    private static final String SCOPED_WORKBENCH_ROLE = "TMP_TEST_WORKBENCH_SCOPED";
    private static final String ORG_USER = "tmp_test_org_001";
    private static final String ORG_ROLE = "TMP_TEST_ORG_001";
    private static final String TRIAL_USER = "tmp_test_trial_001";
    private static final String TRIAL_ROLE = "TMP_TEST_TRIAL_001";
    private static final String RECONCILE_USER = "tmp_test_reconcile_001";
    private static final String RECONCILE_ROLE = "TMP_TEST_RECONCILE_001";
    private static final String CREATED_USER = "tmp_test_created_user";
    private static final String CASE_WORK_ITEM = "tmp-test-salary-case-001";
    private static final String HISTORY_WRITE_WORK_ITEM = "tmp-test-history-write-success";
    private static final String HISTORY_WRITE_CASE_NO = "GZ-TMP-HISTORY-WRITE";
    private static final String HISTORY_WRITE_SOURCE_ID = "TMP-HIS-SOURCE-00055";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NormalGradeTrialService normalGradeTrialService;

    @Autowired
    private WorkbenchService workbenchService;

    @BeforeEach
    void setUp() {
        cleanup();
        createUserRole(WORKBENCH_USER, WORKBENCH_ROLE, "WORKBENCH");
        createUserRole(TODO_USER, TODO_ROLE, "WORKBENCH", "SALARY_TODO");
        createUserRole(SCOPED_WORKBENCH_USER, SCOPED_WORKBENCH_ROLE, "WORKBENCH", "SALARY_TODO", "SALARY_DONE", "SALARY_EXPORT");
        createUserRole(ORG_USER, ORG_ROLE, "SALARY_PERSON");
        createUserRole(TRIAL_USER, TRIAL_ROLE, "SALARY_TRIAL");
        createUserRole(RECONCILE_USER, RECONCILE_ROLE, "SALARY_RECONCILE");
        jdbcTemplate.update("""
                INSERT INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, SCOPED_WORKBENCH_USER);
        jdbcTemplate.update("""
                INSERT INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, ORG_USER);
        jdbcTemplate.update("""
                INSERT INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, TRIAL_USER);
        jdbcTemplate.update("""
                INSERT INTO sys_user_org(username, org_code)
                VALUES (?, '001')
                """, RECONCILE_USER);
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void workbenchWithoutSalaryTodoDoesNotReturnSalaryItems() throws Exception {
        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("SALARY_TODO"))))
                .andExpect(content().string(not(containsString("SALARY_DONE"))));

        mockMvc.perform(get("/api/workbench/items?status=TODO&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isBadRequest());
    }

    @Test
    void workbenchCsvRequiresExportPermission() throws Exception {
        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_TODO")))
                .andExpect(content().string(containsString("\"count\":-1")));

        mockMvc.perform(get("/api/workbench/metrics/salary-todo")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"SALARY_TODO\"")))
                .andExpect(content().string(not(containsString("\"count\":-1"))));

        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"SALARY_TODO\"")))
                .andExpect(content().string(not(containsString("\"count\":-1"))))
                .andExpect(content().string(containsString("\"hint\":\"")))
                .andExpect(content().string(containsString("T")));

        mockMvc.perform(post("/api/workbench/salary-todo-cache/dirty")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"code\":\"SALARY_TODO\"")))
                .andExpect(content().string(not(containsString("\"count\":-1"))))
                .andExpect(content().string(containsString("\"hint\":\"")))
                .andExpect(content().string(containsString("T")));

        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", TODO_USER, "marked dirty");

        mockMvc.perform(get("/api/workbench/items.csv?status=TODO&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/history-write-plans.csv?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-preview?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary done permission is required")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary done permission is required")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-rollback?limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Salary done permission is required")));
    }

    @Test
    void salaryTodoCacheCanBeMarkedDirtyByBaseDataChange() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        workbenchService.markSalaryTodoCacheDirtyForDataChange("base data changed: dryzwbh");

        String status = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", status);
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", "system", "dryzwbh");
    }

    @Test
    void personBaseChangeRegistrationMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/base-changes")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataType":"dryzwbh","changeYear":2026,"changeMonth":1,"sourceTable":"dryzwbh","sourceId":"unit-test","summary":"unit-test base change"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"dataType\":\"dryzwbh\"")))
                .andExpect(content().string(containsString("\"summary\":\"unit-test base change\"")));

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test base change");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        mockMvc.perform(get("/api/persons/001-00055/base-changes?limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("unit-test base change")));

        mockMvc.perform(post("/api/persons/00806-00868/base-changes")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataType":"dryzwbh","summary":"unit-test denied base change"}
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void workbenchUserStatePersistsPerCurrentUser() throws Exception {
        String payload = """
                {"state":{"queueFilter":{"caseNos":["CASE-A","CASE-B"],"autoSelect":true},"selected":[{"caseNo":"CASE-A","personCode":"001-00001","actionCode":"MAINTAIN_AND_RETEST"}]}}
                """;

        mockMvc.perform(put("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"stateKey\":\"history-plan-queue\"")))
                .andExpect(content().string(containsString("\"CASE-A\"")));

        mockMvc.perform(get("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"CASE-B\"")))
                .andExpect(content().string(containsString("\"MAINTAIN_AND_RETEST\"")));

        mockMvc.perform(get("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"state\":{}")));

        mockMvc.perform(delete("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"state\":{}")));

        mockMvc.perform(get("/api/workbench/user-states/history-plan-queue")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"state\":{}")));
    }

    @Test
    void salaryTodoItemIncludesLatestBaseChangeSummary() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache WHERE work_item_id = 'tmp-test-todo-latest-change'");
        mockMvc.perform(post("/api/persons/001-00055/base-changes")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataType":"dryzwbh","changeYear":2026,"changeMonth":1,"sourceTable":"dryzwbh","sourceId":"unit-test","summary":"unit-test latest base summary"}
                                """))
                .andExpect(status().isOk());

        jdbcTemplate.update("""
                INSERT INTO salary_todo_candidate_cache(work_item_id, source, source_id, person_code, org_code,
                                                        person_no, person_name, event_year, event_month, change_type, note)
                VALUES ('tmp-test-todo-latest-change', 'dryzwbh', 'unit-test', '001-00055', '001',
                        '00055', 'Unit Test', 2026, 1, 'unit-test-change', 'unit-test todo note')
                ON DUPLICATE KEY UPDATE note = VALUES(note)
                """);

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test todo note&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":\"tmp-test-todo-latest-change\"")))
                .andExpect(content().string(containsString("unit-test todo note")))
                .andExpect(content().string(containsString("unit-test latest base summary")));

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test latest base summary&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":\"tmp-test-todo-latest-change\"")))
                .andExpect(content().string(containsString("unit-test latest base summary")));

        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-todo-latest-change","source":"SALARY_EVENT","businessType":"unit-test-change","personCode":"001-00055","personName":"Unit Test","orgCode":"001","year":2026,"month":1,"title":"unit-test-change","summary":"stale request summary"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"tmp-test-todo-latest-change\"")))
                .andExpect(content().string(containsString("unit-test todo note")))
                .andExpect(content().string(containsString("unit-test latest base summary")))
                .andExpect(content().string(not(containsString("stale request summary"))));

        jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache WHERE work_item_id = 'tmp-test-todo-latest-change'");
    }

    @Test
    void personPostMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/posts")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postCode":"0190","postName":"unit-test-post","postLevel":"unit-test-level","rankCode":"0190","currentPostCode":"0190","startDate":"2026.01","excludedYears":0,"currentPostFlag":"1","payrollFlag":"UTEST","summary":"unit-test post create"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"postCode\":\"0190\"")))
                .andExpect(content().string(containsString("\"payrollFlag\":\"UTEST\"")));

        Long postId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM dryzwbh
                WHERE dwbm = '001' AND grbm = '00055' AND jsbz = 'UTEST'
                ORDER BY id DESC
                LIMIT 1
                """, Long.class);
        org.junit.jupiter.api.Assertions.assertNotNull(postId);
        assertAudit("person", "person-post-create", "PERSON_POST", String.valueOf(postId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test post create");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(put("/api/persons/posts/" + postId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postCode":"0191","postName":"unit-test-post-edit","postLevel":"unit-test-level-edit","rankCode":"0191","currentPostCode":"0191","startDate":"2026-02","excludedYears":1,"currentPostFlag":"","payrollFlag":"UTEST2","summary":"unit-test post update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"postCode\":\"0191\"")))
                .andExpect(content().string(containsString("\"startDate\":\"2026.02\"")))
                .andExpect(content().string(containsString("\"payrollFlag\":\"UTEST2\"")));

        assertAudit("person", "person-post-update", "PERSON_POST", String.valueOf(postId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test post update");

        mockMvc.perform(get("/api/persons/001-00055/posts")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + postId)))
                .andExpect(content().string(containsString("\"postCode\":\"0191\"")));

        mockMvc.perform(post("/api/persons/00806-00868/posts")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"postCode":"0190","postName":"unit-test denied","startDate":"2026.01","summary":"unit-test denied post"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void personEducationMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/educations")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"educationCode":"23","educationName":"unit-test-edu","school":"unit-test-school","enrollDate":"2025.09","graduationDate":"2026.07","studyYears":1,"educationType":"普通全日制","note":"UTEST","summary":"unit-test education create"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"educationCode\":\"23\"")))
                .andExpect(content().string(containsString("\"note\":\"UTEST\"")));

        Long educationId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM dxl
                WHERE dwbm = '001' AND grbm = '00055' AND bz = 'UTEST'
                ORDER BY id DESC
                LIMIT 1
                """, Long.class);
        org.junit.jupiter.api.Assertions.assertNotNull(educationId);
        assertAudit("person", "person-education-create", "PERSON_EDUCATION", String.valueOf(educationId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test education create");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(put("/api/persons/educations/" + educationId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"educationCode":"31","educationName":"unit-test-edu-edit","school":"unit-test-school-edit","enrollDate":"2025-10","graduationDate":"2026-08","studyYears":2,"educationType":"成人教育","note":"UTEST2","summary":"unit-test education update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"educationCode\":\"31\"")))
                .andExpect(content().string(containsString("\"graduationDate\":\"2026.08\"")))
                .andExpect(content().string(containsString("\"note\":\"UTEST2\"")));

        assertAudit("person", "person-education-update", "PERSON_EDUCATION", String.valueOf(educationId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test education update");

        mockMvc.perform(get("/api/persons/001-00055/educations")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + educationId)))
                .andExpect(content().string(containsString("\"educationCode\":\"31\"")));

        mockMvc.perform(post("/api/persons/00806-00868/educations")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"educationCode":"23","educationName":"unit-test denied","graduationDate":"2026.07","summary":"unit-test denied education"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void personAssessmentMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/persons/001-00055/assessments")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"year":"2098","result":"UTEST","summary":"unit-test assessment create"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"year\":\"2098\"")))
                .andExpect(content().string(containsString("\"result\":\"UTEST\"")));

        Long assessmentId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM dndkh
                WHERE dwbm = '001' AND grbm = '00055' AND khnd = '2098'
                ORDER BY id DESC
                LIMIT 1
                """, Long.class);
        org.junit.jupiter.api.Assertions.assertNotNull(assessmentId);
        assertAudit("person", "person-assessment-create", "PERSON_ASSESSMENT", String.valueOf(assessmentId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test assessment create");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-00055");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(put("/api/persons/assessments/" + assessmentId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"year":"2098","result":"UTEST2","summary":"unit-test assessment update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"year\":\"2098\"")))
                .andExpect(content().string(containsString("\"result\":\"UTEST2\"")));

        assertAudit("person", "person-assessment-update", "PERSON_ASSESSMENT", String.valueOf(assessmentId), ORG_USER, "001-00055");
        assertAudit("person", "person-base-change", "PERSON", "001-00055", ORG_USER, "unit-test assessment update");

        mockMvc.perform(get("/api/persons/001-00055/assessments")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":" + assessmentId)))
                .andExpect(content().string(containsString("\"result\":\"UTEST2\"")));

        mockMvc.perform(post("/api/persons/00806-00868/assessments")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"year":"2098","result":"合格","summary":"unit-test denied assessment"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void personBaseInfoMaintenanceMarksSalaryTodoCacheDirty() throws Exception {
        createTemporaryPerson();
        mockMvc.perform(post("/api/workbench/salary-todo-cache/refresh")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TODO_USER))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/persons/001-UT001/base-info")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCategory":"unit-test","organizationType":"23","postCategory":"unit-post","workStartDate":"2020.01","joinOrgDate":"2021.02","teacherNurseStartDate":"2022.03","teacherNurseFixedYears":1,"educationCode":"23","education":"unit-edu","rankCode":"2306","currentPost":"unit-current-post","postLevel":"unit-level","postStartDate":"2023.04","summary":"unit-test base info update"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-UT001\"")))
                .andExpect(content().string(containsString("\"personCategory\":\"unit-test\"")))
                .andExpect(content().string(containsString("\"postStartDate\":\"2023.04\"")));

        assertAudit("person", "person-base-info-update", "PERSON", "001-UT001", ORG_USER, "unit-test base info update");
        assertAudit("person", "person-base-change", "PERSON", "001-UT001", ORG_USER, "unit-test base info update");
        assertAudit("workbench", "salary-todo-cache-dirty", "SALARY_TODO_CACHE", "ALL", ORG_USER, "001-UT001");

        String cacheStatus = jdbcTemplate.queryForObject("""
                SELECT cache_status
                FROM salary_todo_cache_meta
                WHERE cache_key = 'salary-todo'
                """, String.class);
        org.junit.jupiter.api.Assertions.assertEquals("DIRTY", cacheStatus);

        mockMvc.perform(get("/api/persons/001-UT001/base-info")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"teacherNurseStartDate\":\"2022.03\"")));

        mockMvc.perform(get("/api/persons/001-UT001/base-status")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"personCode\":\"001-UT001\"")))
                .andExpect(content().string(containsString("\"latestChangeType\":\"dryjbxx\"")))
                .andExpect(content().string(containsString("\"latestChangeSummary\":\"unit-test base info update\"")))
                .andExpect(content().string(containsString("\"todoCacheStatus\":\"DIRTY\"")));

        mockMvc.perform(put("/api/persons/00806-00868/base-info")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCategory":"denied","summary":"unit-test denied base info"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/persons/00806-00868/base-status")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void workbenchItemsRespectOrganizationScope() throws Exception {
        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=001-&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"orgCode\":\"001")))
                .andExpect(content().string(not(containsString("\"orgCode\":\"00806\""))));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=00806-&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));
    }

    @Test
    void salaryTodoCanBeCompletedIntoWorkbenchDoneCase() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-preview","source":"SALARY_EVENT","businessType":"姝ｅ父妗ｆ","personCode":"001-00055","personName":"娴嬭瘯浜哄憳","orgCode":"001","year":2026,"month":1,"title":"姝ｅ父妗ｆ鍔炵悊","summary":"unit-test salary case preview"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"workItemId\":\"tmp-test-salary-case-preview\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"trialStatus\":")))
                .andExpect(content().string(containsString("\"trialChanges\":")));

        org.junit.jupiter.api.Assertions.assertEquals(0, countBusinessCase("tmp-test-salary-case-preview"),
                "Preview must not create salary business case.");
        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-requires-force","source":"SALARY_EVENT","businessType":"normal-grade","personCode":"001-00055","personName":"Force Test","orgCode":"001","year":2026,"month":1,"title":"Force Test","summary":"unit-test salary case requires force"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));
        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-missing-force-reason","source":"SALARY_EVENT","businessType":"normal-grade","personCode":"001-00055","personName":"Force Test","orgCode":"001","year":2026,"month":1,"title":"Force Test","summary":"unit-test missing force reason","force":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-001","source":"SALARY_EVENT","businessType":"正常档次","personCode":"001-00055","personName":"测试人员","orgCode":"001","year":2026,"month":1,"title":"正常档次办理","summary":"unit-test salary case done","force":true,"forceReason":"unit-test force reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"SALARY_CASE\"")))
                .andExpect(content().string(containsString("\"status\":\"DONE\"")))
                .andExpect(content().string(containsString("unit-test salary case done")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"source\":\"SALARY_CASE\"")))
                .andExpect(content().string(containsString("\"trialStatus\":\"ERROR\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("unit-test salary case done")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=ERROR&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"trialStatus\":\"ERROR\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=MATCH&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_TRIAL_ERROR")))
                .andExpect(content().string(containsString("SALARY_TRIAL_DIFFERENT")))
                .andExpect(content().string(containsString("SALARY_REVIEW_PENDING")));

        mockMvc.perform(get("/api/workbench/summary")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("HISTORY_PLAN_PREPARED")))
                .andExpect(content().string(containsString("HISTORY_PLAN_EXECUTED")))
                .andExpect(content().string(containsString("HISTORY_PLAN_ROLLED_BACK")))
                .andExpect(content().string(containsString("HISTORY_PLAN_BLOCKED")))
                .andExpect(content().string(containsString("HISTORY_PLAN_REVIEW_PENDING")));

        mockMvc.perform(get("/api/workbench/history-write-review-ledger?comparisonStatus=MISMATCHED&reviewStatus=PENDING&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\"")))
                .andExpect(content().string(containsString("\"pending\"")))
                .andExpect(content().string(containsString("\"byReviewCategory\"")))
                .andExpect(content().string(containsString("\"topMismatchFields\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?comparisonStatus=MISMATCHED&mismatchField=hj2&limit=20")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/workbench/items?status=TODO&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        assertAudit("workbench", "salary-case-done", "SALARY_CASE", caseNo(CASE_WORK_ITEM), SCOPED_WORKBENCH_USER, "001-00055");
        assertTrialSnapshot(CASE_WORK_ITEM);
        assertBusinessCaseSnapshot(CASE_WORK_ITEM);

        String caseNo = caseNo(CASE_WORK_ITEM);
        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"workItemId\":\"" + CASE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"handledBy\":\"" + SCOPED_WORKBENCH_USER + "\"")))
                .andExpect(content().string(containsString("\"trialStatus\":")))
                .andExpect(content().string(containsString("\"trialSummary\":")))
                .andExpect(content().string(containsString("\"trialBaselineTotal\":")))
                .andExpect(content().string(containsString("\"trialCalculatedTotal\":")))
                .andExpect(content().string(containsString("\"trialExpectedTotal\":")))
                .andExpect(content().string(containsString("\"trialChanges\":")))
                .andExpect(content().string(containsString("\"audits\":")))
                .andExpect(content().string(containsString("\"snapshotExists\":true")))
                .andExpect(content().string(containsString("\"snapshotBy\":\"" + SCOPED_WORKBENCH_USER + "\"")))
                .andExpect(content().string(containsString("\"snapshotAt\":")))
                .andExpect(content().string(containsString("salary-case-done")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"forceReason\":\"unit-test force reason\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/snapshot")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"workItemId\":\"" + CASE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"snapshotBy\":\"" + SCOPED_WORKBENCH_USER + "\"")))
                .andExpect(content().string(containsString("\"salaryItems\":")))
                .andExpect(content().string(containsString("\"snapshotJson\":")))
                .andExpect(content().string(containsString("trialCalculatedTotal")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"workItemId\":\"" + CASE_WORK_ITEM + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"status\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"writable\":false")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("trial risk must be reviewed")))
                .andExpect(content().string(containsString("\"sidPlan\":")))
                .andExpect(content().string(containsString("\"fields\":")))
                .andExpect(content().string(containsString("\"issues\":")));
        assertHistoryWritePlan(caseNo, CASE_WORK_ITEM);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("History write preview is not writable")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&reviewStatus=PENDING&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewReason":"unit-test review reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"reviewReason\":\"unit-test review reason\"")))
                .andExpect(content().string(containsString("salary-case-review")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&reviewStatus=REVIEWED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&reviewStatus=PENDING&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        assertAudit("workbench", "salary-case-review", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "unit-test review reason");

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/cancel")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/cancel")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cancelReason":"unit-test cancel reason"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"status\":\"CANCELLED\"")))
                .andExpect(content().string(containsString("\"cancelReason\":\"unit-test cancel reason\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"status\":\"CANCELLED\"")))
                .andExpect(content().string(containsString("unit-test cancel reason")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=ALL&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"status\":\"CANCELLED\"")));

        mockMvc.perform(get("/api/workbench/items.csv?status=DONE&caseStatus=CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u8bd5\u7b97\u72b6\u6001")))
                .andExpect(content().string(containsString("\u8bd5\u7b97\u5f02\u5e38")))
                .andExpect(content().string(containsString("\u5df2\u64a4\u56de")))
                .andExpect(content().string(not(containsString("\"CANCELLED\""))));

        assertAudit("workbench", "salary-case-cancel", "SALARY_CASE", caseNo, SCOPED_WORKBENCH_USER, "unit-test cancel reason");

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-001","source":"SALARY_EVENT","businessType":"姝ｅ父妗ｆ","personCode":"001-00055","personName":"娴嬭瘯浜哄憳","orgCode":"001","year":2026,"month":1,"title":"姝ｅ父妗ｆ鍔炵悊","summary":"unit-test salary case done","force":true,"forceReason":"unit-test force reason again"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")))
                .andExpect(content().string(containsString("\"cancelReason\":\"\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"reviewReason\":\"\"")))
                .andExpect(content().string(containsString("salary-case-cancel")))
                .andExpect(content().string(containsString("unit-test cancel reason")))
                .andExpect(content().string(containsString("\"forceReason\":\"unit-test force reason again\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"status\":\"DONE\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&caseStatus=CANCELLED&keyword=unit-test salary case done&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":0")))
                .andExpect(content().string(containsString("\"items\":[]")));
    }

    @Test
    void historyWriteExecuteCreatesHisbaseRowAndUpdatesSidChain() throws Exception {
        insertTemporaryHistoryTemplate();
        String caseNo = HISTORY_WRITE_CASE_NO;

        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"%s","source":"SALARY_EVENT","businessType":"测试写入","personCode":"001-00055","personName":"History Write","orgCode":"001","year":2099,"month":1,"title":"History Write","summary":"unit-test history write success"}
                                """.formatted(HISTORY_WRITE_WORK_ITEM)))
                .andExpect(status().isOk());
        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, trial_status, trial_matched,
                                                 trial_baseline_total, trial_calculated_total, trial_expected_total,
                                                 trial_difference, trial_summary, trial_changes_json,
                                                 review_status, handled_by)
                VALUES (?, ?, 'SALARY_EVENT', 'DONE', '测试写入',
                        '001-00055', 'History Write', '001', 2099, 1,
                        'History Write', 'unit-test history write success', 'MATCH', 1,
                        1200, 1290, 1290, 0, 'unit-test history write success', '[]',
                        'PENDING', ?)
                """, caseNo, HISTORY_WRITE_WORK_ITEM, SCOPED_WORKBENCH_USER);
        ensureSnapshotTableForTest();
        jdbcTemplate.update("""
                INSERT INTO salary_business_case_snapshot(case_no, work_item_id, person_code, org_code,
                                                          event_year, event_month, business_type, trial_status,
                                                          trial_matched, trial_difference, trial_baseline_total,
                                                          trial_calculated_total, trial_expected_total,
                                                          trial_changes_json, salary_items_json, snapshot_json,
                                                          snapshot_by)
                VALUES (?, ?, '001-00055', '001', 2099, 1, '测试写入', 'MATCH',
                        1, 0, 1200, 1290, 1290,
                        '[]',
                        '[{"itemCode":"JCGZ2","itemName":"基础工资","amount":1234,"ruleNote":"unit-test"},{"itemCode":"GLGZ2","itemName":"工龄工资","amount":56,"ruleNote":"unit-test"}]',
                        '{"workItemId":"tmp-test-history-write-success","trialCalculatedTotal":1290,"salaryItems":[{"itemCode":"JCGZ2","amount":1234},{"itemCode":"GLGZ2","amount":56}]}',
                        ?)
                """, caseNo, HISTORY_WRITE_WORK_ITEM, SCOPED_WORKBENCH_USER);

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"READY\"")))
                .andExpect(content().string(containsString("\"writable\":true")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"planStatus\":\"PREPARED\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"NOT_WRITTEN\"")))
                .andExpect(content().string(containsString("\"comparisonMismatchCount\":0")))
                .andExpect(content().string(containsString("\"previewStatus\":\"READY\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"NOT_WRITTEN\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=PREPARED&comparisonStatus=NOT_WRITTEN&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"NOT_WRITTEN\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u8ba1\u5212\u53f7")))
                .andExpect(content().string(containsString("\u5bf9\u7167\u72b6\u6001")))
                .andExpect(content().string(containsString("\u5dee\u5f02\u6570\u91cf")))
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString("001-00055")))
                .andExpect(content().string(containsString("\u672a\u5199\u5165")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-preview?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"ready\":1")))
                .andExpect(content().string(containsString("\"blocked\":0")))
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"personCode\":\"001-00055\"")));

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-execute?status=PREPARED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":1")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":0")))
                .andExpect(content().string(containsString("\"status\":\"EXECUTED\"")))
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")));

        Map<String, Object> inserted = jdbcTemplate.queryForMap("""
                SELECT TRIM(id) AS id, TRIM(COALESCE(sid, '')) AS sid, hj2, jcgz2, glgz2
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsnf) = '2099'
                  AND TRIM(jsyf) = '1'
                  AND TRIM(jslb) = '测试写入'
                LIMIT 1
                """);
        String insertedId = String.valueOf(inserted.get("id"));
        org.junit.jupiter.api.Assertions.assertEquals(1290, ((Number) inserted.get("hj2")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(1234, ((Number) inserted.get("jcgz2")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(56, ((Number) inserted.get("glgz2")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals("", String.valueOf(inserted.get("sid")));

        String sourceSid = jdbcTemplate.queryForObject("""
                SELECT TRIM(COALESCE(sid, ''))
                FROM hisbase
                WHERE id = ?
                """, String.class, HISTORY_WRITE_SOURCE_ID);
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, sourceSid);

        Map<String, Object> plan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id, previous_history_id, next_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("EXECUTED", String.valueOf(plan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", String.valueOf(plan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(plan.get("inserted_history_id")));
        org.junit.jupiter.api.Assertions.assertEquals(HISTORY_WRITE_SOURCE_ID, String.valueOf(plan.get("previous_history_id")));
        org.junit.jupiter.api.Assertions.assertEquals("null", String.valueOf(plan.get("next_history_id")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"writePlanId\":\"HWP-" + caseNo + "\"")));

        Map<String, Object> planAfterExecutedPreview = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("EXECUTED", String.valueOf(planAfterExecutedPreview.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("SUCCESS", String.valueOf(planAfterExecutedPreview.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(planAfterExecutedPreview.get("inserted_history_id")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MATCHED\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MATCHED\"")))
                .andExpect(content().string(containsString("\"comparisonMismatchCount\":0")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&reviewStatus=PENDING&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(not(containsString("\"comparisonReviewStatus\":\"REVIEWED\""))));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"insertedHistoryId\":\"" + insertedId + "\"")))
                .andExpect(content().string(containsString("\"historyField\":\"jcgz2\"")))
                .andExpect(content().string(containsString("\"expectedAmount\":1234")))
                .andExpect(content().string(containsString("\"actualAmount\":1234")))
                .andExpect(content().string(containsString("\"totalMatched\":true")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u5199\u5165\u8ba1\u5212\u53f7")))
                .andExpect(content().string(containsString("HWP-" + caseNo)))
                .andExpect(content().string(containsString("jcgz2")))
                .andExpect(content().string(containsString("1234")))
                .andExpect(content().string(containsString(insertedId)));

        jdbcTemplate.update("UPDATE hisbase SET glgz2 = ? WHERE id = ?", 99, insertedId);

        jdbcTemplate.update("""
                INSERT INTO sys_audit_log(module_name, action_name, target_type, target_code, summary, operator)
                VALUES ('workbench', 'history-write-comparison-retest', 'SALARY_CASE', ?, 'unit-test retest mismatch', ?)
                """, caseNo, SCOPED_WORKBENCH_USER);

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=base&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"MISMATCHED\"")))
                .andExpect(content().string(containsString("\"comparisonRetestStatus\":\"RETEST_MISMATCHED\"")))
                .andExpect(content().string(containsString("\"maintenanceSuggestionJson\"")))
                .andExpect(content().string(containsString("\\\"target\\\":\\\"base\\\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=post&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"planNo\":\"HWP-" + caseNo + "\""))));

        mockMvc.perform(get("/api/workbench/history-write-review-ledger?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=base&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"byMaintenanceTarget\"")))
                .andExpect(content().string(containsString("\"key\":\"base\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans.csv?status=EXECUTED&comparisonStatus=MISMATCHED&maintenanceTarget=base&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u5efa\u8bae\u68c0\u67e5\u65b9\u5411")))
                .andExpect(content().string(containsString("\u5efa\u8bae\u5b57\u6bb5")))
                .andExpect(content().string(containsString("HWP-" + caseNo)));

        jdbcTemplate.update("UPDATE hisbase SET glgz2 = ? WHERE id = ?", 56, insertedId);

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-retest-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"matched\":1")))
                .andExpect(content().string(containsString("\"status\":\"MATCHED\"")));

        mockMvc.perform(post("/api/workbench/history-write-plans/selected-retest-approve")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caseNos\":[\"" + caseNo + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":1")))
                .andExpect(content().string(containsString("\"status\":\"REVIEWED\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison-retest")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"caseNo\":\"" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"totalMatched\":true")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison-retest-approve")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"reviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"reviewReason\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"comparisonRetestStatus\":\"RETEST_MATCHED\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&retestStatus=RETEST_MATCHED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonRetestStatus\":\"RETEST_MATCHED\"")));

        mockMvc.perform(get("/api/workbench/history-write-review-ledger?status=EXECUTED&comparisonStatus=MATCHED&retestStatus=RETEST_MATCHED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"retestMatched\":1")))
                .andExpect(content().string(containsString("\"byRetestStatus\"")))
                .andExpect(content().string(containsString("\"key\":\"RETEST_MATCHED\"")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-comparison-review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewCategory\":\"BASE_CHANGED\",\"reviewReason\":\"unit-test comparison reviewed\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"reviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"reviewReason\":\"unit-test comparison reviewed\"")))
                .andExpect(content().string(containsString("\"reviewedBy\":\"" + SCOPED_WORKBENCH_USER + "\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"comparisonReviewStatus\":\"REVIEWED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewCategory\":\"BASE_CHANGED\"")))
                .andExpect(content().string(containsString("\"comparisonReviewReason\":\"unit-test comparison reviewed\"")));

        mockMvc.perform(get("/api/workbench/history-write-plans?status=EXECUTED&comparisonStatus=MATCHED&reviewStatus=REVIEWED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planNo\":\"HWP-" + caseNo + "\"")))
                .andExpect(content().string(containsString("\"comparisonReviewStatus\":\"REVIEWED\"")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-comparison-review\"")))
                .andExpect(content().string(containsString("unit-test comparison reviewed")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("already been executed")));

        Integer duplicateWriteCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsnf) = '2099'
                  AND TRIM(jsyf) = '1'
                  /*
                  AND TRIM(jslb) = '娴嬭瘯鍐欏叆'
                  */
                """, Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(1, duplicateWriteCount);

        Integer insertedStillExists = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ?
                """, Integer.class, insertedId);
        org.junit.jupiter.api.Assertions.assertEquals(1, insertedStillExists);

        mockMvc.perform(post("/api/workbench/history-write-plans/batch-rollback?status=EXECUTED&keyword=tmp-test-history&limit=5")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"success\":1")))
                .andExpect(content().string(containsString("\"failed\":0")))
                .andExpect(content().string(containsString("\"skipped\":0")))
                .andExpect(content().string(containsString("\"status\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"historyId\":\"" + insertedId + "\"")));

        Integer insertedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM hisbase
                WHERE id = ?
                """, Integer.class, insertedId);
        org.junit.jupiter.api.Assertions.assertEquals(0, insertedCount);
        sourceSid = jdbcTemplate.queryForObject("""
                SELECT TRIM(COALESCE(sid, ''))
                FROM hisbase
                WHERE id = ?
                """, String.class, HISTORY_WRITE_SOURCE_ID);
        org.junit.jupiter.api.Assertions.assertEquals("", sourceSid);

        Map<String, Object> rolledBackPlan = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id, rollback_message
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("ROLLED_BACK", String.valueOf(rolledBackPlan.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("ROLLED_BACK", String.valueOf(rolledBackPlan.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(rolledBackPlan.get("inserted_history_id")));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(rolledBackPlan.get("rollback_message")).contains(insertedId));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-execute\"")))
                .andExpect(content().string(containsString("\"action\":\"history-write-batch-rollback\"")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-audits.csv")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\u5ba1\u8ba1ID")))
                .andExpect(content().string(containsString("history-write-batch-execute")))
                .andExpect(content().string(containsString("history-write-batch-rollback")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo + "/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"planStatus\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"executionResult\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"comparisonStatus\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("\"rollbackMessage\":")));

        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"historyWritePlan\":")))
                .andExpect(content().string(containsString("\"historyWriteAudits\":")))
                .andExpect(content().string(containsString("\"planStatus\":\"ROLLED_BACK\"")))
                .andExpect(content().string(containsString("history-write-batch-rollback")))
                .andExpect(content().string(containsString(insertedId)));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"BLOCKED\"")))
                .andExpect(content().string(containsString("\"writable\":false")))
                .andExpect(content().string(containsString("rolled back history write plans cannot be executed again")));

        Map<String, Object> rolledBackPlanAfterPreview = jdbcTemplate.queryForMap("""
                SELECT plan_status, execution_result, inserted_history_id
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, HISTORY_WRITE_WORK_ITEM);
        org.junit.jupiter.api.Assertions.assertEquals("ROLLED_BACK", String.valueOf(rolledBackPlanAfterPreview.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals("ROLLED_BACK", String.valueOf(rolledBackPlanAfterPreview.get("execution_result")));
        org.junit.jupiter.api.Assertions.assertEquals(insertedId, String.valueOf(rolledBackPlanAfterPreview.get("inserted_history_id")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("cannot be executed again")));

        mockMvc.perform(post("/api/workbench/salary-cases/" + caseNo + "/history-write-rollback")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Only successful executed history write plans can be rolled back")));
    }

    @Test
    void salaryDifferentTrialRequiresDifferenceReason() throws Exception {
        Optional<DifferentTrialSample> sample = differentTrialSample();
        assumeTrue(sample.isPresent(), "No DIFFERENT salary trial sample in org 001.");
        DifferentTrialSample item = sample.get();

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-difference","source":"SALARY_EVENT","businessType":"%s","personCode":"%s","personName":"Difference Test","orgCode":"%s","year":%d,"month":%d,"title":"Difference Test","summary":"unit-test salary case difference"}
                                """.formatted(item.changeType(), item.personCode(), item.orgCode(), item.year(), item.month())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("\"code\":\"BAD_REQUEST\"")));

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-difference","source":"SALARY_EVENT","businessType":"%s","personCode":"%s","personName":"Difference Test","orgCode":"%s","year":%d,"month":%d,"title":"Difference Test","summary":"unit-test salary case difference","differenceReason":"unit-test difference reason"}
                                """.formatted(item.changeType(), item.personCode(), item.orgCode(), item.year(), item.month())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"DONE\"")));

        String caseNo = caseNo("tmp-test-salary-case-difference");
        mockMvc.perform(get("/api/workbench/salary-cases/" + caseNo)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"trialStatus\":\"DIFFERENT\"")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")))
                .andExpect(content().string(containsString("\"differenceReason\":\"unit-test difference reason\"")))
                .andExpect(content().string(containsString("differenceReason=unit-test difference reason")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=DIFFERENT&keyword=unit-test salary case difference&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"trialStatus\":\"DIFFERENT\"")));

        mockMvc.perform(get("/api/workbench/items?status=DONE&trialStatus=DIFFERENT&reviewStatus=PENDING&keyword=unit-test salary case difference&limit=3")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"total\":1")))
                .andExpect(content().string(containsString("\"reviewStatus\":\"PENDING\"")));
    }

    @Test
    void salaryCaseCompletionRespectsOrganizationScope() throws Exception {
        mockMvc.perform(post("/api/workbench/salary-cases/preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-denied-preview","source":"SALARY_EVENT","businessType":"姝ｅ父妗ｆ","personCode":"00806-00868","personName":"娴嬭瘯浜哄憳","orgCode":"00806","year":2026,"month":1,"title":"姝ｅ父妗ｆ鍔炵悊","summary":"unit-test denied salary case preview"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemId":"tmp-test-salary-case-denied","source":"SALARY_EVENT","businessType":"正常档次","personCode":"00806-00868","personName":"测试人员","orgCode":"00806","year":2026,"month":1,"title":"正常档次办理","summary":"unit-test denied salary case"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void salaryCaseDetailRespectsOrganizationScope() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO salary_business_case(case_no, work_item_id, source, status, business_type,
                                                 person_code, person_name, org_code, event_year, event_month,
                                                 title, summary, handled_by)
                VALUES ('GZ-UNIT-TEST-DENIED', 'tmp-test-denied-detail', 'SALARY_EVENT', 'DONE', '姝ｅ父妗ｆ',
                        '00806-00868', 'Denied Detail', '00806', 2026, 1,
                        'Denied Detail', 'unit-test denied detail', 'admin')
                """);

        mockMvc.perform(get("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/cancel")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cancelReason":"denied"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/review")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewReason":"denied"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/snapshot")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-preview")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-plan")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-execute")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/workbench/salary-cases/GZ-UNIT-TEST-DENIED/history-write-rollback")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, SCOPED_WORKBENCH_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleTemplatesRequireRolePermissionAndCanApplyTemplate() throws Exception {
        mockMvc.perform(get("/api/system/role-templates")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, WORKBENCH_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/system/role-templates")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_OPERATOR")));

        mockMvc.perform(put("/api/system/roles/" + WORKBENCH_ROLE + "/template/SALARY_VIEWER")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SALARY_PERSON")))
                .andExpect(content().string(containsString("SALARY_DONE")));
    }

    @Test
    void createUserCanAssignInitialRolesAndOrganizations() throws Exception {
        mockMvc.perform(post("/api/system/users")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tmp_test_created_user","displayName":"Created User","roleCodes":["%s"],"orgCodes":["001","00111"]}
                                """.formatted(WORKBENCH_ROLE)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(CREATED_USER)))
                .andExpect(content().string(containsString(WORKBENCH_ROLE)))
                .andExpect(content().string(containsString("\"001\"")));

        Integer roleCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user_role
                WHERE username = ? AND role_code = ?
                """, Integer.class, CREATED_USER, WORKBENCH_ROLE);
        Integer orgCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user_org
                WHERE username = ? AND org_code = '001'
                """, Integer.class, CREATED_USER);
        Integer childOrgCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user_org
                WHERE username = ? AND org_code = '00111'
                """, Integer.class, CREATED_USER);

        org.junit.jupiter.api.Assertions.assertEquals(1, roleCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, orgCount);
        org.junit.jupiter.api.Assertions.assertEquals(0, childOrgCount);
    }

    @Test
    void authorizationChangesAreAudited() throws Exception {
        mockMvc.perform(put("/api/system/roles/" + WORKBENCH_ROLE + "/template/SALARY_VIEWER")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/system/users")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"tmp_test_created_user","displayName":"Created User","roleCodes":["%s"],"orgCodes":["001"]}
                                """.formatted(WORKBENCH_ROLE)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/system/users/" + CREATED_USER + "/orgs")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ADMIN_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orgCodes":["001","00111"]}
                                """))
                .andExpect(status().isOk());

        assertAudit("role-template", "ROLE", WORKBENCH_ROLE, "ADMIN", "SALARY_VIEWER");
        assertAudit("user-create", "USER", CREATED_USER, "ADMIN", "Created User");
        assertAudit("user-roles", "USER", CREATED_USER, "ADMIN", WORKBENCH_ROLE);
        assertAudit("user-orgs", "USER", CREATED_USER, "ADMIN", "001");
    }

    @Test
    void organizationScopeRestrictsPeopleApis() throws Exception {
        mockMvc.perform(get("/api/persons?orgCode=001&page=1&size=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/persons?orgCode=00806&page=1&size=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/persons/001-00055")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/persons/00806-00868")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizationScopeRestrictsOrganizationTree() throws Exception {
        mockMvc.perform(get("/api/org/tree")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"orgCode\":\"001\"")))
                .andExpect(content().string(containsString("\"orgCode\":\"00111\"")))
                .andExpect(content().string(not(containsString("\"orgCode\":\"00806\""))));
    }

    @Test
    void organizationScopeRestrictsSalaryApis() throws Exception {
        mockMvc.perform(get("/api/salary/periods?orgCode=001&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salary/periods?orgCode=00806&limit=1")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/salary/history/001-00055")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salary/history/00806-00868")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizationScopeRestrictsSalaryHistoryDetails() throws Exception {
        String allowedHistoryId = historyId("001", "00055");
        String deniedHistoryId = historyId("00806", "00868");

        mockMvc.perform(get("/api/salary/history-records/" + allowedHistoryId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/salary/history-records/" + deniedHistoryId)
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, ORG_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizationScopeRestrictsSalaryActionCommands() throws Exception {
        mockMvc.perform(post("/api/salary/trial-calc")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, TRIAL_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCode":"00806-00868","orgCode":"00806","year":2024,"month":11,"changeType":"见习工资"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/salary/reconcile")
                        .sessionAttr(AuthSessionService.SESSION_USERNAME, RECONCILE_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"personCode":"00806-00868","orgCode":"00806","year":2024,"month":11,"changeType":"见习工资"}
                                """))
                .andExpect(status().isForbidden());
    }

    private void createUserRole(String username, String roleCode, String... menuCodes) {
        jdbcTemplate.update("""
                INSERT INTO sys_user(username, display_name, password_hash, status)
                VALUES (?, ?, '{noop}123456', 'ACTIVE')
                """, username, username);
        jdbcTemplate.update("""
                INSERT INTO sys_role(code, name, status)
                VALUES (?, ?, 'ACTIVE')
                """, roleCode, roleCode);
        jdbcTemplate.update("""
                INSERT INTO sys_user_role(username, role_code)
                VALUES (?, ?)
                """, username, roleCode);
        for (String menuCode : menuCodes) {
            jdbcTemplate.update("""
                    INSERT INTO sys_role_menu(role_code, menu_code)
                    VALUES (?, ?)
                    """, roleCode, menuCode);
        }
    }

    private void cleanup() {
        jdbcTemplate.update("DELETE FROM salary_business_case WHERE work_item_id IN (?, ?, ?, ?, ?, ?, ?)",
                CASE_WORK_ITEM, "tmp-test-salary-case-denied", "tmp-test-denied-detail",
                "tmp-test-salary-case-requires-force", "tmp-test-salary-case-missing-force-reason",
                "tmp-test-salary-case-difference", HISTORY_WRITE_WORK_ITEM);
        if (tableExists("salary_business_case_snapshot")) {
            jdbcTemplate.update("DELETE FROM salary_business_case_snapshot WHERE work_item_id IN (?, ?, ?, ?, ?, ?, ?)",
                    CASE_WORK_ITEM, "tmp-test-salary-case-denied", "tmp-test-denied-detail",
                    "tmp-test-salary-case-requires-force", "tmp-test-salary-case-missing-force-reason",
                    "tmp-test-salary-case-difference", HISTORY_WRITE_WORK_ITEM);
        }
        if (tableExists("salary_history_write_plan")) {
            jdbcTemplate.update("DELETE FROM salary_history_write_plan WHERE work_item_id IN (?, ?, ?, ?, ?, ?, ?)",
                    CASE_WORK_ITEM, "tmp-test-salary-case-denied", "tmp-test-denied-detail",
                    "tmp-test-salary-case-requires-force", "tmp-test-salary-case-missing-force-reason",
                    "tmp-test-salary-case-difference", HISTORY_WRITE_WORK_ITEM);
        }
        jdbcTemplate.update("""
                DELETE FROM hisbase
                WHERE id = ?
                   OR (dwbm = '001' AND grbm = '00055' AND TRIM(jsnf) = '2099' AND TRIM(jsyf) = '1' AND TRIM(jslb) = '测试写入')
                """, HISTORY_WRITE_SOURCE_ID);
        if (tableExists("salary_todo_candidate_cache")) {
            jdbcTemplate.update("DELETE FROM salary_todo_candidate_cache WHERE work_item_id = 'tmp-test-todo-latest-change'");
        }
        if (tableExists("sys_user_work_state")) {
            jdbcTemplate.update("""
                    DELETE FROM sys_user_work_state
                    WHERE username IN (?, ?, ?, ?, ?, ?, ?, ?)
                    """, ADMIN_USER, WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER,
                    ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        }
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = '001' AND grbm = 'UT001'");
        jdbcTemplate.update("""
                DELETE FROM dryzwbh
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(jsbz) IN ('UTEST', 'UTEST2')
                """);
        jdbcTemplate.update("""
                DELETE FROM dxl
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND TRIM(bz) IN ('UTEST', 'UTEST2')
                """);
        jdbcTemplate.update("""
                DELETE FROM dndkh
                WHERE dwbm = '001'
                  AND grbm = '00055'
                  AND khnd = '2098'
                """);
        jdbcTemplate.update("""
                DELETE FROM person_base_change_log
                WHERE source_id = 'unit-test'
                   OR summary LIKE '%unit-test base change%'
                   OR summary LIKE '%unit-test denied base change%'
                   OR summary LIKE '%unit-test post create%'
                   OR summary LIKE '%unit-test post update%'
                   OR summary LIKE '%unit-test education create%'
                   OR summary LIKE '%unit-test education update%'
                   OR summary LIKE '%unit-test assessment create%'
                   OR summary LIKE '%unit-test assessment update%'
                   OR summary LIKE '%unit-test base info update%'
                   OR summary LIKE '%unit-test latest base summary%'
                """);
        jdbcTemplate.update("DELETE FROM sys_user_org WHERE username IN (?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE username IN (?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        jdbcTemplate.update("DELETE FROM sys_user WHERE username IN (?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER, CREATED_USER);
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_code IN (?, ?, ?, ?, ?, ?)",
                WORKBENCH_ROLE, TODO_ROLE, SCOPED_WORKBENCH_ROLE, ORG_ROLE, TRIAL_ROLE, RECONCILE_ROLE);
        jdbcTemplate.update("DELETE FROM sys_role WHERE code IN (?, ?, ?, ?, ?, ?)",
                WORKBENCH_ROLE, TODO_ROLE, SCOPED_WORKBENCH_ROLE, ORG_ROLE, TRIAL_ROLE, RECONCILE_ROLE);
        jdbcTemplate.update("DELETE FROM sys_audit_log WHERE target_code IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                WORKBENCH_USER, TODO_USER, SCOPED_WORKBENCH_USER, ORG_USER, TRIAL_USER, RECONCILE_USER,
                CREATED_USER, WORKBENCH_ROLE, TODO_ROLE, SCOPED_WORKBENCH_ROLE, ORG_ROLE, TRIAL_ROLE, RECONCILE_ROLE,
                HISTORY_WRITE_CASE_NO);
        jdbcTemplate.update("""
                DELETE FROM sys_audit_log
                WHERE summary LIKE '%unit-test base change%'
                   OR summary LIKE '%unit-test denied base change%'
                   OR summary LIKE '%unit-test post create%'
                   OR summary LIKE '%unit-test post update%'
                   OR summary LIKE '%unit-test denied post%'
                   OR summary LIKE '%unit-test education create%'
                   OR summary LIKE '%unit-test education update%'
                   OR summary LIKE '%unit-test denied education%'
                   OR summary LIKE '%unit-test assessment create%'
                   OR summary LIKE '%unit-test assessment update%'
                   OR summary LIKE '%unit-test denied assessment%'
                   OR summary LIKE '%unit-test base info update%'
                   OR summary LIKE '%unit-test denied base info%'
                   OR summary LIKE '%unit-test latest base summary%'
                   OR target_type = 'PERSON_POST'
                   OR target_type = 'PERSON_EDUCATION'
                   OR target_type = 'PERSON_ASSESSMENT'
                   OR target_code = '001-UT001'
                   OR summary LIKE '%001-00055 dryzwbh%'
                """);
    }

    private void createTemporaryPerson() {
        cleanupTemporaryPersonOnly();
        jdbcTemplate.update("""
                INSERT INTO dryjbxx(dwbm, grbm, xm, sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny,
                                    zzny, jrny, jrfs, zdgznx, gznx, jhlqsny, zdjhlnx, xlbm,
                                    zgxl, bjglxlnx, tc, txsj, bgdwjc, zwjb, zjbm, xrzw, srny,
                                    tgbl, jtbl, fddc, khqk, dynkh, denkh, bbz, bh, gryhzh,
                                    spdw, mz, zzmm, fdgd, fdsj, jzgb, ydwzw, yzwrzsj, dah,
                                    sfjzgb, yctxsj)
                SELECT dwbm, 'UT001', 'UTEST', sfzh, xb, csny, ryfl, dwsx, gwfl, cjgzny,
                       zzny, jrny, jrfs, zdgznx, gznx, jhlqsny, zdjhlnx, xlbm,
                       zgxl, bjglxlnx, tc, txsj, bgdwjc, zwjb, zjbm, xrzw, srny,
                       tgbl, jtbl, fddc, khqk, dynkh, denkh, bbz, bh, gryhzh,
                       spdw, mz, zzmm, fdgd, fdsj, jzgb, ydwzw, yzwrzsj, dah,
                       sfjzgb, yctxsj
                FROM dryjbxx
                WHERE dwbm = '001' AND grbm = '00055'
                LIMIT 1
                """);
    }

    private void cleanupTemporaryPersonOnly() {
        jdbcTemplate.update("DELETE FROM dryjbxx WHERE dwbm = '001' AND grbm = 'UT001'");
    }

    private void insertTemporaryHistoryTemplate() {
        jdbcTemplate.update("DELETE FROM hisbase WHERE id = ?", HISTORY_WRITE_SOURCE_ID);
        List<String> columns = jdbcTemplate.queryForList("""
                SELECT LOWER(column_name)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'hisbase'
                ORDER BY ordinal_position
                """, String.class);
        String columnSql = String.join(", ", columns);
        String selectSql = String.join(", ", columns.stream()
                .map(column -> switch (column) {
                    case "id" -> "?";
                    case "jsnf" -> "'2098'";
                    case "jsyf" -> "'12'";
                    case "jslb" -> "'测试模板'";
                    case "sid" -> "NULL";
                    default -> column;
                })
                .toList());
        jdbcTemplate.update("""
                INSERT INTO hisbase (__COLUMNS__)
                SELECT __SELECTS__
                FROM hisbase
                WHERE dwbm = '001'
                  AND grbm = '00055'
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC,
                         id DESC
                LIMIT 1
                """.replace("__COLUMNS__", columnSql).replace("__SELECTS__", selectSql), HISTORY_WRITE_SOURCE_ID);
    }

    private void ensureSnapshotTableForTest() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_business_case_snapshot (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    case_no VARCHAR(64) NOT NULL,
                    work_item_id VARCHAR(255) NOT NULL,
                    person_code VARCHAR(128) NOT NULL,
                    org_code VARCHAR(64) NOT NULL,
                    event_year INT NULL,
                    event_month INT NULL,
                    business_type VARCHAR(128) NOT NULL,
                    trial_status VARCHAR(32) NULL,
                    trial_matched TINYINT NULL,
                    trial_difference DECIMAL(18,2) NULL,
                    trial_baseline_total DECIMAL(18,2) NULL,
                    trial_calculated_total DECIMAL(18,2) NULL,
                    trial_expected_total DECIMAL(18,2) NULL,
                    trial_changes_json LONGTEXT NULL,
                    salary_items_json LONGTEXT NULL,
                    snapshot_json LONGTEXT NOT NULL,
                    snapshot_by VARCHAR(64) NULL,
                    snapshot_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_salary_case_snapshot_work_item (work_item_id),
                    KEY idx_salary_case_snapshot_case (case_no),
                    KEY idx_salary_case_snapshot_person (person_code),
                    KEY idx_salary_case_snapshot_org_period (org_code, event_year, event_month)
                )
                """);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private void assertAudit(String action, String targetType, String targetCode, String operator, String summaryPart) {
        assertAudit("system", action, targetType, targetCode, operator, summaryPart);
    }

    private void assertAudit(String module, String action, String targetType, String targetCode, String operator, String summaryPart) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_audit_log
                WHERE module_name = ?
                  AND action_name = ?
                  AND target_type = ?
                  AND target_code = ?
                  AND operator = ?
                  AND summary LIKE CONCAT('%', ?, '%')
                """, Integer.class, module, action, targetType, targetCode, operator, summaryPart);
        org.junit.jupiter.api.Assertions.assertTrue(count != null && count > 0,
                "Missing audit log for " + action + " " + targetCode);
    }

    private String caseNo(String workItemId) {
        return jdbcTemplate.queryForObject("""
                SELECT case_no
                FROM salary_business_case
                WHERE work_item_id = ?
                LIMIT 1
                """, String.class, workItemId);
    }

    private int countBusinessCase(String workItemId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM salary_business_case
                WHERE work_item_id = ?
                """, Integer.class, workItemId);
        return count == null ? 0 : count;
    }

    private void assertTrialSnapshot(String workItemId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT trial_status, trial_summary, trial_changes_json
                FROM salary_business_case
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        String status = String.valueOf(row.get("trial_status"));
        org.junit.jupiter.api.Assertions.assertTrue(
                status.equals("MATCH") || status.equals("DIFFERENT") || status.equals("ERROR"),
                "Unexpected trial status: " + status);
        org.junit.jupiter.api.Assertions.assertFalse(String.valueOf(row.get("trial_summary")).isBlank());
        org.junit.jupiter.api.Assertions.assertNotNull(row.get("trial_changes_json"));
    }

    private void assertBusinessCaseSnapshot(String workItemId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT trial_status, trial_changes_json, salary_items_json, snapshot_json
                FROM salary_business_case_snapshot
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        String status = String.valueOf(row.get("trial_status"));
        org.junit.jupiter.api.Assertions.assertTrue(
                status.equals("MATCH") || status.equals("DIFFERENT") || status.equals("ERROR"),
                "Unexpected snapshot trial status: " + status);
        org.junit.jupiter.api.Assertions.assertNotNull(row.get("trial_changes_json"));
        String snapshotJson = String.valueOf(row.get("snapshot_json"));
        org.junit.jupiter.api.Assertions.assertTrue(snapshotJson.contains(workItemId));
        org.junit.jupiter.api.Assertions.assertTrue(snapshotJson.contains("trialCalculatedTotal"));
        org.junit.jupiter.api.Assertions.assertNotNull(row.get("salary_items_json"));
        org.junit.jupiter.api.Assertions.assertTrue(snapshotJson.contains("salaryItems"));
    }

    private void assertHistoryWritePlan(String caseNo, String workItemId) {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT plan_no, preview_status, writable, plan_status, fields_json, issues_json, preview_json
                FROM salary_history_write_plan
                WHERE work_item_id = ?
                LIMIT 1
                """, workItemId);
        org.junit.jupiter.api.Assertions.assertEquals("HWP-" + caseNo, String.valueOf(row.get("plan_no")));
        org.junit.jupiter.api.Assertions.assertEquals("BLOCKED", String.valueOf(row.get("preview_status")));
        org.junit.jupiter.api.Assertions.assertEquals("PREPARED", String.valueOf(row.get("plan_status")));
        org.junit.jupiter.api.Assertions.assertEquals(0, ((Number) row.get("writable")).intValue());
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(row.get("fields_json")).contains("itemCode"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(row.get("issues_json")).contains("trial risk must be reviewed"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(row.get("preview_json")).contains("\"writePlanId\":\"HWP-" + caseNo + "\""));
    }

    private String historyId(String orgCode, String personNo) {
        return jdbcTemplate.queryForObject("""
                SELECT TRIM(id)
                FROM hisbase
                WHERE dwbm = ? AND grbm = ?
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC,
                         id DESC
                LIMIT 1
                """, String.class, orgCode, personNo);
    }

    private Optional<DifferentTrialSample> differentTrialSample() {
        List<DifferentTrialSample> samples = jdbcTemplate.query("""
                SELECT CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS person_code,
                       TRIM(dwbm) AS org_code,
                       CAST(TRIM(jsnf) AS UNSIGNED) AS event_year,
                       CAST(TRIM(jsyf) AS UNSIGNED) AS event_month,
                       TRIM(jslb) AS change_type
                FROM hisbase
                WHERE TRIM(dwbm) = '001'
                  AND CAST(TRIM(jsnf) AS UNSIGNED) >= 2006
                  AND TRIM(jslb) NOT IN ('\u6d25\u8d34\u53d8\u5316', '\u8c03\u6807\u664b\u5347')
                ORDER BY CAST(TRIM(jsnf) AS UNSIGNED) DESC,
                         CAST(TRIM(jsyf) AS UNSIGNED) DESC
                LIMIT 1000
                """, (rs, rowNum) -> new DifferentTrialSample(
                rs.getString("person_code"),
                rs.getString("org_code"),
                rs.getInt("event_year"),
                rs.getInt("event_month"),
                rs.getString("change_type")
        ));
        for (DifferentTrialSample sample : samples) {
            try {
                NormalGradeTrialResult result = normalGradeTrialService.trial(new NormalGradeTrialCommand(
                        sample.personCode(),
                        sample.orgCode(),
                        sample.year(),
                        sample.month(),
                        sample.changeType()
                ));
                if (!result.matchedExpected() && result.expectedHistoryId() != null && !result.expectedHistoryId().isBlank()) {
                    return Optional.of(sample);
                }
            } catch (RuntimeException ignored) {
                // Keep looking; not every legacy row can be replayed by the current rule path.
            }
        }
        return Optional.empty();
    }

    private record DifferentTrialSample(String personCode, String orgCode, int year, int month, String changeType) {
    }
}
