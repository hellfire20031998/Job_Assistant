package com.hellFire.JobAssistant.dto.response;

import java.time.Instant;
import java.util.Set;

import com.hellFire.JobAssistant.model.User;

public record UserProfileResponse(
		String id,
		String email,
		boolean emailVerified,
		String displayName,
		String givenName,
		String familyName,
		String pictureUrl,
		String locale,
		String provider,
		Set<String> roles,
		Instant lastLoginAt,
		Instant memberSince
) {

	public static UserProfileResponse from(User user) {
		return new UserProfileResponse(
				user.getId(),
				user.getEmail(),
				user.isEmailVerified(),
				user.getDisplayName(),
				user.getGivenName(),
				user.getFamilyName(),
				user.getPictureUrl(),
				user.getLocale(),
				user.getProvider() != null ? user.getProvider().name() : null,
				user.getRoles() != null ? Set.copyOf(user.getRoles()) : Set.of(),
				user.getLastLoginAt(),
				user.getCreatedAt()
		);
	}
}
