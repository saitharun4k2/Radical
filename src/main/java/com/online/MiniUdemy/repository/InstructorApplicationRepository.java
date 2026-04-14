package com.online.MiniUdemy.repository;

import com.online.MiniUdemy.entity.InstructorApplication;
import com.online.MiniUdemy.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, Long> {
    // Custom query to let the Admin find only PENDING applications
    List<InstructorApplication> findByStatus(ApplicationStatus status);
}