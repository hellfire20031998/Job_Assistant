package com.hellFire.JobAssistant.model.resume;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeSkills {

	private List<String> languages = new ArrayList<>();

	private List<String> backend = new ArrayList<>();

	private List<String> frontend = new ArrayList<>();

	private List<String> databases = new ArrayList<>();

	private List<String> tools = new ArrayList<>();

	private List<String> concepts = new ArrayList<>();
}
