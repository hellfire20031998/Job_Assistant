package com.hellFire.JobAssistant.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hellFire.JobAssistant.config.JwtProperties;
import com.hellFire.JobAssistant.constant.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final JwtProperties jwtProperties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = resolveToken(request);
		if (StringUtils.hasText(token)) {
			jwtService.parseValid(token).ifPresent(valid -> {
				var auth = new UsernamePasswordAuthenticationToken(
						valid.subject(),
						null,
						valid.authorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			});
		}
		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.hasText(header) && header.startsWith(SecurityConstants.AUTHORIZATION_BEARER_PREFIX)) {
			return header.substring(SecurityConstants.AUTHORIZATION_BEARER_PREFIX.length()).trim();
		}
		if (request.getCookies() != null) {
			for (Cookie c : request.getCookies()) {
				if (jwtProperties.getCookieName().equals(c.getName()) && StringUtils.hasText(c.getValue())) {
					return c.getValue();
				}
			}
		}
		return null;
	}
}
