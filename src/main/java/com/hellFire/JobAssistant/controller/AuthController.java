package com.hellFire.JobAssistant.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hellFire.JobAssistant.constant.ApiConstants;
import com.hellFire.JobAssistant.dto.ApiResponse;
import com.hellFire.JobAssistant.dto.response.UserProfileResponse;
import com.hellFire.JobAssistant.exception.BusinessException;
import com.hellFire.JobAssistant.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;

	@GetMapping("/me")
	public ApiResponse<UserProfileResponse> me(Authentication authentication) {
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new BusinessException("Not authenticated", 401);
		}
		var user = userService.requireUserForMe(authentication);
		return ApiResponse.ok(UserProfileResponse.from(user));
	}
}
