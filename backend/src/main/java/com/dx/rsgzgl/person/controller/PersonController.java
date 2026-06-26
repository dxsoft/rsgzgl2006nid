package com.dx.rsgzgl.person.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.common.api.PageResponse;
import com.dx.rsgzgl.person.dto.PersonAssessmentRequest;
import com.dx.rsgzgl.person.dto.PersonAssessmentResponse;
import com.dx.rsgzgl.person.dto.PersonBaseChangeRequest;
import com.dx.rsgzgl.person.dto.PersonBaseChangeResponse;
import com.dx.rsgzgl.person.dto.PersonBaseInfoRequest;
import com.dx.rsgzgl.person.dto.PersonBaseInfoResponse;
import com.dx.rsgzgl.person.dto.PersonBaseStatusResponse;
import com.dx.rsgzgl.person.dto.PersonDetail;
import com.dx.rsgzgl.person.dto.PersonEducationRequest;
import com.dx.rsgzgl.person.dto.PersonEducationResponse;
import com.dx.rsgzgl.person.dto.PersonPostRequest;
import com.dx.rsgzgl.person.dto.PersonPostResponse;
import com.dx.rsgzgl.person.dto.PersonSummary;
import com.dx.rsgzgl.person.service.PersonAssessmentService;
import com.dx.rsgzgl.person.service.PersonBaseChangeService;
import com.dx.rsgzgl.person.service.PersonBaseInfoService;
import com.dx.rsgzgl.person.service.PersonBaseStatusService;
import com.dx.rsgzgl.person.service.PersonEducationService;
import com.dx.rsgzgl.person.service.PersonPostService;
import com.dx.rsgzgl.person.service.PersonQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonQueryService personQueryService;
    private final PersonBaseChangeService personBaseChangeService;
    private final PersonBaseInfoService personBaseInfoService;
    private final PersonBaseStatusService personBaseStatusService;
    private final PersonPostService personPostService;
    private final PersonEducationService personEducationService;
    private final PersonAssessmentService personAssessmentService;

    public PersonController(
            PersonQueryService personQueryService,
            PersonBaseChangeService personBaseChangeService,
            PersonBaseInfoService personBaseInfoService,
            PersonBaseStatusService personBaseStatusService,
            PersonPostService personPostService,
            PersonEducationService personEducationService,
            PersonAssessmentService personAssessmentService
    ) {
        this.personQueryService = personQueryService;
        this.personBaseChangeService = personBaseChangeService;
        this.personBaseInfoService = personBaseInfoService;
        this.personBaseStatusService = personBaseStatusService;
        this.personPostService = personPostService;
        this.personEducationService = personEducationService;
        this.personAssessmentService = personAssessmentService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PersonSummary>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgCode
    ) {
        return ApiResponse.ok(personQueryService.page(page, size, keyword, orgCode));
    }

    @GetMapping("/{personCode}")
    public ApiResponse<PersonDetail> detail(@PathVariable String personCode) {
        return ApiResponse.ok(personQueryService.detail(personCode));
    }

    @GetMapping("/{personCode}/base-info")
    public ApiResponse<PersonBaseInfoResponse> baseInfo(@PathVariable String personCode) {
        return ApiResponse.ok(personBaseInfoService.get(personCode));
    }

    @GetMapping("/{personCode}/base-status")
    public ApiResponse<PersonBaseStatusResponse> baseStatus(@PathVariable String personCode) {
        return ApiResponse.ok(personBaseStatusService.get(personCode));
    }

    @PutMapping("/{personCode}/base-info")
    public ApiResponse<PersonBaseInfoResponse> updateBaseInfo(
            @PathVariable String personCode,
            @RequestBody PersonBaseInfoRequest request
    ) {
        return ApiResponse.ok(personBaseInfoService.update(personCode, request));
    }

    @GetMapping("/{personCode}/base-changes")
    public ApiResponse<List<PersonBaseChangeResponse>> baseChanges(
            @PathVariable String personCode,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(personBaseChangeService.list(personCode, limit));
    }

    @PostMapping("/{personCode}/base-changes")
    public ApiResponse<PersonBaseChangeResponse> createBaseChange(
            @PathVariable String personCode,
            @RequestBody PersonBaseChangeRequest request
    ) {
        return ApiResponse.ok(personBaseChangeService.create(personCode, request));
    }

    @GetMapping("/{personCode}/posts")
    public ApiResponse<List<PersonPostResponse>> posts(@PathVariable String personCode) {
        return ApiResponse.ok(personPostService.list(personCode));
    }

    @PostMapping("/{personCode}/posts")
    public ApiResponse<PersonPostResponse> createPost(
            @PathVariable String personCode,
            @RequestBody PersonPostRequest request
    ) {
        return ApiResponse.ok(personPostService.create(personCode, request));
    }

    @PutMapping("/posts/{id}")
    public ApiResponse<PersonPostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PersonPostRequest request
    ) {
        return ApiResponse.ok(personPostService.update(id, request));
    }

    @GetMapping("/{personCode}/educations")
    public ApiResponse<List<PersonEducationResponse>> educations(@PathVariable String personCode) {
        return ApiResponse.ok(personEducationService.list(personCode));
    }

    @PostMapping("/{personCode}/educations")
    public ApiResponse<PersonEducationResponse> createEducation(
            @PathVariable String personCode,
            @RequestBody PersonEducationRequest request
    ) {
        return ApiResponse.ok(personEducationService.create(personCode, request));
    }

    @PutMapping("/educations/{id}")
    public ApiResponse<PersonEducationResponse> updateEducation(
            @PathVariable Long id,
            @RequestBody PersonEducationRequest request
    ) {
        return ApiResponse.ok(personEducationService.update(id, request));
    }

    @GetMapping("/{personCode}/assessments")
    public ApiResponse<List<PersonAssessmentResponse>> assessments(@PathVariable String personCode) {
        return ApiResponse.ok(personAssessmentService.list(personCode));
    }

    @PostMapping("/{personCode}/assessments")
    public ApiResponse<PersonAssessmentResponse> saveAssessment(
            @PathVariable String personCode,
            @RequestBody PersonAssessmentRequest request
    ) {
        return ApiResponse.ok(personAssessmentService.save(personCode, request));
    }

    @PutMapping("/assessments/{id}")
    public ApiResponse<PersonAssessmentResponse> updateAssessment(
            @PathVariable Long id,
            @RequestBody PersonAssessmentRequest request
    ) {
        return ApiResponse.ok(personAssessmentService.update(id, request));
    }
}
