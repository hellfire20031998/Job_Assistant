package com.hellFire.JobAssistant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hellFire.JobAssistant.model.resume.Resume;

public interface ResumeRepository extends MongoRepository<Resume, String> {

	List<Resume> findByUserIdOrderByUpdatedAtDesc(String userId);

	Optional<Resume> findByUserIdAndIsDefaultTrue(String userId);

	long countByUserId(String userId);
}
