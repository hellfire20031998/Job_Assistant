package com.hellFire.JobAssistant.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hellFire.JobAssistant.config.JwtProperties;
import com.hellFire.JobAssistant.constant.SecurityConstants;
import com.hellFire.JobAssistant.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtProperties properties;
	private SecretKey signingKey;

	@PostConstruct
	void initKey() {
		byte[] keyBytes = properties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Parsed JWT suitable for building a security context. Empty if missing, malformed, or invalid signature / expiry.
	 */
	public Optional<ValidatedJwt> parseValid(String rawToken) {
		if (!StringUtils.hasText(rawToken)) {
			return Optional.empty();
		}
		try {
			Claims claims = parseClaims(rawToken.trim());
			return Optional.of(new ValidatedJwt(claims.getSubject(), authoritiesFromClaims(claims)));
		} catch (JwtException | IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	public String createToken(User user) {
		List<String> roles = user.getRoles() != null && !user.getRoles().isEmpty()
				? new ArrayList<>(user.getRoles())
				: List.of(SecurityConstants.ROLE_USER);
		Date now = new Date();
		Date exp = new Date(now.getTime() + properties.getExpirationMs());
		return Jwts.builder()
				.subject(user.getId())
				.claim(SecurityConstants.JWT_CLAIM_ROLES, roles)
				.claim(SecurityConstants.JWT_CLAIM_EMAIL, user.getEmail())
				.issuedAt(now)
				.expiration(exp)
				.signWith(signingKey)
				.compact();
	}

	private List<SimpleGrantedAuthority> authoritiesFromClaims(Claims claims) {
		@SuppressWarnings("unchecked")
		List<String> roles = claims.get(SecurityConstants.JWT_CLAIM_ROLES, List.class);
		if (roles == null || roles.isEmpty()) {
			return List.of(new SimpleGrantedAuthority(SecurityConstants.ROLE_USER));
		}
		return roles.stream().map(SimpleGrantedAuthority::new).toList();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public record ValidatedJwt(String subject, List<SimpleGrantedAuthority> authorities) {
	}
}
