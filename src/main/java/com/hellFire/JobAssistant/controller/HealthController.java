package com.hellFire.JobAssistant.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hellFire.JobAssistant.constant.ApiConstants;
import com.hellFire.JobAssistant.dto.ApiResponse;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX)
public class HealthController {

	@GetMapping("/health")
	public ApiResponse<Map<String, String>> health() {
		return ApiResponse.ok(Map.of("status", "UP"));
	}
}
