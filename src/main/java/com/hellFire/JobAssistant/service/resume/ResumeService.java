package com.hellFire.JobAssistant.service.resume;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.hellFire.JobAssistant.dto.request.ResumeUpdateRequest;
import com.hellFire.JobAssistant.dto.response.ResumeResponse;
import com.hellFire.JobAssistant.dto.response.ResumeSummaryResponse;

public interface ResumeService {

	ResumeResponse upload(Authentication authentication, MultipartFile file);

	List<ResumeSummaryResponse> list(Authentication authentication);

	ResumeResponse get(Authentication authentication, String resumeId);

	ResumeResponse update(Authentication authentication, String resumeId, ResumeUpdateRequest request);

	void delete(Authentication authentication, String resumeId);
}
