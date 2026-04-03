package com.hellFire.JobAssistant.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.hellFire.JobAssistant.security.CustomOAuth2UserService;
import com.hellFire.JobAssistant.security.JwtAuthenticationFilter;
import com.hellFire.JobAssistant.security.JwtLogoutHandler;
import com.hellFire.JobAssistant.security.OAuth2LoginFailureHandler;
import com.hellFire.JobAssistant.security.OAuth2LoginSuccessHandler;
import com.hellFire.JobAssistant.security.RestAccessDeniedHandler;
import com.hellFire.JobAssistant.security.RestAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
	private final LogoutSuccessHandler oAuth2LogoutSuccessHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtLogoutHandler jwtLogoutHandler;
	private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
	private final RestAccessDeniedHandler restAccessDeniedHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.headers(headers -> headers
						.contentTypeOptions(Customizer.withDefaults())
						.frameOptions(frame -> frame.deny())
						.referrerPolicy(referrer -> referrer.policy(
								ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(restAuthenticationEntryPoint)
						.accessDeniedHandler(restAccessDeniedHandler))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/public/**",
								"/api/v1/health",
								"/oauth2/**",
								"/login/oauth2/**",
								"/actuator/health",
								"/actuator/health/**",
								"/actuator/info",
								"/error"
						).permitAll()
						.anyRequest().authenticated()
				)
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(u -> u.userService(customOAuth2UserService))
						.successHandler(oAuth2LoginSuccessHandler)
						.failureHandler(oAuth2LoginFailureHandler)
				)
				.logout(logout -> logout
						.logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/logout"))
						.addLogoutHandler(jwtLogoutHandler)
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID")
						.logoutSuccessHandler(oAuth2LogoutSuccessHandler)
				);
		return http.build();
	}
}
