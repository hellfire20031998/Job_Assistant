package com.hellFire.JobAssistant.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hellFire.JobAssistant.constant.SecurityConstants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "users")
public class User extends BaseDocument {

	/**
	 * Stable external identity, e.g. {@code GOOGLE:sub-from-id-token}. Sparse unique skips documents without this field.
	 */
	@Indexed(unique = true, sparse = true)
	private String oauthPrincipalKey;

	@Indexed(unique = true, sparse = true)
	private String email;

	private boolean emailVerified;

	private String displayName;

	private String givenName;

	private String familyName;

	private String pictureUrl;

	private String locale;

	private AuthProvider provider;

	private String providerSubject;

	private Instant lastLoginAt;

	private Set<String> roles = new HashSet<>();

	public User() {
		this.roles.add(SecurityConstants.ROLE_USER);
	}
}
