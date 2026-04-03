package com.hellFire.JobAssistant.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

import com.hellFire.JobAssistant.model.User;

public interface UserService {

	User syncFromGoogle(OAuth2User oauth2User);

	User requireById(String id);
}
