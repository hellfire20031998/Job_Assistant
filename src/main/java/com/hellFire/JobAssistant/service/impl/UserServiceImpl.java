package com.hellFire.JobAssistant.service.impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hellFire.JobAssistant.constant.SecurityConstants;
import com.hellFire.JobAssistant.exception.BusinessException;
import com.hellFire.JobAssistant.model.AuthProvider;
import com.hellFire.JobAssistant.model.User;
import com.hellFire.JobAssistant.repository.UserRepository;
import com.hellFire.JobAssistant.security.CustomOAuth2UserService;
import com.hellFire.JobAssistant.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public User syncFromGoogle(OAuth2User oauth2User) {
		Map<String, Object> attrs = oauth2User.getAttributes();
		String subject = stringAttr(attrs, "sub");
		String email = stringAttr(attrs, "email");
		if (subject == null || subject.isBlank()) {
			throw new BusinessException("OAuth account is missing subject (sub)", 400);
		}
		if (email == null || email.isBlank()) {
			throw new BusinessException("Google did not return an email. Ensure scope includes email.", 400);
		}

		boolean verified = Boolean.TRUE.equals(attrs.get("email_verified"));

		String oauthKey = oauthPrincipalKey(AuthProvider.GOOGLE, subject);
		User user = userRepository
				.findByOauthPrincipalKey(oauthKey)
				.orElseGet(() -> userRepository.findByEmailIgnoreCase(email).orElseGet(User::new));

		user.setOauthPrincipalKey(oauthKey);
		user.setProvider(AuthProvider.GOOGLE);
		user.setProviderSubject(subject);
		user.setEmail(email.trim().toLowerCase());
		user.setEmailVerified(verified);
		user.setDisplayName(stringAttr(attrs, "name"));
		user.setGivenName(stringAttr(attrs, "given_name"));
		user.setFamilyName(stringAttr(attrs, "family_name"));
		user.setPictureUrl(stringAttr(attrs, "picture"));
		user.setLocale(stringAttr(attrs, "locale"));
		user.setLastLoginAt(Instant.now());
		if (user.getRoles() == null) {
			user.setRoles(new HashSet<>());
		}
		if (user.getRoles().isEmpty()) {
			user.getRoles().add(SecurityConstants.ROLE_USER);
		}

		return userRepository.save(user);
	}

	@Override
	public User requireById(String id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new BusinessException("User not found", 404));
	}

	@Override
	public User requireUserForMe(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof OAuth2User ou) {
			Object appId = ou.getAttribute(CustomOAuth2UserService.ATTR_APP_USER_ID);
			if (appId != null && StringUtils.hasText(appId.toString())) {
				return requireById(appId.toString());
			}
			String sub = ou.getName();
			if (StringUtils.hasText(sub)) {
				String oauthKey = oauthPrincipalKey(AuthProvider.GOOGLE, sub);
				Optional<User> byKey = userRepository.findByOauthPrincipalKey(oauthKey);
				if (byKey.isPresent()) {
					return byKey.get();
				}
			}
			String email = stringAttr(ou.getAttributes(), "email");
			if (StringUtils.hasText(email)) {
				return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
						.orElseThrow(() -> new BusinessException("User not found", 404));
			}
			throw new BusinessException("User not found", 404);
		}
		String mongoId = authentication.getName();
		if (!StringUtils.hasText(mongoId)) {
			throw new BusinessException("Not authenticated", 401);
		}
		return requireById(mongoId);
	}

	private static String stringAttr(Map<String, Object> attrs, String key) {
		Object v = attrs.get(key);
		if (v == null) {
			return null;
		}
		String s = Objects.toString(v, null);
		return s != null && !s.isBlank() ? s : null;
	}

	private static String oauthPrincipalKey(AuthProvider provider, String subject) {
		return provider.name() + ":" + subject;
	}
}
