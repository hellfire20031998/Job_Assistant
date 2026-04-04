package com.hellFire.JobAssistant.dto.response;

import java.time.Instant;

import com.hellFire.JobAssistant.model.resume.ParseStatus;
import com.hellFire.JobAssistant.model.resume.Resume;

public record ResumeSummaryResponse(
		String id,
		String label,
		boolean isDefault,
		ParseStatus parseStatus,
		String parseError,
		Instant updatedAt
) {

	public static ResumeSummaryResponse from(Resume r) {
		return new ResumeSummaryResponse(
				r.getId(),
				r.getLabel(),
				r.isDefault(),
				r.getParseStatus(),
				r.getParseError(),
				r.getUpdatedAt());
	}
}
