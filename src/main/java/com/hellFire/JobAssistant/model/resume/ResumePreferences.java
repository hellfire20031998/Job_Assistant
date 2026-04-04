package com.hellFire.JobAssistant.model.resume;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code jobType} entries: {@code remote}, {@code onsite}, {@code hybrid}.
 */
@Getter
@Setter
public class ResumePreferences {

	private List<String> jobType = new ArrayList<>();

	private Boolean relocation;

	private String expectedCTC;

	private String noticePeriod;
}
