package com.hellFire.JobAssistant.dto.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hellFire.JobAssistant.model.resume.CertificationEntry;
import com.hellFire.JobAssistant.model.resume.EducationEntry;
import com.hellFire.JobAssistant.model.resume.ExperienceEntry;
import com.hellFire.JobAssistant.model.resume.PersonalInfo;
import com.hellFire.JobAssistant.model.resume.ProjectEntry;
import com.hellFire.JobAssistant.model.resume.ResumePreferences;
import com.hellFire.JobAssistant.model.resume.ResumeProfileMetadata;
import com.hellFire.JobAssistant.model.resume.ResumeSkills;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeUpdateRequest {

	private String label;

	private Boolean isDefault;

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

	private Map<String, Object> rawModelJson;
}
