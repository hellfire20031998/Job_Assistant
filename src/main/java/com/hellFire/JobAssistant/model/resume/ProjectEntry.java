package com.hellFire.JobAssistant.model.resume;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectEntry {

	private String name;

	private String description;

	private List<String> techStack = new ArrayList<>();

	private String startDate;

	private String endDate;

	private List<String> highlights = new ArrayList<>();

	private String githubUrl;

	private String liveUrl;
}
