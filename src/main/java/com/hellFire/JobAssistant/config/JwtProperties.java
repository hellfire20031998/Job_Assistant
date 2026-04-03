package com.hellFire.JobAssistant.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	/**
	 * HMAC secret; must be at least 256 bits (32 UTF-8 bytes). Override via env in production.
	 */
	@NotBlank
	private String secret = "dev-only-change-me-min-32-chars-long-secret!!";

	@Min(60_000)
	private long expirationMs = 3_600_000L;

	private String cookieName = "accessToken";

	private boolean cookieSecure = false;

	/**
	 * Lax works for same-site localhost (different ports). Use None + secure cookie for cross-site HTTPS.
	 */
	private String cookieSameSite = "Lax";

	/**
	 * If true, appends {@code ?access_token=} to the OAuth success redirect (for clients that cannot use cookies).
	 */
	private boolean appendTokenToRedirect = false;

	@PostConstruct
	void validate() {
		if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("app.jwt.secret must be at least 32 bytes (UTF-8)");
		}
		String site = cookieSameSite != null ? cookieSameSite.trim() : "";
		String normalized = Stream.of("Lax", "Strict", "None")
				.filter(s -> s.toLowerCase(Locale.ROOT).equals(site.toLowerCase(Locale.ROOT)))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"app.jwt.cookie-same-site must be one of: Lax, Strict, None (got: " + cookieSameSite + ")"));
		this.cookieSameSite = normalized;
		if ("None".equals(normalized) && !cookieSecure) {
			throw new IllegalStateException("app.jwt.cookie-same-site=None requires app.jwt.cookie-secure=true");
		}
	}
}
