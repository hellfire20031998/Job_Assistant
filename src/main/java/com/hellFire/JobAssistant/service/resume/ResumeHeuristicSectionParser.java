package com.hellFire.JobAssistant.service.resume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.hellFire.JobAssistant.model.resume.EducationEntry;
import com.hellFire.JobAssistant.model.resume.ExperienceEntry;
import com.hellFire.JobAssistant.model.resume.ProjectEntry;
import com.hellFire.JobAssistant.model.resume.Resume;
import com.hellFire.JobAssistant.model.resume.ResumeSkills;

/**
 * Section-aware heuristics over {@link Resume#getExtractedText()} until LLM parsing exists.
 */
final class ResumeHeuristicSectionParser {

	private static final Pattern SECTION_HEADER = Pattern.compile(
			"^(EXPERIENCE|PROJECTS|SKILLS|ACHIEVEMENTS|EDUCATION)$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern SKILL_LABEL = Pattern.compile(
			"^(Languages?|Backend|Frontend|Databases?|Tools|Core\\s*Concepts?)\\s*:\\s*(.+)$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern COMPANY_DATES = Pattern.compile(
			"^(.+?)\\s+((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\.?\\s+\\d{4})\\s*[–\\-]\\s*(Present|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\.?\\s+\\d{4}|\\d{4})\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern PAREN_TECH_LINE = Pattern.compile("^\\((.+)\\)$");

	private static final Pattern BULLET = Pattern.compile("^[•\\-*]\\s*(.+)$");

	private ResumeHeuristicSectionParser() {
	}

	static String normalizeLines(String text) {
		if (text == null) {
			return "";
		}
		return Arrays.stream(text.replace('\r', '\n').split("\n"))
				.map(l -> l.replaceAll("[ \t]+", " ").trim())
				.filter(ResumeHeuristicSectionParser::keepLine)
				.collect(Collectors.joining("\n"));
	}

	private static boolean keepLine(String line) {
		if (line.isEmpty()) {
			return false;
		}
		if (line.contains("Paste one or more documents")) {
			return false;
		}
		if (line.matches("(?s)^/\\*\\*.*\\*/$")) {
			return false;
		}
		return true;
	}

	static Map<String, String> splitSections(String[] lines) {
		Map<String, Integer> headerLine = new LinkedHashMap<>();
		for (int i = 0; i < lines.length; i++) {
			String t = lines[i].trim();
			Matcher m = SECTION_HEADER.matcher(t);
			if (m.matches()) {
				headerLine.putIfAbsent(m.group(1).toUpperCase(Locale.ROOT), i);
			}
		}
		Map<String, String> out = new LinkedHashMap<>();
		for (Map.Entry<String, Integer> e : headerLine.entrySet()) {
			int start = e.getValue() + 1;
			int end = nextSectionLine(lines, e.getValue());
			if (start < end) {
				out.put(e.getKey(), String.join("\n", Arrays.copyOfRange(lines, start, end)));
			} else {
				out.put(e.getKey(), "");
			}
		}
		return out;
	}

	private static int nextSectionLine(String[] lines, int headerIdx) {
		for (int i = headerIdx + 1; i < lines.length; i++) {
			String t = lines[i].trim();
			if (SECTION_HEADER.matcher(t).matches()) {
				return i;
			}
		}
		return lines.length;
	}

	static void parseLabeledSkills(String skillsBlock, ResumeSkills skills) {
		if (!StringUtils.hasText(skillsBlock)) {
			return;
		}
		for (String raw : skillsBlock.split("\n")) {
			String line = raw.trim();
			if (line.isEmpty()) {
				continue;
			}
			Matcher m = SKILL_LABEL.matcher(line);
			if (!m.matches()) {
				continue;
			}
			String label = m.group(1).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
			List<String> parts = splitCsvSkills(m.group(2));
			if (label.startsWith("language")) {
				skills.getLanguages().addAll(parts);
			} else if (label.equals("backend")) {
				skills.getBackend().addAll(parts);
			} else if (label.equals("frontend")) {
				skills.getFrontend().addAll(parts);
			} else if (label.startsWith("database")) {
				skills.getDatabases().addAll(parts);
			} else if (label.equals("tools")) {
				skills.getTools().addAll(parts);
			} else if (label.contains("concept")) {
				skills.getConcepts().addAll(parts);
			}
		}
	}

	private static List<String> splitCsvSkills(String value) {
		List<String> out = new ArrayList<>();
		for (String p : value.split(",")) {
			String t = p.trim();
			if (StringUtils.hasText(t)) {
				out.add(t);
			}
		}
		return out;
	}

	static List<ExperienceEntry> parseExperiences(String block) {
		List<ExperienceEntry> list = new ArrayList<>();
		if (!StringUtils.hasText(block)) {
			return list;
		}
		String[] lines = block.split("\n");
		String pendingTitle = null;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if (line.isEmpty()) {
				continue;
			}
			Matcher bullet = BULLET.matcher(line);
			if (bullet.matches()) {
				if (!list.isEmpty()) {
					list.get(list.size() - 1).getHighlights().add(bullet.group(1).trim());
				}
				continue;
			}
			Matcher cd = COMPANY_DATES.matcher(line);
			if (cd.matches() && StringUtils.hasText(pendingTitle)) {
				ExperienceEntry ex = new ExperienceEntry();
				ex.setTitle(pendingTitle.trim());
				ex.setCompany(cd.group(1).trim());
				ex.setStartDate(cd.group(2).trim());
				ex.setEndDate(cd.group(3).trim());
				list.add(ex);
				pendingTitle = null;
				continue;
			}
			pendingTitle = line;
		}
		return list;
	}

