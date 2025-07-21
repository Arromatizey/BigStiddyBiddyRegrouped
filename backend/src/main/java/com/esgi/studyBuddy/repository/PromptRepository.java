package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface PromptRepository extends JpaRepository<Prompt, UUID> {
    List<Prompt> findByRoom_IdOrderByCreatedAtDesc(UUID roomId);
}
