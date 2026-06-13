package com.dx.rsgzgl.person;

import com.dx.rsgzgl.common.api.PageResponse;
import com.dx.rsgzgl.person.dto.PersonSummary;
import com.dx.rsgzgl.person.service.PersonQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersonSearchRegressionTests {

    @Autowired
    private PersonQueryService personQueryService;

    @Test
    void fullPersonCodeSearchMatchesListResult() {
        PageResponse<PersonSummary> result = personQueryService.page(1, 20, "041-00210", null);

        assertThat(result.records())
                .extracting(PersonSummary::personCode)
                .contains("041-00210");
    }
}
