package com.hellFire.JobAssistant.model.resume;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code source} values: {@code resume_upload}, {@code manual}, {@code linkedin}.
 */
@Getter
@Setter
public class ResumeProfileMetadata {

	private String source;

	private Instant lastUpdated;

	private Double confidenceScore;
}
