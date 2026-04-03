package com.hellFire.JobAssistant.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hellFire.JobAssistant.model.User;

public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findByOauthPrincipalKey(String oauthPrincipalKey);

	Optional<User> findByEmailIgnoreCase(String email);
}