	static List<ProjectEntry> parseProjects(String block) {
		List<ProjectEntry> list = new ArrayList<>();
		if (!StringUtils.hasText(block)) {
			return list;
		}
		ProjectEntry cur = null;
		for (String raw : block.split("\n")) {
			String line = raw.trim();
			if (line.isEmpty()) {
				continue;
			}
			Matcher bullet = BULLET.matcher(line);
			if (bullet.matches()) {
				if (cur != null) {
					cur.getHighlights().add(bullet.group(1).trim());
				}
				continue;
			}
			Matcher paren = PAREN_TECH_LINE.matcher(line);
			if (paren.matches() && cur != null) {
				cur.getTechStack().addAll(splitCsvSkills(paren.group(1)));
				continue;
			}
			if (cur != null && StringUtils.hasText(cur.getName())) {
				list.add(cur);
			}
			cur = new ProjectEntry();
			cur.setName(line);
		}
		if (cur != null && StringUtils.hasText(cur.getName())) {
			list.add(cur);
		}
		return list;
	}

	static List<EducationEntry> parseEducation(String block) {
		List<EducationEntry> list = new ArrayList<>();
		if (!StringUtils.hasText(block)) {
			return list;
		}
		List<String> lines = Arrays.stream(block.split("\n")).map(String::trim).filter(StringUtils::hasText)
				.collect(Collectors.toList());
		if (lines.size() >= 2) {
			EducationEntry ed = new EducationEntry();
			String degreeLine = lines.get(0);
			ed.setDegree(degreeLine);
			Matcher m = Pattern.compile("^(.+?)\\(([^)]+)\\)\\s*$").matcher(degreeLine);
			if (m.matches()) {
				ed.setDegree(m.group(1).trim());
				ed.setField(m.group(2).trim());
			}
			String schoolLine = lines.get(1);
			Matcher end = Pattern.compile("^(.*?)\\s+(\\d{4})\\s*$").matcher(schoolLine);
			if (end.matches()) {
				ed.setSchool(end.group(1).trim().replaceAll("\\s+", " "));
				ed.setEndDate(end.group(2));
			} else {
				ed.setSchool(schoolLine);
			}
			list.add(ed);
		}
		return list;
	}

