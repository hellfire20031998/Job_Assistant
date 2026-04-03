package com.hellFire.JobAssistant.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.hellFire.JobAssistant.constant.SecurityConstants;
import com.hellFire.JobAssistant.model.User;
import com.hellFire.JobAssistant.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	public static final String ATTR_APP_USER_ID = "appUserId";

	private final UserService userService;
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User loaded = delegate.loadUser(userRequest);
		User persisted = userService.syncFromGoogle(loaded);

		Map<String, Object> attrs = new HashMap<>(loaded.getAttributes());
		attrs.put(ATTR_APP_USER_ID, persisted.getId());

		String[] roles = persisted.getRoles() != null && !persisted.getRoles().isEmpty()
				? persisted.getRoles().toArray(new String[0])
				: new String[] { SecurityConstants.ROLE_USER };

		return new DefaultOAuth2User(AuthorityUtils.createAuthorityList(roles), attrs, "sub");
	}
}
