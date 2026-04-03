package com.hellFire.JobAssistant.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	public OAuth2LogoutSuccessHandler(
			@Value("${app.auth.oauth2.logout-success-redirect:http://localhost:5173/}") String redirectUrl) {
		setDefaultTargetUrl(redirectUrl);
	}
}
