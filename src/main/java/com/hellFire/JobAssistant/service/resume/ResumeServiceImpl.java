package com.hellFire.JobAssistant.service.resume;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hellFire.JobAssistant.dto.request.ResumeUpdateRequest;
import com.hellFire.JobAssistant.dto.response.ResumeResponse;
import com.hellFire.JobAssistant.dto.response.ResumeSummaryResponse;
import com.hellFire.JobAssistant.exception.BusinessException;
import com.hellFire.JobAssistant.model.User;
import com.hellFire.JobAssistant.model.resume.ParseStatus;
import com.hellFire.JobAssistant.model.resume.Resume;
import com.hellFire.JobAssistant.model.resume.ResumeProfileMetadata;
import com.hellFire.JobAssistant.repository.ResumeRepository;
import com.hellFire.JobAssistant.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

	private final ResumeRepository resumeRepository;
	private final UserService userService;
	private final ResumePdfTextExtractor pdfTextExtractor;
	private final ResumeStubProfileBuilder stubProfileBuilder;

	@Value("${app.resume.upload.max-bytes:5242880}")
	private long maxUploadBytes;

	@Value("${app.resume.max-per-user:5}")
	private int maxResumesPerUser;

	@Override
	public ResumeResponse upload(Authentication authentication, MultipartFile file) {
		User user = userService.requireUserForMe(authentication);
		if (resumeRepository.countByUserId(user.getId()) >= maxResumesPerUser) {
			throw new BusinessException(
					"Maximum of " + maxResumesPerUser + " resumes allowed. Delete one to upload another.",
					409);
		}
		if (file == null || file.isEmpty()) {
			throw new BusinessException("File is required", 400);
		}
		if (file.getSize() > maxUploadBytes) {
			throw new BusinessException("File exceeds maximum size of " + maxUploadBytes + " bytes", 413);
		}
		String filename = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "resume.pdf";
		if (!filename.toLowerCase().endsWith(".pdf")) {
			throw new BusinessException("Only PDF files are supported", 400);
		}
		String ct = file.getContentType();
		if (StringUtils.hasText(ct)
				&& !ct.equalsIgnoreCase("application/pdf")
				&& !ct.equalsIgnoreCase("application/x-pdf")
				&& !ct.equalsIgnoreCase("application/octet-stream")) {
			throw new BusinessException("Only PDF uploads are supported", 400);
		}

		Resume resume = new Resume();
		resume.setUserId(user.getId());
		resume.setLabel(filename);
		resume.setParseStatus(ParseStatus.PENDING);

		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (Exception e) {
			throw new BusinessException("Failed to read upload: " + e.getMessage(), 400);
		}

		String text;
		try {
			text = pdfTextExtractor.extractText(bytes, filename);
		} catch (Exception e) {
			resume.setParseStatus(ParseStatus.FAILED);
			resume.setParseError("Could not parse PDF: " + e.getMessage());
			if (resumeRepository.countByUserId(user.getId()) == 0) {
				resume.setDefault(true);
			}
			return ResumeResponse.from(resumeRepository.save(resume));
		}

		if (!StringUtils.hasText(text.trim())) {
			resume.setParseStatus(ParseStatus.FAILED);
			resume.setParseError("No text could be extracted from this PDF");
			if (resumeRepository.countByUserId(user.getId()) == 0) {
				resume.setDefault(true);
			}
			return ResumeResponse.from(resumeRepository.save(resume));
		}

		resume.setExtractedText(text);
		resume.setParseStatus(ParseStatus.READY);
		resume.setParseError(null);
		stubProfileBuilder.apply(resume, user, text);

		boolean first = resumeRepository.countByUserId(user.getId()) == 0;
		resume.setDefault(first);
		if (first) {
			clearOtherDefaults(user.getId(), null);
		}

		return ResumeResponse.from(resumeRepository.save(resume));
	}

	@Override
	public List<ResumeSummaryResponse> list(Authentication authentication) {
		User user = userService.requireUserForMe(authentication);
		return resumeRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
				.map(ResumeSummaryResponse::from)
				.toList();
	}

	@Override
	public ResumeResponse get(Authentication authentication, String resumeId) {
		User user = userService.requireUserForMe(authentication);
		Resume resume = resumeRepository.findById(resumeId)
				.orElseThrow(() -> new BusinessException("Resume not found", 404));
		assertOwner(user.getId(), resume);
		return ResumeResponse.from(resume);
	}

	@Override
	public ResumeResponse update(Authentication authentication, String resumeId, ResumeUpdateRequest request) {
		User user = userService.requireUserForMe(authentication);
		Resume resume = resumeRepository.findById(resumeId)
				.orElseThrow(() -> new BusinessException("Resume not found", 404));
		assertOwner(user.getId(), resume);

		if (request.getLabel() != null) {
			resume.setLabel(request.getLabel());
		}
		if (request.getIsDefault() != null && Boolean.TRUE.equals(request.getIsDefault())) {
			clearOtherDefaults(user.getId(), resume.getId());
			resume.setDefault(true);
		} else if (request.getIsDefault() != null) {
			resume.setDefault(false);
		}

		if (request.getPersonalInfo() != null) {
			resume.setPersonalInfo(request.getPersonalInfo());
		}
		if (request.getSummary() != null) {
			resume.setSummary(request.getSummary());
		}
		if (request.getSkills() != null) {
			resume.setSkills(request.getSkills());
		}
		if (request.getExperiences() != null) {
			resume.setExperiences(request.getExperiences());
		}
		if (request.getProjects() != null) {
			resume.setProjects(request.getProjects());
		}
		if (request.getEducation() != null) {
			resume.setEducation(request.getEducation());
		}
		if (request.getCertifications() != null) {
			resume.setCertifications(request.getCertifications());
		}
		if (request.getAchievements() != null) {
			resume.setAchievements(request.getAchievements());
		}
		if (request.getLanguages() != null) {
			resume.setLanguages(request.getLanguages());
		}
		if (request.getPreferences() != null) {
			resume.setPreferences(request.getPreferences());
		}
		if (request.getMetadata() != null) {
			ResumeProfileMetadata merged = request.getMetadata();
			if (resume.getMetadata() != null && merged.getSource() == null) {
				merged.setSource(resume.getMetadata().getSource());
			}
			if (merged.getSource() == null) {
				merged.setSource("manual");
			}
			merged.setLastUpdated(Instant.now());
			resume.setMetadata(merged);
		} else if (resume.getMetadata() != null) {
			resume.getMetadata().setLastUpdated(Instant.now());
		}
		if (request.getRawModelJson() != null) {
			resume.setRawModelJson(request.getRawModelJson());
		}

		return ResumeResponse.from(resumeRepository.save(resume));
	}

	@Override
	public void delete(Authentication authentication, String resumeId) {
		User user = userService.requireUserForMe(authentication);
		Resume resume = resumeRepository.findById(resumeId)
				.orElseThrow(() -> new BusinessException("Resume not found", 404));
		assertOwner(user.getId(), resume);
		boolean wasDefault = resume.isDefault();
		resumeRepository.deleteById(resumeId);
		if (wasDefault) {
			List<Resume> remaining = resumeRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
			if (!remaining.isEmpty()) {
				Resume next = remaining.get(0);
				clearOtherDefaults(user.getId(), next.getId());
				next.setDefault(true);
				resumeRepository.save(next);
			}
		}
	}

	private void assertOwner(String userId, Resume resume) {
		if (!Objects.equals(userId, resume.getUserId())) {
			throw new BusinessException("Resume not found", 404);
		}
	}

	private void clearOtherDefaults(String userId, String exceptId) {
		List<Resume> all = resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
		for (Resume r : all) {
			if (exceptId != null && exceptId.equals(r.getId())) {
				continue;
			}
			if (r.isDefault()) {
				r.setDefault(false);
				resumeRepository.save(r);
			}
		}
	}
}
