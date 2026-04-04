package com.hellFire.JobAssistant.service.resume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hellFire.JobAssistant.model.User;
import com.hellFire.JobAssistant.model.resume.PersonalInfo;
import com.hellFire.JobAssistant.model.resume.Resume;
import com.hellFire.JobAssistant.model.resume.ResumePreferences;
import com.hellFire.JobAssistant.model.resume.ResumeProfileMetadata;
import com.hellFire.JobAssistant.model.resume.ResumeSkills;

/**
 * Heuristic profile until LLM parsing is added. Populates basics from {@link User} + PDF text patterns.
 */
@Component
public class ResumeStubProfileBuilder {

	private static final Pattern EMAIL = Pattern.compile(
			"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern PHONE = Pattern.compile(
			"(\\+?\\d[\\d\\s().-]{8,}\\d)");

	private static final Pattern LINKEDIN = Pattern.compile(
			"https?://(?:www\\.)?linkedin\\.com/[^\\s)]+",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern GITHUB = Pattern.compile(
			"https?://(?:www\\.)?github\\.com/[^\\s)]+",
			Pattern.CASE_INSENSITIVE);

	private static final List<String> TECH_KEYWORDS = List.of(
			"Java", "Spring", "Spring Boot", "Kotlin", "Python", "JavaScript", "TypeScript", "React", "Next.js",
			"Node.js", "MongoDB", "PostgreSQL", "Redis", "Kafka", "Docker", "Kubernetes", "AWS", "Git", "REST",
			"GraphQL", "Microservices", "SQL", "HTML", "CSS", "Tailwind", "Go", "Rust", "C++", "TensorFlow");

	public void apply(Resume resume, User user, String rawText) {
		String text = rawText != null ? rawText : "";
		String normalized = ResumeHeuristicSectionParser.normalizeLines(text);
		String[] lines = normalized.isEmpty() ? new String[0] : normalized.split("\n");

		PersonalInfo pi = new PersonalInfo();
		pi.setEmail(firstEmail(text).orElse(user.getEmail()));
		pi.setFullName(resolveFullName(user, lines));
		phoneMatch(text).ifPresent(pi::setPhone);
		urlMatch(LINKEDIN, text).ifPresent(pi::setLinkedin);
		urlMatch(GITHUB, text).ifPresent(pi::setGithub);
		resume.setPersonalInfo(pi);

		int firstSec = ResumeHeuristicSectionParser.firstSectionLine(lines);
		String summary = ResumeHeuristicSectionParser.buildIntroSummary(lines, firstSec);
		if (!StringUtils.hasText(summary)) {
			summary = buildSummaryFallback(normalized);
		}
		resume.setSummary(summary);

		Map<String, String> sections = ResumeHeuristicSectionParser.splitSections(lines);

		ResumeSkills skills = new ResumeSkills();
		ResumeHeuristicSectionParser.parseLabeledSkills(sections.getOrDefault("SKILLS", ""), skills);
		List<String> keywordsByLength = TECH_KEYWORDS.stream()
				.sorted(Comparator.comparingInt(String::length).reversed())
				.collect(Collectors.toList());
		ResumeHeuristicSectionParser.mergeKeywordTools(text, skills, keywordsByLength);
		dedupeSkillLists(skills);
		resume.setSkills(skills);

		resume.setExperiences(new ArrayList<>(ResumeHeuristicSectionParser.parseExperiences(sections.getOrDefault("EXPERIENCE", ""))));
		resume.setProjects(new ArrayList<>(ResumeHeuristicSectionParser.parseProjects(sections.getOrDefault("PROJECTS", ""))));
		resume.setEducation(new ArrayList<>(ResumeHeuristicSectionParser.parseEducation(sections.getOrDefault("EDUCATION", ""))));
		resume.setCertifications(new ArrayList<>());
		resume.setAchievements(new ArrayList<>(ResumeHeuristicSectionParser.parseAchievementBullets(sections.getOrDefault("ACHIEVEMENTS", ""))));
		resume.setLanguages(new ArrayList<>());

		ResumePreferences pref = new ResumePreferences();
		pref.setJobType(new ArrayList<>());
		pref.setRelocation(Boolean.FALSE);
		pref.setExpectedCTC(null);
		pref.setNoticePeriod(null);
		resume.setPreferences(pref);

		ResumeProfileMetadata meta = new ResumeProfileMetadata();
		meta.setSource("resume_upload");
		meta.setLastUpdated(java.time.Instant.now());
		meta.setConfidenceScore(ResumeHeuristicSectionParser.confidence(resume));
		resume.setMetadata(meta);
	}

	private static void dedupeSkillLists(ResumeSkills skills) {
		skills.setLanguages(new ArrayList<>(new LinkedHashSet<>(skills.getLanguages())));
		skills.setBackend(new ArrayList<>(new LinkedHashSet<>(skills.getBackend())));
		skills.setFrontend(new ArrayList<>(new LinkedHashSet<>(skills.getFrontend())));
		skills.setDatabases(new ArrayList<>(new LinkedHashSet<>(skills.getDatabases())));
		skills.setTools(new ArrayList<>(new LinkedHashSet<>(skills.getTools())));
		skills.setConcepts(new ArrayList<>(new LinkedHashSet<>(skills.getConcepts())));
	}

	private static java.util.Optional<String> firstEmail(String text) {
		Matcher m = EMAIL.matcher(text);
		return m.find() ? java.util.Optional.of(m.group()) : java.util.Optional.empty();
	}

	private static java.util.Optional<String> phoneMatch(String text) {
		Matcher m = PHONE.matcher(text);
		return m.find() ? java.util.Optional.of(m.group(1).trim()) : java.util.Optional.empty();
	}

	private static java.util.Optional<String> urlMatch(Pattern p, String text) {
		Matcher m = p.matcher(text);
		return m.find() ? java.util.Optional.of(m.group().trim()) : java.util.Optional.empty();
	}

	private static String resolveFullName(User user, String[] lines) {
		if (StringUtils.hasText(user.getDisplayName())) {
			return user.getDisplayName().trim();
		}
		String firstLine = Arrays.stream(lines)
				.map(String::trim)
				.filter(s -> !s.isEmpty() && !EMAIL.matcher(s).find() && s.length() < 80)
				.findFirst()
				.orElse("");
		return StringUtils.hasText(firstLine) ? ResumeHeuristicSectionParser.toTitleName(firstLine) : "";
	}

	private static String buildSummaryFallback(String normalized) {
		String compact = normalized.replaceAll("\n{3,}", "\n\n").trim();
		if (compact.length() <= 800) {
			return compact.isEmpty() ? null : compact;
		}
		return compact.substring(0, 800) + "…";
	}
}
