package com.hellFire.JobAssistant.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public OAuth2LoginFailureHandler(
			@Value("${app.auth.oauth2.login-failure-redirect:http://localhost:5173/login?error=oauth}") String redirectUrl) {
		setDefaultFailureUrl(redirectUrl);
	}
}
