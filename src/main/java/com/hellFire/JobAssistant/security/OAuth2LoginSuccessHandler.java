package com.hellFire.JobAssistant.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.hellFire.JobAssistant.config.JwtProperties;
import com.hellFire.JobAssistant.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final UserService userService;
	private final String failureRedirectUrl;

	public OAuth2LoginSuccessHandler(
			JwtService jwtService,
			JwtProperties jwtProperties,
			UserService userService,
			@Value("${app.auth.oauth2.login-success-redirect:http://localhost:5173/}") String redirectUrl,
			@Value("${app.auth.oauth2.login-failure-redirect:http://localhost:5173/login?error=oauth}") String failureRedirectUrl) {
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
		this.userService = userService;
		this.failureRedirectUrl = failureRedirectUrl;
		setDefaultTargetUrl(redirectUrl);
		setAlwaysUseDefaultTargetUrl(true);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
			getRedirectStrategy().sendRedirect(request, response, failureRedirectUrl);
			return;
		}
		Object id = oauth2User.getAttribute(CustomOAuth2UserService.ATTR_APP_USER_ID);
		if (id == null) {
			getRedirectStrategy().sendRedirect(request, response, failureRedirectUrl);
			return;
		}
		try {
			var user = userService.requireById(id.toString());
			String jwt = jwtService.createToken(user);

			ResponseCookie cookie = ResponseCookie.from(jwtProperties.getCookieName(), jwt)
					.httpOnly(true)
					.secure(jwtProperties.isCookieSecure())
					.path("/")
					.maxAge(Duration.ofMillis(jwtProperties.getExpirationMs()))
					.sameSite(jwtProperties.getCookieSameSite())
					.build();
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}

			String target = getDefaultTargetUrl();
			if (jwtProperties.isAppendTokenToRedirect()) {
				target = UriComponentsBuilder.fromUriString(target)
						.queryParam("access_token", jwt)
						.encode(StandardCharsets.UTF_8)
						.build()
						.toUriString();
			}
			getRedirectStrategy().sendRedirect(request, response, target);
		} catch (Exception ex) {
			log.warn("OAuth2 login success handling failed: {}", ex.toString());
			getRedirectStrategy().sendRedirect(request, response, failureRedirectUrl);
		}
	}
}
