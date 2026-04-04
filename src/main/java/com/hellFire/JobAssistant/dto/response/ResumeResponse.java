package com.hellFire.JobAssistant.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.hellFire.JobAssistant.model.resume.CertificationEntry;
import com.hellFire.JobAssistant.model.resume.EducationEntry;
import com.hellFire.JobAssistant.model.resume.ExperienceEntry;
import com.hellFire.JobAssistant.model.resume.ParseStatus;
import com.hellFire.JobAssistant.model.resume.PersonalInfo;
import com.hellFire.JobAssistant.model.resume.ProjectEntry;
import com.hellFire.JobAssistant.model.resume.Resume;
import com.hellFire.JobAssistant.model.resume.ResumePreferences;
import com.hellFire.JobAssistant.model.resume.ResumeProfileMetadata;
import com.hellFire.JobAssistant.model.resume.ResumeSkills;

public record ResumeResponse(
		String id,
		String userId,
		String label,
		boolean isDefault,
		String extractedText,
		ParseStatus parseStatus,
		String parseError,
		PersonalInfo personalInfo,
		String summary,
		ResumeSkills skills,
		List<ExperienceEntry> experiences,
		List<ProjectEntry> projects,
		List<EducationEntry> education,
		List<CertificationEntry> certifications,
		List<String> achievements,
		List<String> languages,
		ResumePreferences preferences,
		ResumeProfileMetadata metadata,
		Map<String, Object> rawModelJson,
		Instant createdAt,
		Instant updatedAt
) {

	public static ResumeResponse from(Resume r) {
		return new ResumeResponse(
				r.getId(),
				r.getUserId(),
				r.getLabel(),
				r.isDefault(),
				r.getExtractedText(),
				r.getParseStatus(),
				r.getParseError(),
				r.getPersonalInfo(),
				r.getSummary(),
				r.getSkills(),
				r.getExperiences(),
				r.getProjects(),
				r.getEducation(),
				r.getCertifications(),
				r.getAchievements(),
				r.getLanguages(),
				r.getPreferences(),
				r.getMetadata(),
				r.getRawModelJson(),
				r.getCreatedAt(),
				r.getUpdatedAt());
	}
}
