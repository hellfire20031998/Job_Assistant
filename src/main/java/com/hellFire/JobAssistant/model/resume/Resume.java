package com.hellFire.JobAssistant.model.resume;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hellFire.JobAssistant.model.BaseDocument;

import lombok.Getter;
import lombok.Setter;

/**
 * Parsed resume profile stored in MongoDB. {@link #userId} links to {@link com.hellFire.JobAssistant.model.User}.
 */
@Getter
@Setter
@Document(collection = "resumes")
@CompoundIndex(name = "user_default", def = "{'userId': 1, 'isDefault': 1}")
public class Resume extends BaseDocument {

	@Indexed
	private String userId;

	/** User-facing label, e.g. file name or "Backend 2026". */
	private String label;

	/** When true, treat as primary resume for the user (enforce single default in service layer). */
	private boolean isDefault;

	private PersonalInfo personalInfo;

	private String summary;

	private ResumeSkills skills;

	private List<ExperienceEntry> experiences = new ArrayList<>();

	private List<ProjectEntry> projects = new ArrayList<>();

	private List<EducationEntry> education = new ArrayList<>();

	private List<CertificationEntry> certifications = new ArrayList<>();

	private List<String> achievements = new ArrayList<>();

	private List<String> languages = new ArrayList<>();

	private ResumePreferences preferences;

	private ResumeProfileMetadata metadata;

	/** Optional raw LLM / parser output for debugging or re-processing. */
	private Map<String, Object> rawModelJson;
}
