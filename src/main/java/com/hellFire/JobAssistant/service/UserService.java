package com.hellFire.JobAssistant.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.hellFire.JobAssistant.model.User;

public interface UserService {

	User syncFromGoogle(OAuth2User oauth2User);

	User requireById(String id);

	/**
	 * Resolves the current user for {@code /auth/me}: JWT principal (Mongo id), or {@link OAuth2User}
	 * via {@code appUserId}, {@code GOOGLE:sub}, or email (session may drop custom OAuth attributes).
	 */
	User requireUserForMe(Authentication authentication);
}
