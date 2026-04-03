package com.hellFire.JobAssistant.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final String[] corsOrigins;

	public WebMvcConfig(@Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}") String origins) {
		this.corsOrigins = Arrays.stream(origins.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedOrigins(corsOrigins)
				.allowCredentials(true);
	}
}
