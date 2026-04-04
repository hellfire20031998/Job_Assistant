package com.hellFire.JobAssistant.security;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.hellFire.JobAssistant.model.User;
import com.hellFire.JobAssistant.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * Google uses OIDC when the {@code openid} scope is requested. Spring then uses {@link OidcUserService},
 * not the OAuth2 user-info endpoint service — so {@link CustomOAuth2UserService} alone never runs for Google.
 * We persist here.
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

	private final UserService userService;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);
		userService.syncFromGoogle(oidcUser);
		return oidcUser;
	}
}
