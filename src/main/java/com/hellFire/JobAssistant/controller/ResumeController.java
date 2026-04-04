package com.hellFire.JobAssistant.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hellFire.JobAssistant.constant.ApiConstants;
import com.hellFire.JobAssistant.dto.ApiResponse;
import com.hellFire.JobAssistant.dto.request.ResumeUpdateRequest;
import com.hellFire.JobAssistant.dto.response.ResumeResponse;
import com.hellFire.JobAssistant.dto.response.ResumeSummaryResponse;
import com.hellFire.JobAssistant.service.resume.ResumeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/me/resumes")
@RequiredArgsConstructor
public class ResumeController {

	private final ResumeService resumeService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<ResumeResponse> upload(
			Authentication authentication,
			@RequestParam("file") MultipartFile file) {
		return ApiResponse.ok(resumeService.upload(authentication, file));
	}

	@GetMapping
	public ApiResponse<List<ResumeSummaryResponse>> list(Authentication authentication) {
		return ApiResponse.ok(resumeService.list(authentication));
	}

	@GetMapping("/{id}")
	public ApiResponse<ResumeResponse> get(Authentication authentication, @PathVariable("id") String id) {
		return ApiResponse.ok(resumeService.get(authentication, id));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponse<ResumeResponse> update(
			Authentication authentication,
			@PathVariable("id") String id,
			@RequestBody ResumeUpdateRequest body) {
		return ApiResponse.ok(resumeService.update(authentication, id, body));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(Authentication authentication, @PathVariable("id") String id) {
		resumeService.delete(authentication, id);
		return ApiResponse.ok(null);
	}
}
