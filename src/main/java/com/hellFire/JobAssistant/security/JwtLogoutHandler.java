package com.hellFire.JobAssistant.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.hellFire.JobAssistant.config.JwtProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

	private final JwtProperties jwtProperties;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		ResponseCookie clear = ResponseCookie.from(jwtProperties.getCookieName(), "")
				.httpOnly(true)
				.secure(jwtProperties.isCookieSecure())
				.path("/")
				.maxAge(0)
				.sameSite(jwtProperties.getCookieSameSite())
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, clear.toString());
	}
}
