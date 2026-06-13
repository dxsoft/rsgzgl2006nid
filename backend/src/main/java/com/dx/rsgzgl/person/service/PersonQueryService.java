package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.common.api.PageResponse;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.person.dto.PersonSummary;

public interface PersonQueryService {

    PageResponse<PersonSummary> page(long page, long size, String keyword, String orgCode);

    PersonDetail detail(String personCode);
}
