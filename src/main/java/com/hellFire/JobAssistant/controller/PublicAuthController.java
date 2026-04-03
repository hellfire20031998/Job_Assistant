package com.hellFire.JobAssistant.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hellFire.JobAssistant.constant.ApiConstants;
import com.hellFire.JobAssistant.dto.ApiResponse;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/public/auth")
public class PublicAuthController {

	@GetMapping("/providers")
	public ApiResponse<Map<String, String>> providers() {
		return ApiResponse.ok(Map.of(
				"google", "/oauth2/authorization/google",
				"hint", "Redirect browser to Google OAuth on the API origin; after login you receive a JWT (HttpOnly cookie and optional query param)."
		));
	}
}
