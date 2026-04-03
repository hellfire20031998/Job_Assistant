package com.hellFire.JobAssistant.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.hellFire.JobAssistant.config.JwtProperties;
import com.hellFire.JobAssistant.constant.SecurityConstants;
import com.hellFire.JobAssistant.model.User;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		JwtProperties props = new JwtProperties();
		props.setSecret("unittest-secret-must-be-32-chars-long-ok!");
		props.setExpirationMs(3_600_000L);
		ReflectionTestUtils.invokeMethod(props, "validate");
		jwtService = new JwtService(props);
		ReflectionTestUtils.invokeMethod(jwtService, "initKey");
	}

	@Test
	void createTokenThenParseValid() {
		User user = new User();
		user.setId("user-1");
		user.setEmail("a@example.com");

		String token = jwtService.createToken(user);

		assertThat(jwtService.parseValid(token))
				.isPresent()
				.hasValueSatisfying(v -> {
					assertThat(v.subject()).isEqualTo("user-1");
					assertThat(v.authorities()).extracting("authority").contains(SecurityConstants.ROLE_USER);
				});
	}

	@Test
	void parseValidRejectsGarbage() {
		assertThat(jwtService.parseValid("not-a-jwt")).isEmpty();
		assertThat(jwtService.parseValid("")).isEmpty();
	}
}