	static List<String> parseAchievementBullets(String block) {
		List<String> out = new ArrayList<>();
		if (!StringUtils.hasText(block)) {
			return out;
		}
		for (String raw : block.split("\n")) {
			String line = raw.trim();
			Matcher bullet = BULLET.matcher(line);
			if (bullet.matches()) {
				out.add(bullet.group(1).trim());
			}
		}
		return out;
	}

	static String buildIntroSummary(String[] lines, int upToLineExclusive) {
		StringBuilder sb = new StringBuilder();
		int end = Math.min(upToLineExclusive, lines.length);
		for (int i = 0; i < end; i++) {
			String t = lines[i].trim();
			if (t.isEmpty()) {
				continue;
			}
			if (SECTION_HEADER.matcher(t).matches()) {
				break;
			}
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(t);
		}
		String s = sb.toString().replaceAll("\\s+", " ").trim();
		if (s.length() > 700) {
			int cut = s.lastIndexOf(' ', 700);
			s = (cut > 400 ? s.substring(0, cut) : s.substring(0, 700)) + "…";
		}
		return StringUtils.hasText(s) ? s : null;
	}

	static int firstSectionLine(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			if (SECTION_HEADER.matcher(lines[i].trim()).matches()) {
				return i;
			}
		}
		return lines.length;
	}

	static String toTitleName(String line) {
		if (!StringUtils.hasText(line)) {
			return line;
		}
		String[] parts = line.split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (p.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(' ');
			}
			if (p.length() == 1) {
				sb.append(p.toUpperCase(Locale.ROOT));
			} else {
				sb.append(Character.toUpperCase(p.charAt(0)));
				sb.append(p.substring(1).toLowerCase(Locale.ROOT));
			}
		}
		return sb.toString();
	}

	/** Keyword scan with word boundaries; longer phrases first to reduce false positives (e.g. Go). */
	static void mergeKeywordTools(String text, ResumeSkills skills, List<String> orderedKeywords) {
		if (!StringUtils.hasText(text)) {
			return;
		}
		Set<String> already = new LinkedHashSet<>();
		already.addAll(skills.getLanguages());
		already.addAll(skills.getBackend());
		already.addAll(skills.getFrontend());
		already.addAll(skills.getDatabases());
		already.addAll(skills.getTools());
		already.addAll(skills.getConcepts());
		String norm = text.replace('\r', '\n');
		for (String tech : orderedKeywords) {
			if (containsPhrase(norm, tech)) {
				if (!already.contains(tech)) {
					skills.getTools().add(tech);
					already.add(tech);
				}
			}
		}
	}

	private static boolean containsPhrase(String text, String tech) {
		String esc = Pattern.quote(tech);
		if (tech.contains(" ")) {
			return Pattern.compile(esc.replace("\\ ", "\\s+"), Pattern.CASE_INSENSITIVE).matcher(text).find();
		}
		if (tech.length() <= 2) {
			return Pattern.compile("\\b" + esc + "\\b").matcher(text).find();
		}
		if ("Java".equalsIgnoreCase(tech)) {
			return Pattern.compile("\\bJava\\b(?!\\s*Script)").matcher(text).find();
		}
		return Pattern.compile("\\b" + esc + "\\b", Pattern.CASE_INSENSITIVE).matcher(text).find();
	}

	static double confidence(Resume resume) {
		double score = 0.2;
		if (resume.getPersonalInfo() != null && StringUtils.hasText(resume.getPersonalInfo().getEmail())) {
			score += 0.1;
		}
		if (!resume.getExperiences().isEmpty()) {
			score += 0.15;
		}
		if (!resume.getProjects().isEmpty()) {
			score += 0.1;
		}
		ResumeSkills sk = resume.getSkills();
		if (sk != null && (!sk.getLanguages().isEmpty() || !sk.getBackend().isEmpty())) {
			score += 0.15;
		}
		if (!resume.getEducation().isEmpty()) {
			score += 0.1;
		}
		if (!resume.getAchievements().isEmpty()) {
			score += 0.05;
		}
		return Math.min(score, 0.85);
	}
}
