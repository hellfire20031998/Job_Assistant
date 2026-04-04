package com.hellFire.JobAssistant.model.resume;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code type} values: {@code full-time}, {@code internship}, {@code freelance}, {@code contract}.
 */
@Getter
@Setter
public class ExperienceEntry {

	private String title;

	private String company;

	private String location;

	private String type;

	private String startDate;

	private String endDate;

	private List<String> technologies = new ArrayList<>();

	private List<String> highlights = new ArrayList<>();
}
