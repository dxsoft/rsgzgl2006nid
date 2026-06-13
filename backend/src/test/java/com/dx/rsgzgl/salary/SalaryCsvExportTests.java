package com.dx.rsgzgl.salary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SalaryCsvExportTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void batchReconcileCsvUsesReadableChineseHeaders() throws Exception {
        String content = responseText("/api/salary/reconcile-batch.csv?orgCode=001&year=2024&month=7&limit=1&changeType=BATCH");

        assertThat(content).contains("单位编码,年度,月份,检查人数,通过人数,差异人数,跳过人数,差额合计");
        assertThat(content).contains("人员编码,姓名,单位编码,单位名称,老系统金额,试算金额,差额,状态,消息");
        assertThat(content).doesNotContain("鍗曚綅");
    }

    @Test
    void normalGradeTrialCsvUsesReadableChineseHeaders() throws Exception {
        String content = responseText("/api/salary/rule-trial/normal-grade-batch.csv?orgCode=001&year=2023&month=1&limit=1");

        assertThat(content).contains("单位编码,年度,月份,检查人数,匹配人数,差异人数,无目标记录");
        assertThat(content).contains("人员编码,姓名,单位编码,单位名称,基线记录,目标记录,基线合计,试算合计,历史合计");
    }

    private String responseText(String url) throws Exception {
        byte[] content = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        String text = new String(content, StandardCharsets.UTF_8);
        return text.startsWith("\uFEFF") ? text.substring(1) : text;
    }
}
