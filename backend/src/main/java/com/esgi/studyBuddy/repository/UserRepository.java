package com.esgi.studyBuddy.repository;

import com.esgi.studyBuddy.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);


    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteUserByEmail(String email);
}
