package com.hellFire.JobAssistant.model.resume;

/**
 * {@link #PENDING} reserved for async / future LLM pipeline; upload flow sets {@link #READY} or {@link #FAILED}.
 */
public enum ParseStatus {
	PENDING,
	READY,
	FAILED
}
